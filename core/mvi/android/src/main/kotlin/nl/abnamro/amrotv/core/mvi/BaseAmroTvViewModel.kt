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
 * Implements [AmroTvViewModel] and delegates pure state transitions to [StateReducer].
 * Subclasses override [handleIntent] to orchestrate async work and dispatch effects
 * via [sendEffect] and pure state changes via [reduce].
 *
 * @param reducer drives initial state and all pure state transitions.
 */
abstract class BaseAmroTvViewModel<S : MviState, I : MviIntent, E : MviEffect>(
    private val reducer: StateReducer<S, I>,
) : ViewModel(), AmroTvViewModel<S, I, E> {

    private val _state = MutableStateFlow(reducer.initialState)
    override val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = Channel<E>(Channel.BUFFERED)
    override val effects: Flow<E> = _effects.receiveAsFlow()

    override fun reduce(intent: I) {
        _state.update { current -> reducer.reduce(current, intent) }
    }

    override fun sendEffect(effect: E) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
