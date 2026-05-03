# Advanced Test Patterns

Load this file when writing **ViewModel tests**, **fake repository** patterns, or **Compose E2E instrumented tests** with the Robot Pattern.

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

## E2E Instrumented Tests — Compose + Hilt + MockWebServer

E2E tests live in `src/androidTest/` and run on a device or emulator. The project uses
**Jetpack Compose testing** (not Espresso) with the **typed Robot Pattern** for readable,
maintainable tests. All network traffic is intercepted by `MockWebServer` loaded with real
API fixture data, making tests fully deterministic and network-independent.

### File layout

```
:feature:{feature}:ui
└── src/androidTest/
    ├── kotlin/.../
    │   ├── {Feature}E2ETestActivity.kt     ← @AndroidEntryPoint host activity
    │   ├── E2ETestData.kt                  ← all test string/ID constants
    │   ├── mock/
    │   │   ├── MockWebServerRule.kt        ← JUnit 4 rule wrapping MockWebServer
    │   │   └── {Feature}MockDispatcher.kt  ← routes requests to JSON fixtures
    │   └── {screen}/robots/
    │       └── {Screen}Robot.kt            ← typed Robot implementation
    └── resources/mock/                     ← JSON fixture files (real API responses)
```

### Rule order

```kotlin
@get:Rule(order = 0) val mockServerRule = MockWebServerRule()
@get:Rule(order = 1) val hiltRule = HiltAndroidRule(this)
@get:Rule(order = 2) val composeRule = createAndroidComposeRule<{Feature}E2ETestActivity>()
```

`MockWebServerRule` must be order 0 so the base URL is available when the
`@Singleton Retrofit.Builder` is provided by Hilt.

### Test class template

```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class {Screen}FlowE2ETest {

    @get:Rule(order = 0) val mockServerRule = MockWebServerRule()
    @get:Rule(order = 1) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 2) val composeRule = createAndroidComposeRule<{Feature}E2ETestActivity>()

    // DI overrides live as an inner @Module — no separate test module file needed
    @Module
    @InstallIn(SingletonComponent::class)
    object TestBuildConfigModule {
        @Provides @Singleton
        fun provideBuildConfigProvider(): BuildConfigProvider = object : BuildConfigProvider {
            override val tmdbReadAccessToken: String get() = ""
            override val isDebug: Boolean get() = true
        }
    }

    @Before
    fun setUp() {
        // Do NOT call hiltRule.inject() unless this class has @Inject fields.
        // hiltViewModel() inside setContent {} uses Hilt's built-in factory via the activity.
        composeRule.setContent {
            var current: Screen by remember { mutableStateOf(Screen.List) }
            AmroTvTheme {
                when (val screen = current) {
                    is Screen.List -> {List}Screen(
                        onNavigate = { id -> current = Screen.Detail(id) },
                        viewModel = hiltViewModel<{List}ViewModel>(),
                    )
                    is Screen.Detail -> {Detail}Screen(
                        navigateBack = { current = Screen.List },
                        viewModel = hiltViewModel<{Detail}ViewModel, {Detail}ViewModel.Factory>(
                            creationCallback = { factory -> factory.create(screen.id) },
                        ),
                    )
                }
            }
        }
    }

    @Test
    fun given{Context}_when{Action}_then{Outcome}() {
        withRobot({Screen}Robot(composeRule)) {
            execute { onLoaded { doSomething() } }
            verify { onLoaded { somethingIsVisible() } }
        }
    }
}
```

### MockWebServer dispatcher

Route requests by **concrete path** where the ID is known. Avoid broad regexes:

```kotlin
class {Feature}MockDispatcher : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        val path = request.path ?: return MockResponse().setResponseCode(404)
        return when {
            // Concrete ID path — preferred over Regex("\\d+") when ID is known
            path.startsWith("/3/{resource}/${E2ETestData.KNOWN_ID}") ->
                mockResponse("mock/{detail_fixture}.json")
            path.startsWith("/3/{resource}/list") ->
                mockResponse("mock/{list_fixture}.json")
            else -> MockResponse().setResponseCode(404)
        }
    }

    private fun mockResponse(assetPath: String): MockResponse {
        val body = javaClass.classLoader!!
            .getResourceAsStream(assetPath)!!
            .bufferedReader().readText()
        return MockResponse()
            .setResponseCode(200)
            .setBody(body)
            .addHeader("Content-Type", "application/json")
    }
}
```

Fixture files live in `src/androidTest/resources/mock/` and contain real API responses.
Source them from the live API once, then commit — they become the stable test contract.

