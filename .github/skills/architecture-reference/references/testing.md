# Testing Conventions

## Table of Contents
1. [Framework Overview](#framework-overview)
2. [JUnit 5 — Nested Tests (Use Case)](#junit-5--nested-tests-use-case)
3. [ViewModel Tests — JUnit 5 + Turbine](#viewmodel-tests--junit-5--turbine)
4. [Integration Tests — Data Layer](#integration-tests--data-layer)
5. [Integration Tests — Presentation Layer](#integration-tests--presentation-layer)
6. [UI Tests — Espresso + Robot Pattern](#ui-tests--espresso--robot-pattern)
7. [Test Utilities](#test-utilities)

---

## Framework Overview

| Framework | Purpose |
|-----------|---------|
| **JUnit 5** (Jupiter) | All unit tests — enabled via `de.mannodermaus.android-junit5` Gradle plugin |
| **MockK** | Mocking and stubbing Kotlin classes/interfaces |
| **Turbine** | Asserting `StateFlow` / `Flow` emissions |
| **MockWebServer** (OkHttp) | HTTP-level data layer integration tests |
| **Espresso + Compose UI Test** (`src/androidTest/`) | UI tests with Robot Pattern |

---

## JUnit 5 — Nested Tests (Use Case)

Use `@Nested` inner classes to group related test cases:

```kotlin
// src/test/kotlin/.../GetTrendingMoviesUseCaseImplTest.kt
class GetTrendingMoviesUseCaseImplTest {

    private val repository: MovieRepository = mockk()
    private val useCase = GetTrendingMoviesUseCaseImpl(repository)

    @Nested
    inner class WhenNoFilterApplied {
        @Test
        fun `returns all movies sorted by popularity descending by default`() = runTest {
            every { repository.getTrendingMovies() } returns flowOf(unsortedMovies)
            useCase().test {
                assertEquals(moviesSortedByPopularityDesc, awaitItem())
                awaitComplete()
            }
        }
    }

    @Nested
    inner class WhenFilteringByGenre {
        @Test
        fun `returns only movies matching the genre`() = runTest {
            val movies = listOf(movieWithGenre(28), movieWithGenre(35))
            every { repository.getTrendingMovies() } returns flowOf(movies)
            useCase(genreFilter = 28).test {
                assertEquals(listOf(movieWithGenre(28)), awaitItem())
                awaitComplete()
            }
        }

        @Test
        fun `returns empty list when no movies match genre`() = runTest {
            every { repository.getTrendingMovies() } returns flowOf(listOf(movieWithGenre(28)))
            useCase(genreFilter = 99).test {
                assertTrue(awaitItem().isEmpty())
                awaitComplete()
            }
        }
    }

    @Nested
    inner class WhenSorting {
        @Test fun `sorts by title ascending`() = runTest { /* ... */ }
        @Test fun `sorts by release date descending`() = runTest { /* ... */ }
    }
}
```

---

## ViewModel Tests — JUnit 5 + Turbine

Use `@ExtendWith(MainDispatcherExtension::class)` (JUnit 5 extension, replaces `@get:Rule`):

```kotlin
// src/test/kotlin/.../TrendingMoviesViewModelTest.kt
@ExtendWith(MainDispatcherExtension::class)
class TrendingMoviesViewModelTest {

    private val getTrendingMovies: GetTrendingMoviesUseCase = mockk()
    private val getGenres: GetGenresUseCase = mockk()
    private val logger: Logger = mockk(relaxed = true)

    private val viewModel by lazy {
        TrendingMoviesViewModel(getTrendingMovies, getGenres, logger)
    }

    @Nested
    inner class WhenLoading {
        @Test
        fun `emits loading state then success state`() = runTest {
            every { getTrendingMovies(any(), any(), any()) } returns flowOf(fakeMovies)
            every { getGenres() } returns flowOf(fakeGenres)

            viewModel.state.test {
                assertTrue(awaitItem().isLoading)
                val success = awaitItem()
                assertFalse(success.isLoading)
                assertEquals(fakeMovies, success.movies)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `emits error state on exception`() = runTest {
            every { getTrendingMovies(any(), any(), any()) } returns flow { throw IOException("Network error") }
            every { getGenres() } returns flowOf(emptyList())

            viewModel.state.test {
                skipItems(1) // loading
                val error = awaitItem()
                assertFalse(error.isLoading)
                assertNotNull(error.error)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class WhenFiltering {
        @Test
        fun `FilterByGenre intent updates selectedGenreId in state`() = runTest {
            every { getTrendingMovies(any(), any(), any()) } returns flowOf(fakeMovies)
            every { getGenres() } returns flowOf(fakeGenres)

            viewModel.state.test {
                skipItems(2) // loading + initial success
                viewModel.handleIntent(TrendingMoviesIntent.FilterByGenre(28))
                val filtered = awaitItem()
                assertEquals(28, filtered.selectedGenreId)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
```

---

## Integration Tests — Data Layer

Uses **Room in-memory DB** + **MockWebServer** with real JSON fixtures. Tests the full pipeline from HTTP response → domain model. Located in `src/androidTest/` of `:feature:movies:data`.

```kotlin
@RunWith(AndroidJUnit4::class)
class MovieRepositoryImplIntegrationTest {

    private val mockWebServer = MockWebServer()
    private lateinit var db: AmroDatabase
    private lateinit var repository: MovieRepositoryImpl

    @Before
    fun setUp() {
        mockWebServer.start()
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AmroDatabase::class.java,
        ).allowMainThreadQueries().build()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(/* kotlinx.serialization */)
            .build()

        val remote = TmdbMovieDataSource(retrofit.create(TmdbApiService::class.java))
        val local = RoomMovieDataSource(db.movieDao(), db.genreDao())
        repository = MovieRepositoryImpl(remote, local, FakeLogger())
    }

    @After
    fun tearDown() { mockWebServer.shutdown(); db.close() }

    @Test
    fun `returns network data on success`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody(TRENDING_JSON_FIXTURE))
        repository.getTrendingMovies().first().also { movies ->
            assertEquals(20, movies.size)
            assertEquals("Inception", movies.first().title)
        }
    }

    @Test
    fun `falls back to cache when network fails`() = runBlocking {
        local.saveMovies(fakeMovies)
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        repository.getTrendingMovies().first().also { movies ->
            assertEquals(fakeMovies, movies)
        }
    }
}
```

Room DAO tests follow the same pattern (`inMemoryDatabaseBuilder`) but test DAOs directly.

---

## Integration Tests — Presentation Layer

Uses real UseCase implementations backed by a `FakeMovieRepository` (no MockK at this level). Located in `src/test/` of `:feature:movies:presentation:implementation`.

```kotlin
// FakeMovieRepository.kt
class FakeMovieRepository(
    var movies: List<Movie> = emptyList(),
    var genres: List<Genre> = emptyList(),
) : MovieRepository {
    override fun getTrendingMovies(): Flow<List<Movie>> = flowOf(movies)
    override fun getMovieDetail(id: Int): Flow<MovieDetail> = flowOf(fakeMovieDetail)
    override fun getGenres(): Flow<List<Genre>> = flowOf(genres)
}

// TrendingMoviesViewModelIntegrationTest.kt
@ExtendWith(MainDispatcherExtension::class)
class TrendingMoviesViewModelIntegrationTest {

    private val fakeRepo = FakeMovieRepository(movies = fakeMovies, genres = fakeGenres)
    private val getTrendingMovies = GetTrendingMoviesUseCaseImpl(fakeRepo)
    private val getGenres = GetGenresUseCaseImpl(fakeRepo)
    private val viewModel by lazy { TrendingMoviesViewModel(getTrendingMovies, getGenres, FakeLogger()) }

    @Test
    fun `genre filter intent reduces movie list end-to-end`() = runTest {
        viewModel.state.test {
            skipItems(2) // loading + initial success
            viewModel.handleIntent(TrendingMoviesIntent.FilterByGenre(28))
            val filtered = awaitItem()
            assertTrue(filtered.movies.all { 28 in it.genreIds })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

---

## UI Tests — Espresso + Robot Pattern

UI tests live in `src/androidTest/` and use `createComposeRule()` (screen-level, no full app launch). Each screen has a **Robot** class that encapsulates interactions.

```
src/androidTest/.../
├── robots/
│   ├── TrendingMoviesRobot.kt
│   └── MovieDetailRobot.kt
├── TrendingMoviesScreenTest.kt
└── MovieDetailScreenTest.kt
```

```kotlin
// robots/TrendingMoviesRobot.kt
class TrendingMoviesRobot(private val composeRule: ComposeContentTestRule) {

    fun assertMoviesListVisible() = apply {
        composeRule.onNodeWithTag("movies_list").assertIsDisplayed()
    }
    fun assertMovieVisible(title: String) = apply {
        composeRule.onNodeWithText(title).assertIsDisplayed()
    }
    fun filterByGenre(genreName: String) = apply {
        composeRule.onNodeWithText(genreName).performClick()
    }
    fun clickMovie(title: String) = apply {
        composeRule.onNodeWithText(title).performClick()
    }
    fun assertLoadingVisible() = apply {
        composeRule.onNodeWithTag("loading_view").assertIsDisplayed()
    }
    fun assertErrorVisible() = apply {
        composeRule.onNodeWithTag("error_view").assertIsDisplayed()
    }
}

// DSL entry point
fun ComposeContentTestRule.trendingMoviesRobot(
    block: TrendingMoviesRobot.() -> Unit,
) = TrendingMoviesRobot(this).apply(block)

// TrendingMoviesScreenTest.kt
@RunWith(AndroidJUnit4::class)
class TrendingMoviesScreenTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun `movies list is visible when data is loaded`() {
        composeRule.setContent {
            AmroTheme {
                TrendingMoviesContent(
                    state = TrendingMoviesState(isLoading = false, movies = fakeMovies, genres = fakeGenres),
                    onIntent = {},
                )
            }
        }
        composeRule.trendingMoviesRobot {
            assertMoviesListVisible()
            assertMovieVisible(fakeMovies.first().title)
        }
    }

    @Test
    fun `clicking genre chip fires FilterByGenre intent`() {
        val onIntent = mockk<(TrendingMoviesIntent) -> Unit>(relaxed = true)
        composeRule.setContent {
            AmroTheme {
                TrendingMoviesContent(state = TrendingMoviesState(genres = fakeGenres), onIntent = onIntent)
            }
        }
        composeRule.trendingMoviesRobot { filterByGenre(fakeGenres.first().name) }
        verify { onIntent(TrendingMoviesIntent.FilterByGenre(fakeGenres.first().id)) }
    }

    @Test
    fun `loading indicator is shown when isLoading is true`() {
        composeRule.setContent {
            AmroTheme { TrendingMoviesContent(state = TrendingMoviesState(isLoading = true), onIntent = {}) }
        }
        composeRule.trendingMoviesRobot { assertLoadingVisible() }
    }
}
```

**Robot rules:**
- Robots operate on `ComposeContentTestRule` — they never know which test calls them.
- `setContent {}` in the test; interactions in the robot — clean separation.
- Robots use `apply {}` so calls chain inside the DSL lambda.
- `@Nested` can still group related scenarios inside test classes.

---

## Test Utilities

| Utility | Purpose |
|---------|---------|
| `MainDispatcherExtension` | JUnit 5 `Extension` that sets `Dispatchers.Main` to `UnconfinedTestDispatcher` |
| `FakeMovieRepository` | In-memory `MovieRepository` implementation for integration/ViewModel tests |
| `FakeLogger` | No-op `Logger` for tests |
| `mockk(relaxed = true)` | Ignores calls on deps like `Logger` without explicit stubs |
| `coEvery / coVerify` | For suspend functions |
| `every / verify` | For regular functions and Flow-returning functions |
| `flowOf(...)` / `flow { throw ... }` | Stub `Flow`-returning repo/use case methods |
