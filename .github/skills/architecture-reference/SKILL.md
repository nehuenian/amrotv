---
name: architecture-reference
description: >
  AMRO app architecture reference — Android + MVI + Hilt + Jetpack Compose patterns,
  module structure, naming conventions, and code templates.
  Auto-load this skill whenever working on any AMRO code: writing a feature, reviewing Kotlin
  files, fixing a bug, creating a screen, wiring navigation, checking DI setup, or any task
  that touches the project's Kotlin source. When in doubt, load this skill first. If anything
  touches modules, build files, data layer, or networking, also check the reference files
  listed at the bottom. For unit and E2E testing conventions, load the `testing` skill.
user-invocable: true
---

# AMRO Architecture Reference

## Project Overview

**AMRO** (Advanced Movie Recommendation Organisation) is an **Android-only** app using:
- **Jetpack Compose + Material 3** for UI (no XML layouts)
- **MVI** (Model-View-Intent) with `StateFlow` + `Channel` for state management
- **Dagger Hilt** for dependency injection
- **Navigation3 1.1.1** (`navigation3-runtime` + `navigation3-ui`) for in-app navigation (developer-owned back stack)
- **Retrofit + OkHttp** for networking (TMDB API)
- **Coil** for image loading in Compose
- **Timber** (via `:libraries` module) for logging
- **kotlinx.serialization** for JSON parsing

**Root package**: `nl.abnamro.amrotv`

**SDK**: Min SDK: 26, Compile/Target SDK: 37

---

## Module Structure

```
amrotv/
├── app/                                  # Android Application entry point
├── core/
│   ├── mvi/
│   │   ├── android/                      # BaseAmroTvViewModel (needs androidx.lifecycle:viewmodel-ktx)
│   │   └── kotlin/                       # MviState/Intent/Effect, StateReducer, Mapper<I,O> — pure Kotlin
│   ├── domain/                           # Outcome<T> sealed class (shared across features)
│   ├── data/                             # NetworkModule: Retrofit/OkHttp setup, AuthInterceptor
│   ├── build-config/                     # BuildConfigProvider interface (decouples API token from modules)
│   ├── testing/                          # Robot DSL for E2E instrumented tests
│   └── ui/                              # AmroTheme, shared Compose components, @LightDarkPreview
├── libraries/
│   └── logger/
│       ├── api/                          # Logger interface (no Android deps — pure Kotlin)
│       └── implementation/               # TimberLogger + Hilt binding
└── feature/
    └── movies/
        ├── nav/                          # MoviesNavKey sealed interface, moviesEntry (Navigation3 wiring)
        ├── data/                         # TmdbApiService, DTOs, Mapper classes, MovieRepositoryImpl
        ├── domain/
        │   ├── api/                      # Domain models, MovieRepository interface, UseCase interfaces
        │   └── implementation/           # UseCase implementations
        ├── presentation/
        │   ├── api/                      # MVI State, Intent, Effect for each screen
        │   └── implementation/           # ViewModels + StateReducers
        └── ui/                           # Composable screens and components
```

### Gradle Module IDs (settings.gradle.kts)

```kotlin
include(":app")
include(":core:domain")
include(":core:testing")
include(":core:mvi:kotlin")
include(":core:mvi:android")
include(":core:data")
include(":core:build-config")
include(":core:ui")
include(":libraries:logger:api")
include(":libraries:logger:implementation")
include(":feature:movies:domain:api")
include(":feature:movies:domain:implementation")
include(":feature:movies:data")
include(":feature:movies:presentation:api")
include(":feature:movies:presentation:implementation")
include(":feature:movies:ui")
include(":feature:movies:nav")
```

> Always use type-safe project accessors (`projects.*`) for inter-module dependencies. Enable with `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` in `settings.gradle.kts`.

### Dependency Graph (simplified)