### Typed Robot Pattern

Robots implement `Robot<A : RobotActionScope, V : RobotVerificationScope>` from
`:core:testing`. The typed scope split ensures actions cannot be called in `verify {}`
blocks and vice versa:

```kotlin
class {Screen}Robot(
    private val rule: ComposeContentTestRule,
) : Robot<{Screen}Robot.Actions, {Screen}Robot.Verifications> {

    interface Actions : RobotActionScope {
        fun onLoaded(block: LoadedActions.() -> Unit)
    }

    interface LoadedActions {
        fun filterBy(name: String)
        fun scrollTo(title: String)
    }

    interface Verifications : RobotVerificationScope {
        fun onLoaded(block: LoadedVerifications.() -> Unit)
    }

    interface LoadedVerifications {
        fun itemVisible(title: String)
        fun itemNotVisible(title: String)
    }

    override fun actionScope(): Actions = ActionsImpl(rule)
    override fun verificationScope(): Verifications = VerificationsImpl(rule)

    private class ActionsImpl(private val rule: ComposeContentTestRule) : Actions {
        override fun onLoaded(block: LoadedActions.() -> Unit) {
            rule.waitForContent()
            object : LoadedActions {
                override fun filterBy(name: String) {
                    // Scroll the horizontal row first — chips may be off-screen
                    rule.onNode(
                        SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange)
                    ).performScrollToNode(hasText(name))
                    rule.onNode(hasText(name).and(hasClickAction())).performClick()
                }

                override fun scrollTo(title: String) {
                    rule.onNode(
                        SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
                    ).performScrollToNode(hasText(title))
                }
            }.block()
        }
    }

    private class VerificationsImpl(private val rule: ComposeContentTestRule) : Verifications {
        override fun onLoaded(block: LoadedVerifications.() -> Unit) {
            rule.waitForContent()
            object : LoadedVerifications {
                override fun itemVisible(title: String) {
                    // Scroll first — items below the fold are not in viewport
                    rule.onNode(
                        SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
                    ).performScrollToNode(hasText(title))
                    rule.onNodeWithText(title).assertIsDisplayed()
                }

                override fun itemNotVisible(title: String) {
                    // Use waitUntil — ViewModel recompose may not have settled yet
                    rule.waitUntil(timeoutMillis = 5_000) {
                        rule.onAllNodesWithText(title).fetchSemanticsNodes().isEmpty()
                    }
                }
            }.block()
        }
    }

    companion object {
        private fun ComposeContentTestRule.waitForContent(timeoutMs: Long = 15_000) {
            waitUntil(timeoutMillis = timeoutMs) {
                // Anchor on a stable, always-present element — NOT a conditional one.
                // Example: overview contentDescription is always rendered when detail loads;
                // the IMDb button is conditional on imdbId != null and must NOT be used here.
                onAllNodes(
                    hasContentDescription("{stableElement}", substring = true)
                ).fetchSemanticsNodes().isNotEmpty()
            }
        }
    }
}
```

### Assertions

Use `org.junit.Assert.assertTrue` — **never** Kotlin's built-in `assert()`. Kotlin's
`assert()` is a JVM assertion disabled by default on Android, making it a silent no-op:

```kotlin
// ❌ No-op on Android JVM — never throws even when the condition is false
assert(nodes.isNotEmpty()) { "Expected at least one card" }

// ✅ Always enforced
assertTrue("Expected at least one card", nodes.isNotEmpty())
```

### Off-screen elements

`LazyVerticalGrid` and horizontally scrolling `Row`s render off-screen items that must be
scrolled into view before interacting or asserting:

```kotlin
// Vertical grid — scroll before asserting
rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
    .performScrollToNode(hasText(title))
rule.onNodeWithText(title).assertIsDisplayed()

// Horizontal row — scroll before clicking
rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange))
    .performScrollToNode(hasText(chipName))
rule.onNode(hasText(chipName).and(hasClickAction())).performClick()
```

### Rules

- One Robot per screen — no Compose test API calls directly in test methods
- Typed interfaces for `Actions` and `Verifications` (and their scoped sub-interfaces)
- `waitForContent` anchors on a **stable, always-present element** (not conditional ones)
- DI overrides are inner `@Module @InstallIn(...)` objects inside the test class
- `hiltRule.inject()` is only needed if the test class declares `@Inject` fields
- E2E tests use **JUnit 4** (`@RunWith(AndroidJUnit4::class)`), not JUnit 5
