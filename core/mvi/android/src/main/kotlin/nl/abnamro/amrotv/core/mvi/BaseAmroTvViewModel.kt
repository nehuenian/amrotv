package nl.abnamro.amrotv.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel for all MVI screens.
 *
 * Implements [AmroTvViewModel] and delegates state transitions to injectable
 * `{Screen}StateReducers` factory classes via [updateState]. Each state transition
 * is expressed as a [StateReducer] lambda returned by a named factory method,
 * keeping pure state logic isolated and testable without coroutines or a ViewModel.
 *
 * Subclasses override [handleIntent] to orchestrate async work, call [updateState]
 * for each state transition, and dispatch one-time commands via [sendEffect].
 *
 * @param initialState the state emitted before any intent is processed.
 */
abstract class BaseAmroTvViewModel<S : MviState, I : MviIntent, E : MviEffect>(
    initialState: S,
) : ViewModel(), AmroTvViewModel<S, I, E> {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = Channel<E>(Channel.BUFFERED)
    override val effects: Flow<E> = _effects.receiveAsFlow()

    /**
     * Applies [block] to the current state atomically and emits the result.
     *
     * Use this inside [handleIntent] or private async functions to apply a
     * [StateReducer] from the screen's `{Screen}StateReducers` factory.
     *
     * @param block a pure function that transforms the current state into the next state.
     */
    protected fun updateState(block: (S) -> S) {
        _state.update(block)
    }

    protected fun sendEffect(effect: E) {
        viewModelScope.launch { _effects.send(effect) }
    }
}