```
:app
  └── :feature:movies:nav          ← single entry point; :app has no other feature deps
        ├── :feature:movies:ui
        │     └── :feature:movies:presentation:api
        │           └── :feature:movies:domain:api
        ├── :feature:movies:presentation:implementation
        │     ├── :feature:movies:presentation:api
        │     ├── :feature:movies:domain:api
        │     └── :core:mvi:android
        ├── :feature:movies:data
        │     ├── :feature:movies:domain:api
        │     ├── :core:data
        │     ├── :core:mvi:kotlin
        │     └── :libraries:logger:api
        └── :feature:movies:domain:implementation
              └── :feature:movies:domain:api
:core:data
  └── :libraries:logger:api
:core:mvi:android
  └── :core:mvi:kotlin
```

---

## MVI Pattern

### The Three Interfaces (`:core:mvi:kotlin`)

```kotlin
interface MviState
interface MviIntent
interface MviEffect
```

### MviViewModel Base (`:core:mvi:android`)

> Read the canonical implementation: `core/mvi/android/src/main/kotlin/nl/abnamro/amrotv/core/mvi/BaseAmroTvViewModel.kt`
> Read the pure-Kotlin base: `core/mvi/kotlin/src/main/kotlin/nl/abnamro/amrotv/core/mvi/AmroTvViewModel.kt`

### State — `presentation:api`

```kotlin
// Requires: implementation(libs.kotlinx.collections.immutable)
data class {Screen}State(
    val isLoading: Boolean = false,
    val errors: ImmutableList<{Feature}Error> = persistentListOf(),
    // ... screen-specific fields
) : MviState
```

Rules:
- `isLoading`, `errors` are always present.
- State is a `data class`, never a sealed class.
- State holds **domain models** directly (not DTOs).
- Errors are `ImmutableList<{Feature}Error>` (empty = no errors). Multiple errors can coexist (e.g. movies + genres both failing). Never store `Exception` objects in state.
- **Never** put raw `Throwable.message` strings in state — they are unstable, potentially null, and too technical for UI. Use `{Feature}Error` enum values; the UI maps each entry to a string resource.

### Error enum — `presentation:api`

Each feature defines a typed error enum at the top level of `presentation:api`:

```kotlin
/**
 * Represents a categorised error that can occur in the {feature} feature.
 *
 * The UI layer maps each entry to an Android string resource, keeping hardcoded
 * strings out of the domain and presentation layers and enabling proper localisation.
 */
enum class {Feature}Error {
    /** Failed to load the primary data for this feature. */
    {THING}_LOAD_FAILED,
    // ... one entry per independent failure type
}
```

Rules:
- Declare it as a top-level `enum class` in `presentation:api` (not inside a screen sub-package).
- One entry per independent failure type (e.g. `MOVIES_LOAD_FAILED`, `GENRES_LOAD_FAILED`).
- The UI layer (`:ui`) maps each entry to a string resource via `when(error) { … }` — no strings ever enter the presentation layer.
- Raw `Throwable.message` stays exclusively in `logger.log(…)` calls.

### Intent — `presentation:api`

```kotlin
sealed interface {Screen}Intent : MviIntent {
    data object Load : {Screen}Intent
    data class FilterByGenre(val genreId: Int?) : {Screen}Intent
    data class OpenDetail(val movieId: Int) : {Screen}Intent
}
```

Rules:
- `sealed interface`, not `sealed class`.
- `data object` for intents with no payload; `data class` for intents with payload.
- Names are imperative verbs: `Load`, `Filter`, `Toggle`, `Open`, `Retry`.
- Only user intents belong here — **never** result types (`ContentLoaded`, `LoadFailed`).

### Effect — `presentation:api`

```kotlin
sealed interface {Screen}Effect : MviEffect {
    data class NavigateToMovieDetail(val movieId: Int) : {Screen}Effect
    data class OpenUrl(val url: String) : {Screen}Effect
}
```

