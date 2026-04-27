package nl.abnamro.amrotv.core.mvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Public contract for all MVI screen ViewModels in AMRO.
 *
 * The UI layer depends on this interface; all user actions enter through [handleIntent].
 * This separation allows callers and unit tests to program against the contract
 * without coupling to Android lifecycle or ViewModel internals.
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
     * Implementations decide whether an intent results in a pure state change (via [StateReducer])
     * or triggers async work and side effects.
     *
     * @param intent the user action to route into the ViewModel's business logic.
     */
    fun handleIntent(intent: I)

    /**
     * Applies [StateReducer] to the current state using [intent] and emits the result atomically.
     *
     * Use this for intents that require only a pure state change with no side effects.
     *
     * @param intent the intent to fold into the current state via [StateReducer.reduce].
     */
    fun reduce(intent: I)

    /**
     * Sends [effect] to the effects channel to be consumed once by the UI.
     *
     * @param effect the one-time event to dispatch to the UI layer.
     */
    fun sendEffect(effect: E)
}
