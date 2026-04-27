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
// src/test/kotlin/.../{Screen}UseCaseImplTest.kt
class {Screen}UseCaseImplTest {

    private val repository: {Feature}Repository = mockk()
    private val useCase = {Screen}UseCaseImpl(repository)

    @Nested
    inner class WhenNoFilterApplied {
        @Test
        fun `returns all items sorted by default criteria`() = runTest {
            every { repository.get{Items}() } returns flowOf(unsortedItems)
            useCase().test {
                assertEquals(itemsSortedByDefault, awaitItem())
                awaitComplete()
            }
        }
    }

    @Nested
    inner class WhenFilteringByGenre {
        @Test
        fun `returns only items matching the filter`() = runTest {
            every { repository.get{Items}() } returns flowOf(mixedItems)
            useCase(filter = someFilter).test {
                assertTrue(awaitItem().all { it.matches(someFilter) })
                awaitComplete()
            }
        }

        @Test
        fun `returns empty list when no items match filter`() = runTest {
            every { repository.get{Items}() } returns flowOf(items)
            useCase(filter = nonMatchingFilter).test {
                assertTrue(awaitItem().isEmpty())
                awaitComplete()
            }
        }
    }

    @Nested
    inner class WhenSorting {
        @Test fun `sorts by title ascending`() = runTest { /* ... */ }
        @Test fun `sorts by date descending`() = runTest { /* ... */ }
    }
}
```

---

## ViewModel Tests — JUnit 5 + Turbine

Use `@ExtendWith(MainDispatcherExtension::class)` (JUnit 5 extension, replaces `@get:Rule`):

```kotlin
// src/test/kotlin/.../{Screen}ViewModelTest.kt
@ExtendWith(MainDispatcherExtension::class)
class {Screen}ViewModelTest {

    private val get{Items}: Get{Items}UseCase = mockk()
    private val logger: Logger = mockk(relaxed = true)

    private val viewModel by lazy {
        {Screen}ViewModel(get{Items}, logger)
    }

