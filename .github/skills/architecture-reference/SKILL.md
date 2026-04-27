---
name: architecture-reference
description: >
  AMRO app architecture reference — Android + MVI + Hilt + Jetpack Compose patterns,
  module structure, naming conventions, and code templates.
  Auto-load this skill whenever working on any AMRO code: writing a feature, reviewing Kotlin
  files, fixing a bug, creating a screen, wiring navigation, checking DI setup, or any task
  that touches the project's Kotlin source. When in doubt, load this skill first. If anything
  touches modules, build files, data layer, or networking, also check the reference files
  listed at the bottom. For unit testing conventions, load the `unit-testing` skill.
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

**Min SDK**: 24 | **Target SDK**: 36

---

## Module Structure

```
amrotv/
├── app/                                  # Android Application entry point
├── core/
│   ├── mvi/                              # MVI base classes (MviViewModel, interfaces) — Android library (needs androidx.lifecycle:viewmodel-ktx)
│   ├── network/                          # Retrofit/OkHttp setup, AuthInterceptor, NetworkResult
│   └── ui/                              # AmroTheme, shared Compose components
├── libraries/
│   └── logger/
│       ├── api/                          # Logger interface (no Android deps — pure Kotlin)
│       └── implementation/               # TimberLogger + Hilt binding
└── feature/
    └── movies/
        ├── data/                         # TmdbApiService, DTOs, mappers, MovieRepositoryImpl
        ├── domain/
        │   ├── api/                      # Domain models, MovieRepository interface, UseCase interfaces
        │   └── implementation/           # UseCase implementations
        └── presentation/
            ├── api/                      # MVI State, Intent, Effect for each screen
            ├── implementation/           # ViewModels
            └── ui/                       # Composable screens and components
```

### Gradle Module IDs (settings.gradle.kts)

```kotlin
include(":app")
include(":core:mvi:kotlin")
include(":core:mvi:android")
include(":core:network")
include(":core:ui")
include(":libraries:logger:api")
include(":libraries:logger:implementation")
include(":feature:movies:domain:api")
include(":feature:movies:domain:implementation")
include(":feature:movies:data")
include(":feature:movies:presentation:api")
include(":feature:movies:presentation:implementation")
include(":feature:movies:ui")
```

> Always use type-safe project accessors (`projects.*`) for inter-module dependencies. Enable with `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` in `settings.gradle.kts`.

### Dependency Graph (simplified)

```
:app
  └── :feature:movies:ui
        └── :feature:movies:presentation:api
              └── :feature:movies:domain:api
  └── :feature:movies:presentation:implementation
        ├── :feature:movies:presentation:api
        ├── :feature:movies:domain:api
        └── :core:mvi:android
  └── :feature:movies:data
        ├── :feature:movies:domain:api
        ├── :core:network
        └── :libraries:logger:api
  └── :feature:movies:domain:implementation
        └── :feature:movies:domain:api
  └── :core:ui
  └── :libraries:logger:implementation
        └── :libraries:logger:api
:core:network
  └── :libraries:logger:api
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
data class {Screen}State(
    val isLoading: Boolean = false,
    val error: String? = null,
    // ... screen-specific fields
) : MviState
```

Rules:
- `isLoading`, `error` are always present.
- State is a `data class`, never a sealed class.
- State holds **domain models** directly (not DTOs).
- Error is `String?` (null = no error). Never store `Exception` objects in state.

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

### Effect — `presentation:api`

```kotlin
sealed interface {Screen}Effect : MviEffect {
    data class NavigateToMovieDetail(val movieId: Int) : {Screen}Effect
    data class ShowError(val message: String) : {Screen}Effect
}
```

Rules:
- Effects are **one-time events** (navigation, snackbar, URL opening).
- Collected with `LaunchedEffect(Unit)` in the screen composable via `viewModel.effects.collect`.
- Never re-send effects on recomposition.

### ViewModel — `presentation:implementation`

```kotlin
@HiltViewModel
class {Screen}ViewModel @Inject constructor(
    private val getSomethingUseCase: GetSomethingUseCase,
    private val logger: Logger,
) : MviViewModel<{Screen}State, {Screen}Intent, {Screen}Effect>(
    initialState = {Screen}State()
) {
    init { handleIntent({Screen}Intent.Load) }

    override fun handleIntent(intent: {Screen}Intent) {
        when (intent) {
            is {Screen}Intent.Load -> loadData()
            is {Screen}Intent.FilterByGenre -> updateState { copy(selectedGenreId = intent.genreId) }
            is {Screen}Intent.OpenDetail -> viewModelScope.sendEffect(
                {Screen}Effect.NavigateToMovieDetail(intent.movieId)
            )
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            getSomethingUseCase()
                .catch { e ->
                    logger.e("TAG", "Failed to load", e)
                    updateState { copy(isLoading = false, error = e.message) }
                }
                .collect { data ->
                    updateState { copy(isLoading = false, data = data) }
                }
        }
    }
}
```

Rules:
- Always `@HiltViewModel` + `@Inject constructor`.
- Use `viewModelScope.launch` for coroutine work.
- Reset `isLoading = false` in both success and error paths.
- Reset `error = null` when retrying.
- Use `catch {}` on flows for error handling.
- Inject `Logger` — never use `Log.d` directly.

---

## Clean Architecture Layers

| Layer | Can depend on | Cannot import |
|-------|--------------|---------------|
| `domain:api` | Kotlin stdlib + coroutines only | Android, Hilt, Retrofit, Room |
| `domain:implementation` | `domain:api`, Hilt | Android UI, Retrofit, Room |
| `data` | `domain:api`, `core:network`, `libraries:logger:api`, Hilt | `presentation:*` |
| `presentation:api` | `domain:api`, `core:mvi:kotlin` | ViewModels, Android UI |
| `presentation:implementation` | `presentation:api`, `domain:api`, `core:mvi:android`, `libraries:logger:api`, Hilt | Compose, NavController |
| `ui` | `presentation:api`, `core:ui`, Compose | `domain:*` directly; use `hiltViewModel()` |

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

```kotlin
@Serializable data object TrendingMovies
@Serializable data class MovieDetail(val movieId: Int)
```

> `AmroNavHost.kt` does not exist yet — it will be created in Commit 11. Until then, use the navigation rules below as the source of truth.

Rules:
- Routes: `@Serializable data object` or `data class` in `:app`.
- Navigate forward: `backStack.add(SomeRoute(...))`.
- Navigate back: `backStack.removeLastOrNull()`.
- Screens receive navigation as **lambda parameters** — never import `NavDisplay` or touch `backStack`.
- `hiltViewModel()` works inside `NavEntry` via `rememberViewModelStoreNavEntryDecorator()`.

---

## Compose UI Conventions

### Screen structure

> `TrendingMoviesScreen.kt` does not exist yet — it will be created in Commit 10. Until then, use the screen rules below as the source of truth.

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

## Logging

```kotlin
// Always inject Logger — never call Timber or Log directly
class SomeClass @Inject constructor(private val logger: Logger) {
    fun doSomething() {
        logger.d("SomeClass", "Doing something")
    }
}
```

`Logger` interface (`libraries:api`): `fun d/i/w/e(tag: String, message: String, throwable: Throwable? = null)`

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
| API service | `{Source}ApiService` | `TmdbApiService` |
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
> - `core/network/src/main/kotlin/nl/abnamro/amrotv/core/network/NetworkResult.kt` — sealed class with property docs

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
| `unit-testing` skill | Writing unit, integration, or UI tests (JUnit 5, MockK, Turbine, Robot Pattern) |
