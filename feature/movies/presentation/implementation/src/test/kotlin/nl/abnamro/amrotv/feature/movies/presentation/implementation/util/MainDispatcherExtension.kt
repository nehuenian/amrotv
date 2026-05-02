package nl.abnamro.amrotv.feature.movies.presentation.implementation.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * JUnit 5 extension that installs a [StandardTestDispatcher] as [Dispatchers.Main] for each test.
 *
 * Use with [@RegisterExtension][org.junit.jupiter.api.extension.RegisterExtension] (instance field)
 * so tests can pass [testDispatcher] to [kotlinx.coroutines.test.runTest], sharing the same
 * [kotlinx.coroutines.test.TestCoroutineScheduler]. This ensures that
 * [kotlinx.coroutines.test.advanceUntilIdle] inside a test also drains coroutines launched on
 * [Dispatchers.Main] (e.g. [androidx.lifecycle.viewModelScope]).
 *
 * Why [StandardTestDispatcher] and not [kotlinx.coroutines.test.UnconfinedTestDispatcher]?
 * ViewModels that call [handleIntent] in their `init` block (e.g. to trigger an initial load) rely
 * on the dispatcher being lazy: the init block sets the loading state synchronously, then schedules
 * the async work. With [StandardTestDispatcher] the scheduled coroutine is only executed when the
 * test explicitly calls [kotlinx.coroutines.test.advanceUntilIdle], so tests can observe the
 * intermediate loading state before the final loaded/error state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherExtension : BeforeEachCallback, AfterEachCallback {

    val testDispatcher: TestDispatcher = StandardTestDispatcher()

    override fun beforeEach(context: ExtensionContext) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun afterEach(context: ExtensionContext) {
        Dispatchers.resetMain()
    }
}