Rules:
- Effects are **one-time events** (navigation, snackbar, URL opening).
- Collected with `LaunchedEffect(Unit)` in the screen composable via `viewModel.effects.collect`.
- Never re-send effects on recomposition.
- **Do not add a `ShowError` effect** — errors belong in `state.errors` (`ImmutableList<{Feature}Error>`) for persistent UI display. Effects are for transient, non-recoverable one-shots only.

### StateReducers — `presentation:implementation`

An injectable factory class whose methods each return a `StateReducer<{Screen}State>` lambda
for one named transition. All state logic lives here — the ViewModel is pure orchestration.

```kotlin
// {Screen}StateReducers.kt — in presentation:implementation (NOT in :api)
class {Screen}StateReducers @Inject constructor() {

    fun initialState(): {Screen}State = {Screen}State()

    fun loading(): StateReducer<{Screen}State> =
        StateReducer { it.copy(isLoading = true, errors = persistentListOf()) }

    fun contentLoaded(data: List<DomainModel>): StateReducer<{Screen}State> =
        StateReducer { it.copy(isLoading = false, data = data, errors = persistentListOf()) }

    fun loadFailed(errors: List<{Feature}Error>): StateReducer<{Screen}State> =
        StateReducer { it.copy(isLoading = false, errors = errors.toPersistentList()) }
}
```

Rules:
- Must be `public` — injected into a `@HiltViewModel` which is `public`.
- Each method is a pure function — no side effects.
- `initialState()` returns the screen's default state. It lives here (not in the ViewModel) so that if the initial state ever requires injected dependencies (e.g. a persisted user preference), they are added to `StateReducers`'s constructor without touching the ViewModel.

### ViewModel — `presentation:implementation`

```kotlin
@HiltViewModel
class {Screen}ViewModel @Inject constructor(
    private val getSomethingUseCase: GetSomethingUseCase,
    private val stateReducers: {Screen}StateReducers,
    private val logger: Logger,
) : BaseAmroTvViewModel<{Screen}State, {Screen}Intent, {Screen}Effect>(
    initialState = stateReducers.initialState(),
) {
    init { handleIntent({Screen}Intent.Load) }

    override fun handleIntent(intent: {Screen}Intent) {
        when (intent) {
            is {Screen}Intent.Load          -> {
                updateState { it.reduceWith(stateReducers.loading()) }
                loadData()
            }
            is {Screen}Intent.FilterByGenre -> {
                updateState { it.reduceWith(stateReducers.filterByGenre(intent.genreId)) }
                loadData()
            }
            is {Screen}Intent.OpenDetail    -> sendEffect({Screen}Effect.NavigateToMovieDetail(intent.movieId))
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            when (val result = getSomethingUseCase()) {
                is Outcome.Success -> updateState { it.reduceWith(stateReducers.contentLoaded(result.data)) }
                is Outcome.Error   -> {
                    logger.log(LogLevel.ERROR, TAG, "Failed to load: ${result.cause.message}", result.cause)
                    updateState { it.reduceWith(stateReducers.loadFailed(listOf({Feature}Error.{THING}_LOAD_FAILED))) }
                }
            }
        }
    }

    private companion object {
        const val TAG = "{Screen}ViewModel"
    }
}
```

Rules:
- Always `@HiltViewModel` + `@Inject constructor`.
- Extend `BaseAmroTvViewModel<S, I, E>` — **three** type params; pass `initialState`.
- Inject `{Screen}StateReducers` — all state transitions go through it.
- Apply transitions with `updateState { it.reduceWith(stateReducers.xxx()) }`.
- **Never** inline `copy()` calls in the ViewModel — all state logic belongs in `StateReducers`.
- Use `sendEffect(effect)` (direct call — not `viewModelScope.sendEffect`).
- Inject `Logger` — never use `Log.d` directly. Call `logger.log(LogLevel.LEVEL, TAG, msg, throwable)`.
- `Logger` has no shorthand methods (`d/e/w/i`) — always use `logger.log(LogLevel.X, ...)`.

