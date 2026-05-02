---
name: testing
description: >
  Write, refactor, or review tests for AMRO — use cases, repositories, ViewModels, data
  layer components, and UI screens. Load this skill whenever you are creating or updating
  any file under src/test/ or src/androidTest/, writing JUnit 5 tests with MockK, structuring
  GIVEN/WHEN/THEN test cases with @Nested and @DisplayName, setting up @MockK/@InjectMockKs
  annotations, building mock data fixture objects, stubbing suspend functions with coEvery,
  using runTest for coroutine testing, deciding when to use Turbine vs direct assertion,
  or implementing E2E instrumented tests with Compose testing, Hilt, MockWebServer, and
  the typed Robot Pattern. When in doubt about any test pattern in this project, load this
  skill first.
---

# AMRO Testing Conventions

## Framework stack

| Concern | Tool |
|---------|------|
| Test runner (unit tests) | JUnit 5 (Jupiter) — `tasks.withType<Test> { useJUnitPlatform() }` in `build.gradle.kts` |
| Test runner (E2E / instrumented tests) | JUnit 4 — `@RunWith(AndroidJUnit4::class)` + `@HiltAndroidTest`; the `de.mannodermaus.android-junit5` plugin is present in the module but E2E test classes use JUnit 4 |
| Mocking | MockK (`io.mockk:mockk`) |
| Coroutines | `kotlinx.coroutines.test` — `runTest` |
| Flow/StateFlow assertions | Turbine — only for Flow-returning code |
| Test discovery | `testRuntimeOnly(libs.junit.platform.launcher)` — needed alongside `useJUnitPlatform()` |

> **Rule:** local unit tests (domain, data, presentation layers) use `useJUnitPlatform()`. The `de.mannodermaus.android-junit5` plugin is only needed when writing instrumented tests (`androidTest`) that must run on a device/emulator.

---

## Test structure: GIVEN / WHEN / THEN

Every test class is organized as three levels of `@Nested` inner classes annotated with `@DisplayName`. This makes test reports read like specifications and keeps related setups together.

```kotlin
class {Subject}Test {

    // 1. Mock fields — declared on the outer class
    @MockK lateinit var {dependency}: {DependencyType}

    // 2. SUT declared as the interface — tests verify the contract, not the impl class
    private lateinit var {sut}: {UseCaseInterface}

    // 3. Outer setUp — initialises mocks, then builds the impl manually
    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        {sut} = {SubjectClass}({dependency})
    }

    // 4. GIVEN — one @Nested per distinct input data / precondition state
    @Nested
    @DisplayName("GIVEN {describe the precondition or available data}")
    inner class Given{Precondition} {

        // 5. Shared stub for this GIVEN block
        @BeforeEach
        fun setUp() {
            coEvery { {dependency}.{method}() } returns {stub}
        }

        // 6. WHEN — one @Nested per SPECIFIC invocation (specific parameters)
        @Nested
        @DisplayName("WHEN invoked with {specific parameters}")
        inner class WhenInvokedWith{SpecificParameters} {

            // 7. THEN — one @Test per distinct observable outcome of this invocation
            @Test
            @DisplayName("THEN {describe the expected result}")
            fun {camelCaseDescription}() = runTest {
                val result = {sut}({args})
                assertEquals(expected, result)
            }
        }
    }
}
```

### Semantics of each level

| Level | Meaning | Groups by |
|-------|---------|-----------|
| **GIVEN** | Input data / precondition state | Repository stubs, fixtures, test context |
| **WHEN** | **One specific invocation** with a specific set of parameters | Exact method call + exact arguments |
| **THEN** | One `@Test` per distinct observable outcome of that invocation | Different concerns (return value, side effect, error fields) |

**GIVEN** describes the context set up once for a group of tests via `@BeforeEach`. A GIVEN block is **required for every test** — even happy-path tests need a GIVEN to affirm the precondition (e.g. `GIVEN valid movie details are available`). Example: `GIVEN a list of movies returned by the repository`.

