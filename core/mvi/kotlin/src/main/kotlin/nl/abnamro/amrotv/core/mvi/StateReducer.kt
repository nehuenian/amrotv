package nl.abnamro.amrotv.core.mvi

/**
 * Pure state machine for a single MVI screen.
 *
 * Both the initial state and all state transitions are defined here, keeping
 * state logic isolated and unit-testable without a ViewModel or coroutines.
 *
 * Implementations must be **pure**: no coroutines, no I/O, no side effects.
 * Async work and one-time effects belong in [AmroTvViewModel].
 *
 * @param S State type — a [MviState] data class.
 * @param I Intent type — a [MviIntent] sealed interface.
 */
interface StateReducer<S : MviState, I : MviIntent> {

    /** State emitted before any intent is processed. */
    val initialState: S

    /**
     * Computes the next state from [currentState] and [intent].
     *
     * Must be a pure function: same inputs always produce the same output,
     * with no observable side effects.
     *
     * @param currentState the state before this intent is applied.
     * @param intent the user action driving the state transition.
     * @return the next state after applying [intent] to [currentState].
     */
    fun reduce(currentState: S, intent: I): S
}
