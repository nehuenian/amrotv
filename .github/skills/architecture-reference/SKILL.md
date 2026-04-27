---
name: architecture-reference
description: >
  AMRO app architecture reference — Android + MVI + Hilt + Jetpack Compose patterns,
  module structure, naming conventions, and code templates.
  Auto-load this skill whenever working on any AMRO code: writing a feature, reviewing Kotlin
  files, fixing a bug, creating a screen, wiring navigation, checking DI setup, or any task
  that touches the project's Kotlin source. When in doubt, load this skill first. If anything
  touches modules, build files, data layer, networking, or tests, also check the reference files
  listed at the bottom.
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
include(":core:mvi")
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
        └── :core:mvi
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

### The Three Interfaces (`:core:mvi`)

```kotlin
interface MviState
interface MviIntent
interface MviEffect
```

### MviViewModel Base (`:core:mvi`)

```kotlin
abstract class MviViewModel<S : MviState, I : MviIntent, E : MviEffect>(
    initialState: S
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = Channel<E>(Channel.BUFFERED)
    val effects: Flow<E> = _effects.receiveAsFlow()

    abstract fun handleIntent(intent: I)

    protected fun updateState(reducer: S.() -> S) {
        _state.update { it.reducer() }
    }

    protected fun CoroutineScope.sendEffect(effect: E) {
        launch { _effects.send(effect) }
    }
}
```

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
| `presentation:api` | `domain:api`, `core:mvi` | ViewModels, Android UI |
| `presentation:implementation` | `presentation:api`, `domain:api`, `core:mvi`, `libraries:logger:api`, Hilt | Compose, NavController |
| `ui` | `presentation:api`, `core:ui`, Compose | `domain:*` directly; use `hiltViewModel()` |

Key rules:
- **DTOs never cross layer boundaries** — each data source maps its own DTOs to domain models internally.
- **Repository interface** in `domain:api`; implementation in `data`.
- **Use cases** are `fun interface` in `domain:api`; implementations in `domain:implementation`.
- Repository and use case interfaces return `Flow<T>` (never `suspend` + return). Errors propagate via Flow exceptions.

> For full data layer design (DataSource abstraction, DTOs, Room, mappers, repository), see `references/data-layer.md`.

---

## Hilt DI Conventions

| Use | When |
|-----|------|
| `@Module @InstallIn(SingletonComponent::class) object` | `@Provides` for third-party classes (Retrofit, OkHttp, Room) |
| `@Module @InstallIn(SingletonComponent::class) abstract class` | `@Binds` to bind interfaces to implementations |

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

@Composable
fun AmroNavHost() {
    val backStack = rememberNavBackStack(TrendingMovies)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            addEntryProvider<TrendingMovies> { key ->
                NavEntry(key) {
                    TrendingMoviesScreen(
                        onNavigateToDetail = { id -> backStack.add(MovieDetail(id)) }
                    )
                }
            }
            addEntryProvider<MovieDetail> { key ->
                NavEntry(key) {
                    MovieDetailScreen(movieId = key.movieId)
                }
            }
        }
    )
}
```

Rules:
- Routes: `@Serializable data object` or `data class` in `:app`.
- Navigate forward: `backStack.add(SomeRoute(...))`.
- Navigate back: `backStack.removeLastOrNull()`.
- Screens receive navigation as **lambda parameters** — never import `NavDisplay` or touch `backStack`.
- `hiltViewModel()` works inside `NavEntry` via `rememberViewModelStoreNavEntryDecorator()`.

---

## Compose UI Conventions

### Screen structure

```kotlin
// Screen = stateful entry point (gets ViewModel + collects state)
@Composable
fun TrendingMoviesScreen(
    onNavigateToDetail: (movieId: Int) -> Unit,
    viewModel: TrendingMoviesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TrendingMoviesEffect.NavigateToMovieDetail -> onNavigateToDetail(effect.movieId)
            }
        }
    }

    TrendingMoviesContent(state = state, onIntent = viewModel::handleIntent)
}

// Content = stateless, testable, previewable
@Composable
fun TrendingMoviesContent(
    state: TrendingMoviesState,
    onIntent: (TrendingMoviesIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // UI here — no ViewModel, no NavController, no direct data fetching
}

@Preview(showBackground = true)
@Composable
private fun TrendingMoviesContentPreview() {
    AmroTheme { TrendingMoviesContent(state = TrendingMoviesState(), onIntent = {}) }
}
```

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

```kotlin
// ✅ Interface — class-level + every member documented
/**
 * Abstracts the remote data source for movie data.
 *
 * Any new remote API (OMDB, JustWatch, etc.) must implement this interface.
 * Implementations map their own DTOs to domain models internally — no DTOs cross this boundary.
 */
interface RemoteMovieDataSource {

    /**
     * Fetches a single page of trending movies.
     *
     * @param page 1-based page index.
     * @return list of [Movie] domain models for the requested page.
     * @throws IOException on network failure.
     */
    suspend fun getTrendingMovies(page: Int): List<Movie>

    /**
     * Fetches full detail for a single movie.
     *
     * @param id TMDB movie ID.
     * @return [MovieDetail] with all fields populated.
     */
    suspend fun getMovieDetail(id: Int): MovieDetail

    /**
     * Fetches the complete list of TMDB movie genres.
     *
     * @return list of [Genre] objects, stable for the lifetime of the app session.
     */
    suspend fun getGenres(): List<Genre>
}

// ✅ Abstract base — class + type params documented
/**
 * Base ViewModel for all MVI screens.
 *
 * Exposes [state] as a [StateFlow] and [effects] as a one-shot [Flow].
 * All user actions are routed through [handleIntent].
 *
 * @param S State type — must be a [MviState] data class.
 * @param I Intent type — must be a [MviIntent] sealed interface.
 * @param E Effect type — must be a [MviEffect] sealed interface.
 * @param initialState The initial value emitted on [state].
 */
abstract class MviViewModel<S : MviState, I : MviIntent, E : MviEffect>(
    initialState: S,
) : ViewModel() { ... }

// ✅ Domain model — property KDoc only on non-obvious fields
/**
 * Full movie detail as returned by the domain layer.
 */
data class MovieDetail(
    val id: Int,
    val title: String,
    val tagline: String?,
    /** Null when TMDB does not report the budget (API returns 0, mapper converts to null). */
    val budget: Long?,
    /** Null when TMDB does not report the revenue (API returns 0, mapper converts to null). */
    val revenue: Long?,
    /**
     * Raw TMDB IMDb ID (e.g. `"tt0137523"`).
     * Construct the full link as `"https://www.imdb.com/title/$imdbId/"`.
     */
    val imdbId: String?,
)
```

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
| `references/testing.md` | Writing unit, integration, or UI tests (JUnit 5, Turbine, Robot Pattern) |
| `references/build-templates.md` | Creating new modules or writing/modifying `build.gradle.kts` files |