---

## Clean Architecture Layers

| Layer | Can depend on | Cannot import |
|-------|--------------|---------------|
| `domain:api` | Kotlin stdlib + coroutines only | Android, Hilt, Retrofit, Room |
| `domain:implementation` | `domain:api`, Hilt | Android UI, Retrofit, Room |
| `data` | `domain:api`, `core:data`, `core:mvi:kotlin`, `libraries:logger:api`, Hilt | `presentation:*` |
| `presentation:api` | `domain:api`, `core:mvi:kotlin` | ViewModels, Android UI |
| `presentation:implementation` | `presentation:api`, `domain:api`, `core:mvi:android`, `core:mvi:kotlin`, `libraries:logger:api`, Hilt | Compose, NavController |
| `ui` | `presentation:api`, `core:ui`, Compose | `domain:*` directly; receives `AmroTvViewModel` as a constructor parameter — `hiltViewModel()` is called in `:feature:movies:nav` (entry builder), not in `:ui` |

Key rules:
- **DTOs never cross layer boundaries** — each data source maps its own DTOs to domain models internally.
- **Repository interface** in `domain:api`; implementation in `data`.
- **Use cases** are `fun interface` in `domain:api`; implementations in `domain:implementation`.
- Repository and use case interfaces use `suspend` functions returning `Outcome<T>`. `Outcome.Success` carries data; `Outcome.Error` wraps the cause and optionally carries stale cached data for resilience.

> For full data layer design (DataSource abstraction, DTOs, Room, mappers, repository), see `references/data-layer.md`.

---

## Hilt DI Conventions

| Use | When |
|-----|------|
| `@Module @InstallIn(SingletonComponent::class) object` | `@Provides` for third-party classes (Retrofit, OkHttp, Room) |
| `@Module @InstallIn(SingletonComponent::class) abstract class` | `@Binds` for infrastructure interfaces (repositories, data sources) |
| `@Module @InstallIn(ViewModelComponent::class) abstract class` | `@Binds` + `@ViewModelScoped` for domain use cases |

| Annotation | Scope |
|------------|-------|
| `@Singleton` | App-wide singleton |
| `@ViewModelScoped` | Lives with the ViewModel |
| No annotation | New instance each inject |

```kotlin
// ViewModel — always constructor injection
@HiltViewModel
class MyViewModel @Inject constructor(val dep: Dep) : MviViewModel<...>(...)

// Entrypoints
@AndroidEntryPoint class MainActivity : ComponentActivity()
@HiltAndroidApp class AmroApplication : Application()
```

---

## Navigation

**Library: Navigation3 1.1.1** — developer-owned back stack (`SnapshotStateList<Any>`).

Navigation keys are defined as a `@Serializable` sealed interface in `:feature:movies:nav`:

```kotlin
// feature/movies/nav/src/.../MoviesNavKey.kt
sealed interface MoviesNavKey {
    @Serializable data object TrendingMovies : MoviesNavKey
    @Serializable data class MovieDetail(val movieId: Int) : MoviesNavKey
}
```

> Read `app/src/main/kotlin/nl/abnamro/amrotv/AmroNavHost.kt` — the `NavDisplay` setup.
> Read `feature/movies/nav/src/main/kotlin/.../MoviesEntryBuilder.kt` — the `moviesEntry` function.

Rules:
- Nav keys (`MoviesNavKey`) live in `:feature:movies:nav`, not in `:app`.
- `:app` depends on `:feature:movies:nav` as its sole feature dependency.
- Navigate forward: `backStack.add(MoviesNavKey.MovieDetail(movieId))`.
- Navigate back: `backStack.removeLastOrNull()`.
- Screens receive navigation as **lambda parameters** — never import `NavDisplay` or touch `backStack`.
- `hiltViewModel()` works inside `NavEntry` via `rememberViewModelStoreNavEntryDecorator()`.

