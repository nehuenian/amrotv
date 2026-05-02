# Advanced Test Patterns

Load this file when writing **ViewModel tests**, **fake repository** patterns, or **Espresso UI tests** with the Robot Pattern.

---

## ViewModel tests

### MainDispatcherExtension

ViewModels collect `StateFlow` on `Dispatchers.Main`. Inside `runTest`, `Dispatchers.Main` is not automatically replaced — you need to install a JUnit 5 extension that calls `Dispatchers.setMain` before each test.

Create this class once, alongside the ViewModel tests that need it (e.g. `feature/movies/presentation/implementation/src/test/.../MainDispatcherExtension.kt`):

Read the actual implementation: `feature/movies/presentation/implementation/src/test/kotlin/nl/abnamro/amrotv/feature/movies/presentation/implementation/util/MainDispatcherExtension.kt`

Key points:
- Uses `StandardTestDispatcher` (lazy, not `UnconfinedTestDispatcher`) so tests can observe intermediate loading states before calling `advanceUntilIdle()`
- Exposes `testDispatcher: TestDispatcher` for use in `runTest(mainExtension.testDispatcher)`

### Test class template

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class {Screen}ViewModelTest {

    @JvmField
    @RegisterExtension
    val mainExtension = MainDispatcherExtension()

    @MockK lateinit var get{Items}: Get{Items}UseCase
    @MockK lateinit var logger: Logger

    private lateinit var viewModel: {Screen}ViewModel

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = {Screen}ViewModel(
            get{Items} = get{Items},
            stateReducers = {Screen}StateReducers(/* mappers */),
            logger = logger,
        )
    }

    @Nested
    @DisplayName("GIVEN the use case returns valid {items}")
    inner class GivenUseCaseReturnsValid{Items} {

        @BeforeEach
        fun setUp() {
            coEvery { get{Items}() } returns Outcome.Success({items})
        }

        @Nested
        @DisplayName("WHEN the ViewModel is created")
        inner class WhenViewModelIsCreated {

            @Test
            @DisplayName("THEN state transitions through loading to loaded")
            fun stateTransitionsToLoaded() = runTest(mainExtension.testDispatcher) {
                viewModel.state.test {
                    assertTrue(awaitItem().isLoading)
                    advanceUntilIdle()
                    val loaded = awaitItem()
                    assertFalse(loaded.isLoading)
                    assertTrue(loaded.errors.isEmpty())
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }
}
```

> Use `@JvmField @RegisterExtension` (field-level) — **not** `@ExtendWith(MainDispatcherExtension::class)` (class-level). The field approach gives access to `mainExtension.testDispatcher` for passing to `runTest(mainExtension.testDispatcher)`.

---

## Fake repositories (presentation-layer integration tests)

Use a `Fake{Feature}Repository` (not MockK) for ViewModel integration tests. Return `Outcome<T>` from all methods:

```kotlin
class Fake{Feature}Repository(
    var items: List<{Item}> = emptyList(),
    var itemDetail: {Item}Detail = fake{Item}Detail,
) : {Feature}Repository {

    override suspend fun get{Items}(): Outcome<List<{Item}>> = Outcome.Success(items)
    override suspend fun get{Item}Detail(id: Int): Outcome<{Item}Detail> = Outcome.Success(itemDetail)
}
```

Mutate `items` / `itemDetail` between test calls to simulate different scenarios. For error simulation:

```kotlin
var shouldFail = false
override suspend fun get{Items}(): Outcome<List<{Item}>> =
    if (shouldFail) Outcome.Error(RuntimeException("network error")) else Outcome.Success(items)
```

Fakes give full control over behaviour without MockK's stub-per-test boilerplate, making them ideal when the ViewModel logic — not the repository contract — is the thing under test.

---

## UI Tests — Espresso + Robot Pattern

UI tests live in `src/androidTest/` and run on a device or emulator. Use the **Robot Pattern** to decouple test intent from Espresso API details.

### Location

```
:app or :feature:movies:ui
└── src/androidTest/kotlin/.../
    ├── {Screen}Robot.kt      ← Espresso interactions
    └── {Screen}Test.kt       ← test cases using the robot DSL
```

### Robot Pattern

The Robot encapsulates all Espresso interactions for one screen. Tests read as a DSL, not as raw Espresso calls:

```kotlin
// {Screen}Robot.kt
class {Screen}Robot {

    fun {action}(): {Screen}Robot {
        onView(withId(R.id.{viewId})).perform(click())
        return this
    }

    fun assert{Condition}(): {Screen}Robot {
        onView(withId(R.id.{viewId})).check(matches(isDisplayed()))
        return this
    }
}

// infix DSL helpers (optional but makes tests read naturally)
infix fun {Screen}Robot.andVerify(block: {Screen}Robot.() -> Unit): {Screen}Robot {
    block()
    return this
}
```

### Test class structure

```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class {Screen}Test {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule({Host}Activity::class.java)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun `GIVEN user is logged in WHEN navigating to screen THEN content is visible`() {
        {Screen}Robot()
            .assert{ContentVisible}()
    }
}
```

### Dependencies (app / ui module build.gradle.kts)

```kotlin
plugins {
    alias(libs.plugins.mannodermaus.android.junit5)
}

dependencies {
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.junit.jupiter.api)
    androidTestRuntimeOnly(libs.junit.jupiter.engine)
}
```

### Rules

- One Robot per screen — do not put Espresso calls directly in test methods
- Robots return `this` to allow chaining
- Use `@HiltAndroidTest` + `HiltAndroidRule` for all UI tests that need DI
- Apply the mannodermaus plugin only in modules that have `androidTest` requiring JUnit 5