**Key GIVEN rules:**
- **Always present**: Every test must be nested inside a GIVEN. There is no such thing as a "top-level WHEN" without a GIVEN.
- **Merge when the context is the same**: If several WHEN blocks share the same preconditions (e.g. use case returns success AND a valid IMDB ID is present AND the screen is open), collapse them into one GIVEN block. Multiple WHENs under a single GIVEN is the preferred shape. Example: `GIVEN the use case returns valid movie details` → `WHEN the screen loads` + `WHEN the user taps IMDB` + `WHEN the user navigates back`.
- **Keep separate when the context genuinely differs**: error vs. success, or a specific sub-state (e.g. sort sheet open) that is not shared.

**WHEN** encodes the exact call being made with a description of what the parameters *represent* in domain terms. Each unique combination that produces a different outcome gets its own WHEN class.

**Display name rules:**
- Use **business / domain language** — describe what the user does or what the system shows, never API or intent class names. Bad: `"WHEN FilterByGenre is sent"`. Good: `"WHEN the user selects an action genre as a filter"`.
- **No raw numbers or IDs** — replace literals with what they represent. Bad: `"WHEN the screen requests detail for movie id = 42"`. Good: `"WHEN the screen requests detail for a valid movie"`. Bad: `"WHEN the user selects genre 28 (Action)"`. Good: `"WHEN the user selects an action genre"`.
- Enum names and directions are fine: `"popularity sort DESC"`, `"ascending sort order"`.

Do NOT use category names like "WHEN filtering by genre" that would group tests with different parameters — each unique invocation still gets its own WHEN.

**THEN** is where the observable outcomes live. Use multiple `@Test` methods under the same WHEN only when the same invocation produces **multiple distinct concerns that cannot be inferred from each other**:
- error cause AND error data (two independent fields of `Outcome.Error`)
- data transformation AND a side effect with no observable impact on the return value (e.g., logging, analytics)

**Do NOT** add a `coVerify` delegation check as a separate THEN test when the return value is already being asserted — if the outcome proves the call happened, the verification is redundant noise. Reserve `coVerify` for fire-and-forget side effects (e.g. a `Unit`-returning logger call) where there is no return value to assert on.

If the invocation has only **one observable concern**, use a single `@Test` directly under WHEN (no additional nesting needed).

### Why this structure

- **GIVEN** sets up state once via `@BeforeEach`, removing the repetitive `coEvery { ... }` from every test body.
- **WHEN** = one specific call site → failures pinpoint which exact input combination broke.
- **THEN** = one concern per test → one failure = one clear reason.
- `@DisplayName` on every level produces readable JUnit output:
  `GIVEN repo returns movies > WHEN invoked with action genre filter and popularity ASC > THEN only action movies are returned`

### Example: multiple THEN tests under the same WHEN

Use multiple THEN tests when different observable concerns of the same invocation are independently meaningful — like both fields of an `Outcome.Error`:

```kotlin
@Nested
@DisplayName("WHEN the use case is invoked")
inner class WhenInvoked {

    @Test
    @DisplayName("THEN the error cause is propagated")
    fun errorCausePropagated() = runTest {
        val error = useCase(null, SortOption.POPULARITY, SortOrder.DESC).requireError()
        assertEquals(networkError, error.cause)
    }

    @Test
    @DisplayName("THEN the data is null")
    fun dataIsNull() = runTest {
        val error = useCase(null, SortOption.POPULARITY, SortOrder.DESC).requireError()
        assertNull(error.data)
    }
}
```

> Each THEN `@Test` re-invokes the subject independently. The shared `@BeforeEach` stub in GIVEN/WHEN guarantees the same response each time.

---

## MockK annotations

### Imports (MockK 1.13+)

```kotlin
import io.mockk.impl.annotations.MockK
import io.mockk.MockKAnnotations
```

> `@MockK` lives in `io.mockk.impl.annotations`, not `io.mockk`.

### Pattern

```kotlin
@MockK lateinit var repository: MovieRepository

private lateinit var useCase: GetTrendingMoviesUseCase

@BeforeEach
fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    useCase = GetTrendingMoviesUseCaseImpl(repository)
}
```