---

## Compose UI Conventions

### Screen structure

> Read `feature/movies/ui/src/main/kotlin/.../trendingmovies/TrendingMoviesScreen.kt`
> and `feature/movies/ui/src/main/kotlin/.../moviedetail/MovieDetailScreen.kt`
> for canonical examples of the Screen/Content split.

Rules:
- Always split into `Screen` (stateful) and `Content` (stateless + previewable).
- `Screen` parameters: navigation callbacks + ViewModel (default `hiltViewModel()`).
- Always use `collectAsStateWithLifecycle()` (not `collectAsState()`).
- Collect effects with `LaunchedEffect(Unit)` + `effects.collect`.
- Provide `@Preview` for every `Content` composable.
- Use AmroTheme design tokens — never hardcode colors/sizes/fonts.

### Image loading

```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data("https://image.tmdb.org/t/p/w500${movie.posterPath}")
        .crossfade(true)
        .build(),
    contentDescription = movie.title,
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxSize(),
)
```

---

## Mappers

All model conversions implement the `Mapper<I, O>` interface from `:core:mvi:kotlin`:

```kotlin
// core/mvi/kotlin/src/.../Mapper.kt
interface Mapper<I, O> {
    fun map(input: I): O
}
```

Rules:
- **Always implement `Mapper<I, O>`** — never use extension functions for data transformation.
- **Always use constructor injection** — mapper dependencies (e.g. a genre mapper inside a
  movie-detail mapper) are injected, not created inline.
- **Naming**: `{Model}{SourceLayer}To{TargetLayer}Mapper` — layer names are `Data`, `Domain`,
  `Presentation`.
- **Placement**: keep mappers in the same package as the types they map *from*.
  - `data` layer mappers (`*DataToDomainMapper`) live in `tmdb/mapper/`
  - `presentation` layer mappers (`*DomainToPresentationMapper`) live in `presentation/implementation/mapper/`

```kotlin
// Example — data layer mapper
internal class GenreDataToDomainMapper @Inject constructor() : Mapper<GenreDto, Genre> {
    override fun map(input: GenreDto) = Genre(id = input.id, name = input.name)
}

// Example — composite mapper (injects collaborator)
internal class MovieDetailDataToDomainMapper @Inject constructor(
    private val genreMapper: GenreDataToDomainMapper,
) : Mapper<MovieDetailDto, MovieDetail> {
    override fun map(input: MovieDetailDto) = MovieDetail(
        genres = input.genres.map { genreMapper.map(it) },
        // ...
    )
}
```

> Read existing mappers:
> - `feature/movies/data/src/.../tmdb/mapper/GenreDataToDomainMapper.kt`
> - `feature/movies/data/src/.../tmdb/mapper/MovieDataToDomainMapper.kt`
> - `feature/movies/data/src/.../tmdb/mapper/MovieDetailDataToDomainMapper.kt`
> - `feature/movies/presentation/implementation/src/.../mapper/MovieDomainToPresentationMapper.kt`

---

## Logging

```kotlin
// Always inject Logger — never call Timber or Log directly
class SomeClass @Inject constructor(private val logger: Logger) {
    fun doSomething() {
        logger.log(LogLevel.DEBUG, "SomeClass", "Doing something")
    }
}
```

`Logger` interface (`libraries:logger:api`) has a single method:
```kotlin
fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null)
```

`LogLevel` is an enum: `DEBUG`, `INFO`, `WARN`, `ERROR`.

> Read `libraries/logger/api/src/main/kotlin/nl/abnamro/amrotv/libraries/logger/api/Logger.kt`

---

## Naming Conventions

