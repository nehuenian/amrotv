# AMRO — Advanced Movie Recommendation Organisation

> **Evaluator handover document.** This README describes the project from an architectural and
> decision-making perspective so you can quickly understand the code without a guided walkthrough.

---

## Overview

AMRO is an Android app that lets users browse trending movies (sourced from the
[TMDB API](https://www.themoviedb.org/documentation/api)) and view a rich detail page for each
film. The project is a technical showcase that demonstrates:

- Clean Architecture with a strict multi-module separation
- MVI (Model-View-Intent) with pure, injectable state reducers
- Testability at every layer (unit → E2E instrumented)
- Jetpack Compose with Material 3

There are two screens:

| Screen | Purpose |
|--------|---------|
| **Trending Movies** | Shows up to 100 trending movies for the week, filterable by genre |
| **Movie Detail** | Full detail page: poster, tagline, overview, genres, runtime, budget, revenue |

---

## Architecture

The app follows **Clean Architecture** with a strict dependency rule: outer layers depend on inner
ones; inner layers know nothing about the outer world.

```
┌──────────────────────────────────────────────────────┐
│  :app  (wires everything; provides BuildConfig impl) │
├──────────────────┬───────────────────────────────────┤
│  :feature:movies:nav  (Navigation3 entry builder)    │
├──────────────────┴───────────────────────────────────┤
│  UI Layer     │  :feature:movies:ui                  │
│               │  Jetpack Compose screens + previews  │
├───────────────┼──────────────────────────────────────┤
│  Presentation │  :presentation:api  (State/Intent/   │
│  Layer        │    Effect data classes)               │
│               │  :presentation:implementation        │
│               │  (ViewModels + StateReducers)         │
├───────────────┼──────────────────────────────────────┤
│  Domain       │  :domain:api  (Repository + UseCase  │
│  Layer        │    interfaces, domain models)         │
│               │  :domain:implementation              │
│               │  (UseCase implementations)           │
├───────────────┼──────────────────────────────────────┤
│  Data Layer   │  :feature:movies:data                │
│               │  (Retrofit, DTOs, Mappers,            │
│               │   Repository implementation)          │
└───────────────┴──────────────────────────────────────┘
```

### Why MVI (not MVVM)?

MVVM leaves the data flow open: a ViewModel can expose multiple `LiveData` or `StateFlow` streams
and the View may write back to the ViewModel through arbitrary methods. State can end up scattered
across several streams, and one-shot events (navigation, toasts) require conventions like
`SingleLiveEvent` that are easy to break.

MVI enforces **strict unidirectional data flow**:

```
  User Interaction
       │  (Intent)
       ▼
  ViewModel
       │  applies StateReducer → new State
       ▼
  StateFlow<State>   ──→   UI re-renders (pure function of state)
       │
       └─ Channel<Effect>     ──→  one-shot events (navigation, snackbars)
           (exposed as Flow)         consumed exactly once
```

Key differences from MVVM in this project:

| Concern | MVVM (typical) | MVI (this project) |
|---------|---------------|-------------------|
| State shape | Multiple streams | Single `State` data class in `StateFlow` |
| State transitions | ViewModel mutates fields directly | `StateReducer` lambdas — pure, named, independently testable |
| User actions | Arbitrary method calls on ViewModel | Sealed `Intent` — every possible user action is enumerated |
| One-shot events | Convention-based (`SingleLiveEvent`) | `Effect` via `Channel` — consumed exactly once, never re-delivered on re-subscribe |
| Testability | Must instantiate ViewModel to test state logic | `StateReducer`s are pure functions; tested without a ViewModel |

The explicit `Intent` sealed class is particularly valuable as a **forcing function for design**:
if you cannot name the user action as a sealed subtype, the feature scope is unclear.

---

## Module Graph

17 Gradle modules, all using type-safe project accessors (`projects.*`):

```
:app
 └─ :feature:movies:nav       ← single nav entry point for :app
      ├─ :feature:movies:ui
      │    └─ :feature:movies:presentation:api
      │         └─ :feature:movies:domain:api
      ├─ :feature:movies:presentation:implementation
      │    ├─ :feature:movies:presentation:api
      │    ├─ :feature:movies:domain:api
      │    └─ :core:mvi:android
      ├─ :feature:movies:data
      │    ├─ :feature:movies:domain:api
      │    ├─ :core:data        ← Retrofit/OkHttp setup, AuthInterceptor
      │    ├─ :core:mvi:kotlin  ← Mapper<I,O> interface
      │    └─ :libraries:logger:api
      └─ :feature:movies:domain:implementation
           └─ :feature:movies:domain:api
:core:domain          ← Outcome<T> sealed class (shared across features)
:core:mvi:kotlin      ← MviState/Intent/Effect, StateReducer, Mapper<I,O>
:core:mvi:android     ← BaseAmroTvViewModel (lifecycle-aware)
:core:data            ← NetworkModule (OkHttp, Retrofit.Builder, AuthInterceptor)
:core:build-config    ← BuildConfigProvider interface (decouples API token from modules)
:core:testing         ← Robot DSL for E2E instrumented tests
:core:ui              ← AmroTheme, shared Compose components, @LightDarkPreview
:libraries:logger:api ← Logger interface (pure Kotlin, no Android)
:libraries:logger:implementation ← TimberLogger + Hilt binding
```

### Why so many modules?

1. **Enforced layer boundaries** — the Gradle dependency graph prevents accidental
   cross-layer imports at compile time. A data-layer class can never import a ViewModel.
2. **Faster incremental builds** — unchanged modules are not recompiled.
3. **Scalability** — a second feature (e.g. `:feature:search`) is added as a parallel tree
   with zero changes to `:feature:movies`.

---

## Key Design Decisions

### 1. `Outcome<T>` — shared result type for the domain layer

`Outcome<T>` (Success / Error with optional stale data) lives in `:core:domain`, making it the
**standard way to report results across all features at the domain boundary**. Every repository
method and use case returns `Outcome<T>` — no feature invents its own result wrapper. Any future
feature (`:feature:search`, `:feature:profile`, …) gets the same type for free without depending
on `:feature:movies`.

The `Error` variant carries an optional `data` field so the UI can continue showing
stale content while surfacing the error — a pattern that works equally well for network failures,
empty states, and auth errors.

### 2. `suspend fun` in repositories, not `Flow`

All `MovieRepository` and `DataSource` methods are `suspend` returning a value directly.
A `Flow`-based API implies continuous emission — appropriate for a live database, not for a
one-shot TMDB network request. Callers that need live updates should trigger re-fetches via
MVI intents.

### 3. `DataSource` abstraction

`TmdbMovieDataSource` is bound to `RemoteMovieDataSource` via Hilt. Adding a second remote
source (e.g. OMDb) means implementing the interface and rebinding — `MovieRepositoryImpl` is
untouched. Same principle for `LocalMovieDataSource`.

### 4. `NoOpLocalMovieDataSource` — offline cache planned, not yet implemented

`NoOpLocalMovieDataSource` is the active Hilt binding and returns empty / no-ops for all local
operations. Room (entities, DAOs, `AmroDatabase`) is a planned future addition — none of those
classes exist yet. The repository contract (`LocalMovieDataSource`) is already in place so the
cache can be activated by implementing `RoomLocalMovieDataSource` and swapping the `@Binds`
binding in `DataBindingsModule` without touching `MovieRepositoryImpl`.

### 5. `StateReducer` as a pure `fun interface`

All state transitions are `StateReducer<S>` lambdas (e.g. `loading()`, `contentLoaded(...)`,
`loadFailed(...)`) grouped in a `{Screen}StateReducers` class. ViewModels never call `.copy()`
directly — all state logic is centralised, named, and unit-testable without a ViewModel.

### 6. Error enum in `presentation:api`

State holds `ImmutableList<{Feature}Error>` (an `enum class`) rather than `String?` or
`Throwable`. This keeps raw exception messages out of the state and forces the UI layer to own
the human-readable string resources, enabling proper localisation.

### 7. Navigation3 over Navigation Compose

Navigation3 (1.1.1) gives the developer full ownership of the back stack as a plain
`SnapshotStateList<Any>`. This avoids the NavController abstraction leak and enables
list-detail, multi-pane, and deep-link patterns with no library workarounds. Navigation keys
(`MoviesNavKey`) are `@Serializable` sealed interface entries in `:feature:movies:nav`.

### 8. `BuildConfigProvider` in `:core:build-config`

The TMDB API token is read from `amrotv.properties` at Gradle build time and injected into
`BuildConfig`. `BuildConfigProvider` is an interface in `:core:build-config` that abstracts this,
keeping networking modules free of any direct `BuildConfig` import. In tests, a fake can be
injected trivially.

### 9. `Mapper<I, O>` for all data transformations

All model conversions implement the `Mapper<I, O>` interface from `:core:mvi:kotlin`. This
makes mappers:
- **Injectable** — can receive collaborator mappers via constructor injection (e.g.
  `MovieDetailDataToDomainMapper` injects `GenreDataToDomainMapper`).
- **Testable in isolation** — no DTO extension functions; just `mapper.map(input)`.
- **Discoverable** — naming convention `{Model}{SourceLayer}To{TargetLayer}Mapper`
  (e.g. `GenreDataToDomainMapper`, `MovieDomainToPresentationMapper`).

### 10. `TrendingMovies` paginates to 100 unique movies

TMDB returns ≤20 results per page. `TmdbMovieDataSource` fetches pages sequentially until 100
unique movies are collected. Page 1 failure propagates immediately; subsequent page failures
stop pagination early and return however many movies were collected so far — preventing cascading
requests on rate limits or auth errors.

---

## API Key Setup

You need a free TMDB account and a **Read Access Token** (v4 Bearer token).

1. Register at <https://www.themoviedb.org/signup>
2. Go to **Settings → API → Create → Developer**
3. Copy the **API Read Access Token** (starts with `eyJ…`)
4. Create `amrotv.properties` at the **project root** (next to `settings.gradle.kts`):

```properties
TMDB_READ_ACCESS_TOKEN=eyJhbGciOiJIUzI1NiJ9...
```

> `amrotv.properties` is gitignored. The build reads it via `app/build.gradle.kts` and injects
> it into `BuildConfig.TMDB_READ_ACCESS_TOKEN`. If the file is missing the token defaults to an
> empty string and all network calls will fail with 401.

---

## Building & Running

```bash
# Debug APK
./gradlew :app:assembleDebug

# Install on connected device / emulator
./gradlew :app:installDebug

# Full build check (compile + unit tests + lint)
./gradlew build
```

Minimum requirements: **Android 7.0 (API 24)**, target **API 37**.

---

## Testing

The project uses a two-tier test pyramid:

### Unit tests

Pure JVM tests — no emulator needed. Every layer has unit tests:

| Class type | What is covered |
|-----------|-----------------|
| **Mappers** | Each `Mapper<I,O>` class is tested in isolation: null fields, edge values (zero budget/revenue → `null`), URL prefixing |
| **Repositories** | `MovieRepositoryImpl` — remote/local interplay, `Outcome` wrapping, error propagation |
| **Use cases** | `GetTrendingMoviesUseCase`, `GetMovieDetailUseCase`, `GetGenresUseCase` — orchestration logic and `Outcome` transformation |
| **ViewModels** | `TrendingMoviesViewModel`, `MovieDetailViewModel` — intent handling, state transitions, `Effect` emission, `MainDispatcherExtension` replaces `Dispatchers.Main` with a test dispatcher |
| **StateReducers** | `TrendingMoviesStateReducers` — pure state transformations verified without a ViewModel |

Key patterns:
- **JUnit 5** with `@Nested` + `@DisplayName` — test output reads like a spec
- **MockK** for coroutine-aware mocking (`coEvery`, `coVerify`)
- **Turbine** for `StateFlow` and `Channel` assertions
- GIVEN / WHEN / THEN three-level nesting with domain-language display names

```bash
# All unit tests
./gradlew test

# Single module
./gradlew :feature:movies:data:test
./gradlew :feature:movies:presentation:implementation:test
./gradlew :feature:movies:domain:implementation:test
```

### E2E instrumented tests (`:feature:movies:ui`)

Full-stack tests on a real emulator: Hilt injects a `MockWebServer`-backed Retrofit instance,
a real ViewModel is launched, and the Compose UI is driven via the **Robot Pattern**
(`withRobot<TrendingMoviesRobot> { ... }`).

```bash
./gradlew :feature:movies:ui:connectedDebugAndroidTest
```

> Requires a running emulator or connected device. The `HiltAndComposeE2EExtension` bridges
> JUnit 5 lifecycle callbacks with JUnit 4 Hilt and Compose rules via `RuleChain`.

---

## Future Improvements

The following items were discussed during development and deferred to keep the MVP scope
manageable. They are documented here so the next engineer knows the intent and the gaps.

### Logging — Datadog

The `Logger` interface (`libraries:logger:api`) is already abstracted behind a single
`fun log(level, tag, message, throwable?)` contract. The active implementation is `TimberLogger`.
Replacing it with Datadog (or any crash-reporting/observability SDK) means implementing
`Logger`, binding it in the Hilt module, and removing `TimberLogger` — no call site changes
anywhere in the app.

### Offline cache — implement and activate Room

The `LocalMovieDataSource` interface and `MovieRepositoryImpl` cache logic (fetch remote → write
local → serve local on failure) are already in place. What's missing is the Room implementation:
add `AmroDatabase`, DAOs, Room entities, and `RoomLocalMovieDataSource` to
`:feature:movies:data`; then swap the `@Binds` in `DataBindingsModule` from
`NoOpLocalMovieDataSource` to `RoomLocalMovieDataSource`. No other files need to change.

### Screenshot testing

The component library in `:feature:movies:ui` has `@LightDarkPreview` composable previews for
every screen and component. Adding screenshot tests (e.g. with
[Paparazzi](https://github.com/cashapp/paparazzi) or the
[Android Screenshot Testing](https://developer.android.com/studio/test/advanced-test-setup#screenshot-testing)
library) would lock visual regressions at CI time without an emulator.

### Design system module

`AmroTheme`, colour tokens, typography, and shape are currently in `:core:ui`. As the app grows
with more features, a dedicated `:design-system` module would own the token definitions, enforce
Material 3 component customisation, and provide a Showkase/Composable catalogue for designers.

### Internationalisation — dedicated `:core:i18n` module

`WeekRangeLabelProvider` currently formats week-range strings with hard-coded English patterns.
A `TODO` comment marks the location. The right long-term solution is a dedicated `:core:i18n`
module that owns all date/time formatting, locale-aware string construction, and RTL layout
helpers. This keeps formatting logic out of individual feature modules and makes it trivially
replaceable or mockable in tests. In the short term, replacing the current implementation with
`java.time.format.DateTimeFormatter` using locale-aware patterns would already handle device
locale and RTL scripts correctly.

### Release-year formatting

`extractReleaseYear` in `MapperUtils` uses a simple string split on `"-"`. A `TODO` marks
this for replacement with a proper `LocalDate` parser to handle varied TMDB date formats
and locale edge cases.

### CI / CD pipeline

There is no CI configuration yet. The natural additions are:
- **GitHub Actions** workflow running `./gradlew test detekt spotlessCheck` on every PR
- Separate workflow for `connectedDebugAndroidTest` on a Firebase Test Lab emulator matrix
- Release workflow producing a signed APK artifact

### Pagination library

The current pagination in `TmdbMovieDataSource` is a hand-rolled sequential loop capped at the
configured maximum. Replacing it with **Jetpack Paging 3** would give infinite scroll, loading
state management, and retry out of the box — appropriate once the UI moves to a true paginated
list rather than a single capped fetch.

---

## AI-Assisted Development Setup

This project was built with **GitHub Copilot CLI** as a pair programmer, using a structured
agent-based workflow. The setup is documented here so future contributors understand the
conventions and can continue using the same tooling.

### Skills

Skills are project-specific knowledge documents stored in `.github/skills/`. They encode all
architectural decisions, naming conventions, code templates, and patterns so Copilot generates
correct code without needing to re-explain the project on every session.

| Skill | Location | Purpose |
|-------|----------|---------|
| `architecture-reference` | `.github/skills/architecture-reference/` | **Auto-loaded for every task.** Full module structure, MVI pattern, Hilt DI setup, clean architecture layering, naming rules, Compose conventions, and code templates. |
| `create-feature-module` | `.github/skills/create-feature-module/` | Scaffolds all 6 sub-modules for a new feature in one shot. |
| `create-screen` | `.github/skills/create-screen/` | Adds a new MVI screen (State / Intent / Effect / ViewModel / Screen / Content) to an existing feature. |
| `commit-changes` | `.github/skills/commit-changes/` | Full git workflow — branch creation, staging, lint checks, Conventional Commits message format, and Co-author trailer. |
| `amro-ui-conventions` | `.github/skills/amro-ui-conventions/` | Compose-specific alignment decisions: MVI intent routing, Screen/Content split, LazyGrid sticky headers, design tokens, preview conventions. |

### Agents

Custom review agents are configured in `.github/copilot-instructions.md` and invoked after
every code-writing session:

| Agent | Scope | What it checks |
|-------|-------|----------------|
| `architecture-reviewer` | Domain, data, presentation logic, build files | Module boundaries, clean arch layers, Hilt DI, MVI contracts, logging, naming, KDoc, build files, data/networking patterns |
| `compose-reviewer` | `ui/` modules, `*Screen.kt`, `*Content.kt` | Recomposition, state hoisting, side effects, Screen/Content split, Material 3 tokens, accessibility, previews |

### Review loop

After every code change the workflow is:

1. Determine which agent(s) apply based on changed file scope.
2. Run the relevant agent(s) (both in parallel when both scopes are touched).
3. Address every reported violation.
4. Re-run until the agent reports **no remaining issues**.
5. Only then ask for git commit confirmation.

### Copilot instructions

`.github/copilot-instructions.md` governs all Copilot behaviour in this repository:

- **Auto-load rules**: which skills to load for which task types
- **Mandatory tooling**: LSP (Kotlin language server) for code intelligence; `android-cli` skill for all Android development tasks (Gradle, ADB, AVD)
- **Git hard-stops**: explicit user confirmation required before every `git commit`, `git push`, branch creation, or merge
- **Branch strategy**: one worktree per branch (`git worktree add ../amrotv-<branch> -b <branch>`)
- **Commit conventions**: Conventional Commits with `feature` (not `feat`) as the type for new features

---



### Detekt (static analysis)

Includes Compose-specific rules (`io.nlopez.compose.rules:detekt`).

```bash
./gradlew detekt
```

### Spotless (formatting)

```bash
# Check
./gradlew spotlessCheck

# Auto-fix
./gradlew spotlessApply
```

Both checks run as part of `./gradlew build`.

---

## Project Structure Quick-Reference

```
amrotv/
├── app/                                  # Application entry, DI root, AmroNavHost
├── build-logic/                          # Convention plugins (amro.android.library, etc.)
├── core/
│   ├── build-config/                     # BuildConfigProvider interface
│   ├── data/                             # NetworkModule (OkHttp, Retrofit, AuthInterceptor)
│   ├── domain/                           # Outcome<T> sealed class
│   ├── mvi/
│   │   ├── android/                      # BaseAmroTvViewModel
│   │   └── kotlin/                       # MviState/Intent/Effect, StateReducer, Mapper<I,O>
│   ├── testing/                          # Robot DSL for E2E tests
│   └── ui/                               # AmroTheme, @LightDarkPreview, shared components
├── libraries/
│   └── logger/
│       ├── api/                          # Logger interface (pure Kotlin)
│       └── implementation/               # TimberLogger + Hilt binding
└── feature/
    └── movies/
        ├── nav/                          # MoviesNavKey, moviesEntry (Navigation3 wiring)
        ├── data/                         # TmdbApiService, DTOs, Mapper classes, Repository impl
        ├── domain/
        │   ├── api/                      # MovieRepository, use case interfaces, domain models
        │   └── implementation/           # GetTrendingMoviesUseCase, GetMovieDetailUseCase, etc.
        ├── presentation/
        │   ├── api/                      # TrendingMoviesState/Intent/Effect, MovieDetailState/…
        │   └── implementation/           # ViewModels + StateReducers
        └── ui/                           # TrendingMoviesScreen, MovieDetailScreen, components
```