Declare the SUT as the **interface** type — this ensures the test verifies the contract, not just the implementation class. Build the concrete impl manually in `setUp()` after `MockKAnnotations.init()` so the mocks are ready when the constructor runs.

`relaxed = true` means un-stubbed calls return safe defaults instead of throwing, so `Logger` and other side-effect deps need no explicit stub.

### Other annotations

| Annotation | Use case |
|------------|----------|
| `@MockK` | Interface/abstract class dep you will stub |
| `@RelaxedMockK` | Dep that needs no explicit stubs (auto-returns safe defaults; calls are still verifiable) |
| `@SpyK` | Partial mock of a concrete class |
| `@InjectMockKs` | Auto-inject declared mocks into a concrete class by constructor type — avoid; prefer manual construction with interface typing (see above) |

---

## Stub ordering (GIVEN → shared defaults, WHEN → overrides)

Put the most common stub in the GIVEN `@BeforeEach` so every test in that block gets it for free:

```kotlin
@Nested
@DisplayName("GIVEN repository returns a successful movie list")
inner class GivenRepositoryReturnsSuccess {

    @BeforeEach
    fun setUp() {
        // Default stub — covers all WHEN blocks below
        coEvery { repository.getTrendingMovies() } returns Outcome.Success(MovieDomainMocks.Movies.all)
    }

    @Nested
    @DisplayName("WHEN invoked with no genre filter and popularity sort DESC")
    inner class WhenInvokedWithNoFilterAndPopularitySortDesc {
        // No setUp needed — default stub from GIVEN is used
        @Test
        @DisplayName("THEN movies are sorted most to least popular")
        fun moviesSortedMostToLeastPopular() = runTest { ... }
    }
}
```

Override the stub in a specific WHEN `@BeforeEach` only when that group of tests needs a different return value.

---

## Mock data fixtures

Reusable test data lives in a dedicated `object` in the test source set, not scattered across test classes. One file per domain area:

```
src/test/kotlin/.../
├── {Feature}DomainMocks.kt   ← reusable objects
├── {Subject}Test.kt
└── {OtherSubject}Test.kt
```

Structure the mock object with nested objects by model type:

```kotlin
// {Feature}DomainMocks.kt
object {Feature}DomainMocks {

    object {ModelType}s {
        const val {CONSTANT_FIELD} = ...
        val {instance1} = {Model}(id = 1, ...)
        val {instance2} = {Model}(id = 2, ...)
        val all = listOf({instance1}, {instance2})
    }

    object {OtherModelType}s {
        val {instance} = {OtherModel}(...)
        val all = listOf(...)
    }

    object {FactoryModelType}s {
        // Use a factory when the model needs a variable id or name
        fun of(id: Int = 42) = {FactoryModel}(id = id, ...)
    }
}
```

Use these in tests with named imports for clarity:

```kotlin
import nl.abnamro.amrotv.feature.movies.domain.implementation.usecase.MovieDomainMocks.Movies

coEvery { repository.getTrendingMovies() } returns Outcome.Success(Movies.all)
```

---

## Coroutine testing: suspend fun vs Flow

### `suspend fun` — use `runTest` + direct assertion (no Turbine)

When the function under test is `suspend`, call it directly inside `runTest`:

```kotlin
@Test
fun returnsMovieDetail() = runTest {
    coEvery { repository.getMovieDetail(42) } returns Outcome.Success(expectedDetail)

    val result = useCase(42)

    assertEquals(Outcome.Success(expectedDetail), result)
}
```

### Asserting `Outcome<T>` subtypes — use private test helpers

When a test needs to access the inner data of an `Outcome.Success` or `Outcome.Error`, **do not cast directly with `as`** — a `ClassCastException` is hard to diagnose. Instead, define private helpers at the bottom of the test file that use a `when` expression on the sealed class. The exhaustive `when` gives Kotlin a proper smart-cast (preserving the generic type parameter), and throws a descriptive `IllegalStateException` on mismatch:

