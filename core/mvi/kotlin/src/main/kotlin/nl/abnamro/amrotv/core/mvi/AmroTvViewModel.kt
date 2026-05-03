package nl.abnamro.amrotv.core.mvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Public contract for all MVI screen ViewModels in AMRO.
 *
 * The UI layer depends on this interface; all user actions enter through [handleIntent]. State
 * transitions are applied inside [handleIntent] via [StateReducer] factories, keeping pure state
 * logic isolated and independently testable. This separation allows callers and unit tests to
 * program against the contract without coupling to Android lifecycle or ViewModel internals.
 */
interface AmroTvViewModel<S : MviState, I : MviIntent, E : MviEffect> {

    /** Reactive state stream. Collect with `collectAsStateWithLifecycle` in Compose. */
    val state: StateFlow<S>

    /**
     * One-shot side-effect stream (navigation, snackbars, URL opens).
     *
     * Each emission is consumed exactly once; late subscribers do not replay past effects.
     */
    val effects: Flow<E>

    /**
     * Entry point for all user actions.
     *
     * Implementations route the intent to the appropriate async work, state transitions (via
     * [StateReducer] factories), and one-shot effects.
     *
     * @param intent the user action to route into the ViewModel's business logic.
     */
    fun handleIntent(intent: I)
}
