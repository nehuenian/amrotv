package nl.abnamro.amrotv.core.mvi

/**
 * Pure, single-responsibility state transformer for one MVI screen state.
 *
 * Each instance represents **one named transition** (e.g. loading, contentLoaded, loadFailed).
 * Instances are produced by an injectable `{Screen}StateReducers` factory class, keeping state
 * logic isolated and unit-testable without a ViewModel or coroutines.
 *
 * Implementations must be **pure**: no coroutines, no I/O, no side effects.
 *
 * Usage:
 * ```kotlin
 * updateState { it.reduceWith(stateReducers.contentLoaded(movies, genres)) }
 * ```
 *
 * @param S Screen state type — a [MviState] data class.
 */
fun interface StateReducer<S : MviState> {

    /**
     * Computes the next state from [currentState].
     *
     * Must be a pure function: same input always produces the same output, with no observable side
     * effects.
     *
     * @param currentState the state before this transition is applied.
     * @return the next state after applying this transition.
     */
    fun transform(currentState: S): S
}

/** Applies [reducer] to this state and returns the result. */
fun <S : MviState> S.reduceWith(reducer: StateReducer<S>): S = reducer.transform(this)

/**
 * Applies [reducerProvider] only when [predicate] is true; returns the state unchanged otherwise.
 */
inline fun <S : MviState> S.reduceWithIf(
    reducerProvider: (S) -> StateReducer<S>,
    predicate: (S) -> Boolean,
): S = if (predicate(this)) reducerProvider(this).transform(this) else this