```kotlin
private fun Outcome<List<Movie>>.requireSuccess(): List<Movie> = when (this) {
    is Outcome.Success -> data
    is Outcome.Error -> error("Expected Outcome.Success but got Outcome.Error: $cause")
}

private fun Outcome<List<Movie>>.requireError(): Outcome.Error<List<Movie>> = when (this) {
    is Outcome.Error -> this
    is Outcome.Success -> error("Expected Outcome.Error but got Outcome.Success")
}
```

Usage in tests:

```kotlin
val data = useCase(null, SortOption.POPULARITY, SortOrder.DESC).requireSuccess()
assertEquals(Movies.all.size, data.size)

val error = useCase(null, SortOption.POPULARITY, SortOrder.DESC).requireError()
assertEquals(networkError, error.cause)
assertNull(error.data)
```

> Scope these helpers narrowly — define them per return type in the file that needs them (e.g., `Outcome<List<Movie>>`). Do not make them generic top-level helpers since the type parameter is necessary for correct smart-casting.

### Flow-returning code — use Turbine

When the function returns `Flow<T>` (ViewModels, StateFlow), use Turbine:

```kotlin
@Test
fun emitsSuccessState() = runTest {
    viewModel.state.test {
        assertTrue(awaitItem().isLoading)
        assertFalse(awaitItem().isLoading)
        cancelAndIgnoreRemainingEvents()
    }
}
```

The domain layer (use cases, repository) uses `suspend fun ... : Outcome<T>` — **no Turbine needed there**. Turbine is reserved for the presentation layer (ViewModels with `StateFlow` / `effects: Flow<E>`).

### MockK coroutine matchers

| Scenario | Use |
|----------|-----|
| Stub a `suspend fun` | `coEvery { ... } returns value` |
| Verify a fire-and-forget `suspend fun` (no return value to assert) | `coVerify(exactly = N) { ... }` |
| Stub a regular fun or Flow-returning fun | `every { ... } returns value` |
| Verify a regular fun | `verify(exactly = N) { ... }` |

Never use `every` / `verify` for `suspend fun` — it will compile but won't intercept coroutine calls correctly.

> When a `suspend fun` returns a meaningful value, asserting that value is sufficient — `coVerify` is redundant. Use `coVerify` only for side effects with no observable return value (e.g. a logger call, an analytics event).

---

## JUnit 5 lifecycle with @Nested

JUnit 5 runs `@BeforeEach` methods from outer to inner. A new test instance is created per test by default (PER_METHOD). This means:

1. Outer `setUp()` runs → mocks initialized via `MockKAnnotations.init()`  
2. GIVEN `setUp()` runs → stubs set on the fresh mocks  
3. Test method runs  

`inner class` gives each nested class access to the outer instance's `@MockK` fields — no need to pass mocks down.

---

## Advanced patterns (ViewModel tests, Fake repositories, E2E instrumented tests)

For ViewModel tests, fake repository patterns, and E2E Compose instrumented tests with
MockWebServer and the typed Robot Pattern, read:

`references/advanced-patterns.md`

Load it when writing tests for the presentation layer or `androidTest` instrumented tests.

---

## Canonical examples

The following files are the reference implementation of all these conventions:

- `feature/movies/domain/implementation/src/test/.../MovieDomainMocks.kt` — mock data fixture object
- `feature/movies/domain/implementation/src/test/.../GetTrendingMoviesUseCaseImplTest.kt` — full GIVEN/WHEN/THEN with multiple preconditions, filters, sorts, and error scenarios
- `feature/movies/domain/implementation/src/test/.../GetMovieDetailUseCaseImplTest.kt` — minimal delegation test pattern
- `feature/movies/domain/implementation/src/test/.../GetGenresUseCaseImplTest.kt` — minimal delegation test pattern
- `feature/movies/ui/src/androidTest/.../MoviesFlowE2ETest.kt` — E2E test class (JUnit 4 + Hilt + MockWebServer + Robot DSL)
- `feature/movies/ui/src/androidTest/.../mock/MoviesMockDispatcher.kt` — MockWebServer dispatcher with concrete path routing
- `feature/movies/ui/src/androidTest/.../robots/TrendingMoviesRobot.kt` — typed Robot with scoped action/verification interfaces