    @Nested
    inner class WhenLoading {
        @Test
        fun `emits loading state then success state`() = runTest {
            every { get{Items}(/* any args */) } returns flowOf(fakeItems)

            viewModel.state.test {
                assertTrue(awaitItem().isLoading)
                val success = awaitItem()
                assertFalse(success.isLoading)
                assertNull(success.error)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `emits error state on exception`() = runTest {
            every { get{Items}(/* any args */) } returns flow { throw IOException("Network error") }

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
    inner class When{SomeIntent} {
        @Test
        fun `{SomeIntent} intent updates state correctly`() = runTest {
            every { get{Items}(/* any args */) } returns flowOf(fakeItems)

            viewModel.state.test {
                skipItems(2) // loading + initial success
                viewModel.handleIntent({Screen}Intent.{SomeIntent}(someParam))
                val updated = awaitItem()
                assertEquals(expectedValue, updated.someField)
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
class {Feature}RepositoryImplIntegrationTest {

    private val mockWebServer = MockWebServer()
    private lateinit var db: AmroDatabase
    private lateinit var repository: {Feature}RepositoryImpl

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

        val remote = Tmdb{Feature}DataSource(retrofit.create(Tmdb{Feature}ApiService::class.java))
        val local = Room{Feature}DataSource(db.{feature}Dao())
        repository = {Feature}RepositoryImpl(remote, local, FakeLogger())
    }

    @After
    fun tearDown() { mockWebServer.shutdown(); db.close() }

    @Test
    fun `returns network data on success`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody(JSON_FIXTURE))
        repository.get{Items}().first().also { items ->
            assertFalse(items.isEmpty())
        }
    }

    @Test
    fun `falls back to cache when network fails`() = runBlocking {
        // seed local cache
        local.save{Items}(fakeItems)
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        repository.get{Items}().first().also { items ->
            assertEquals(fakeItems, items)
        }
    }
}
```

Room DAO tests follow the same pattern (`inMemoryDatabaseBuilder`) but test DAOs directly.

---

## Integration Tests — Presentation Layer

Uses real UseCase implementations backed by a `Fake{Feature}Repository` (no MockK at this level). Located in `src/test/` of `:feature:{feature}:presentation:implementation`.

```kotlin
// Fake{Feature}Repository.kt
class Fake{Feature}Repository(
    var items: List<{Item}> = emptyList(),
) : {Feature}Repository {
    override fun get{Items}(): Flow<List<{Item}>> = flowOf(items)
    override fun get{Item}Detail(id: Int): Flow<{Item}Detail> = flowOf(fake{Item}Detail)
}

// {Screen}ViewModelIntegrationTest.kt
@ExtendWith(MainDispatcherExtension::class)
class {Screen}ViewModelIntegrationTest {

    private val fakeRepo = Fake{Feature}Repository(items = fakeItems)
    private val get{Items} = Get{Items}UseCaseImpl(fakeRepo)
    private val viewModel by lazy { {Screen}ViewModel(get{Items}, FakeLogger()) }

    @Test
    fun `filter intent reduces item list end-to-end`() = runTest {
        viewModel.state.test {
            skipItems(2) // loading + initial success
            viewModel.handleIntent({Screen}Intent.Filter(someFilter))
            val filtered = awaitItem()
            assertTrue(filtered.items.all { it.matches(someFilter) })
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
│   ├── {Screen}Robot.kt
│   └── {OtherScreen}Robot.kt
├── {Screen}ScreenTest.kt
└── {OtherScreen}ScreenTest.kt
```

```kotlin
// robots/{Screen}Robot.kt
class {Screen}Robot(private val composeRule: ComposeContentTestRule) {

    fun assert{List}Visible() = apply {
        composeRule.onNodeWithTag("{list}_list").assertIsDisplayed()
    }
    fun assert{Item}Visible(label: String) = apply {
        composeRule.onNodeWithText(label).assertIsDisplayed()
    }
    fun click{Action}(label: String) = apply {
        composeRule.onNodeWithText(label).performClick()
    }
    fun assertLoadingVisible() = apply {
        composeRule.onNodeWithTag("loading_view").assertIsDisplayed()
    }
    fun assertErrorVisible() = apply {
        composeRule.onNodeWithTag("error_view").assertIsDisplayed()
    }
}

// DSL entry point
fun ComposeContentTestRule.{screen}Robot(
    block: {Screen}Robot.() -> Unit,
) = {Screen}Robot(this).apply(block)

// {Screen}ScreenTest.kt
@RunWith(AndroidJUnit4::class)
class {Screen}ScreenTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun `list is visible when data is loaded`() {
        composeRule.setContent {
            AmroTheme {
                {Screen}Content(
                    state = {Screen}State(isLoading = false, items = fakeItems),
                    onIntent = {},
                )
            }
        }
        composeRule.{screen}Robot {
            assert{List}Visible()
            assert{Item}Visible(fakeItems.first().title)
        }
    }

    @Test
    fun `clicking filter fires correct intent`() {
        val onIntent = mockk<({Screen}Intent) -> Unit>(relaxed = true)
        composeRule.setContent {
            AmroTheme {
                {Screen}Content(state = {Screen}State(/* fake data */), onIntent = onIntent)
            }
        }
        composeRule.{screen}Robot { click{Action}(fakeFilter.label) }
        verify { onIntent({Screen}Intent.{FilterIntent}(fakeFilter.id)) }
    }

    @Test
    fun `loading indicator is shown when isLoading is true`() {
        composeRule.setContent {
            AmroTheme { {Screen}Content(state = {Screen}State(isLoading = true), onIntent = {}) }
        }
        composeRule.{screen}Robot { assertLoadingVisible() }
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
| `Fake{Feature}Repository` | In-memory `{Feature}Repository` for integration/ViewModel tests |
| `FakeLogger` | No-op `Logger` for tests |
| `mockk(relaxed = true)` | Ignores calls on deps like `Logger` without explicit stubs |
| `coEvery / coVerify` | For suspend functions |
| `every / verify` | For regular functions and Flow-returning functions |
| `flowOf(...)` / `flow { throw ... }` | Stub `Flow`-returning repo/use case methods |
