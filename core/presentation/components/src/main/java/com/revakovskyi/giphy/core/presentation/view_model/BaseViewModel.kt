package com.revakovskyi.giphy.core.presentation.view_model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<S, A, E>(initialState: S) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _event = Channel<E>()
    val event: Flow<E> = _event.receiveAsFlow()


    abstract fun onAction(action: A)


    protected fun updateState(update: (S) -> S) {
        _state.update { update(it) }
    }

    protected fun sendEvent(event: E) {
        _event.trySend(event).isSuccess
    }

}