| Type | Convention | Example |
|------|-----------|---------|
| ViewModel | `{Screen}ViewModel` | `TrendingMoviesViewModel` |
| State | `{Screen}State` | `TrendingMoviesState` |
| Intent | `{Screen}Intent` | `TrendingMoviesIntent` |
| Effect | `{Screen}Effect` | `TrendingMoviesEffect` |
| Use case interface | `Get{Thing}UseCase` | `GetTrendingMoviesUseCase` |
| Use case impl | `Get{Thing}UseCaseImpl` | `GetTrendingMoviesUseCaseImpl` |
| Repository interface | `{Thing}Repository` | `MovieRepository` |
| Repository impl | `{Thing}RepositoryImpl` | `MovieRepositoryImpl` |
| Mapper | `{Model}{SourceLayer}To{TargetLayer}Mapper` | `GenreDataToDomainMapper`, `MovieDomainToPresentationMapper` |
| DTO | `{Thing}Dto` | `MovieDto`, `TrendingMoviesResponseDto` |
| Hilt module | `{Layer}Module` | `DataModule`, `DomainModule`, `NetworkModule` |
| Screen composable | `{Screen}Screen` | `TrendingMoviesScreen` |
| Content composable | `{Screen}Content` | `TrendingMoviesContent` |
| Impl classes | suffix `Impl`, no `public` modifier | `MovieRepositoryImpl` |

---

## KDoc Conventions

KDoc is **required** on interfaces, abstract classes, and domain model properties with non-obvious semantics. Implementations skip KDoc — they inherit documentation from their interface.

### What requires KDoc

| Element | Required? | Notes |
|---------|-----------|-------|
| `interface` — class-level | ✅ Always | Describe purpose, not implementation |
| `interface` members | ✅ Always | `@param`, `@return`, `@throws` where applicable |
| `abstract class` — class-level | ✅ Always | e.g. `MviViewModel`, data source bases |
| `abstract` members | ✅ Always | |
| Domain `data class` — non-obvious fields | ✅ Property KDoc | e.g. `budget: Long?` — why it can be null |
| Sealed interface hierarchy | ✅ Class-level only | Describe the sealed family |
| Implementation class | ❌ Skip | Inherits from interface |
| Private / internal | ❌ Skip | |
| Test code | ❌ Skip | Test names are self-documenting |

### Format

```kotlin
/**
 * One-sentence summary (no trailing period).
 *
 * Optional extended description. Reference other types with [OtherClass].
 *
 * @param name Description starting with lowercase.
 * @return Description of the return value.
 * @throws IOException When/why this is thrown.
 * @see RelatedClass
 */
```

### Examples

> Read real KDoc examples in:
> - `core/mvi/android/src/main/kotlin/nl/abnamro/amrotv/core/mvi/BaseAmroTvViewModel.kt` — abstract class with type-param docs
> - `core/mvi/kotlin/src/main/kotlin/nl/abnamro/amrotv/core/mvi/StateReducer.kt` — interface with member KDoc
> - `core/domain/src/main/kotlin/nl/abnamro/amrotv/core/domain/model/Outcome.kt` — sealed class with property docs

### Anti-patterns

| ❌ Don't | ✅ Do |
|----------|-------|
| KDoc on an `Impl` class restating the interface | Write KDoc on the interface only |
| `/** Gets the movies. */` — just restating the name | Explain *why*, *constraints*, or *non-obvious semantics* |
| Skip `@param`/`@return` on interface methods | Document all params and return values on interfaces |
| KDoc on test classes or test methods | Skip — test names are self-documenting |

---

## Reference Files

Load these when the task touches the relevant area:

| File | When to load |
|------|-------------|
| `references/data-layer.md` | Building or modifying the data layer: DTOs, DataSources, Room, mappers, repository |
| `references/networking.md` | TMDB endpoints, AuthInterceptor, NetworkResult, API token setup, image URLs |
| `references/build-templates.md` | Creating new modules or writing/modifying `build.gradle.kts` files |
| `testing` skill | Writing unit, integration, or E2E instrumented tests (JUnit 5/4, MockK, Turbine, MockWebServer, typed Robot Pattern) |
