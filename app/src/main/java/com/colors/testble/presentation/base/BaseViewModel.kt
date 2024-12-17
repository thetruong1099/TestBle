package com.colors.testble.presentation.base

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch

abstract class BaseViewModel<State : IViewState, Event : IViewEvent> : ViewModel() {
    private val initialContext: Context by lazy { createInitialConText() }
    private val initialState: State by lazy { createInitialState() }

    private val _uiState: MutableStateFlow<State> = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()
    protected val currentState: State get() = uiState.value

    abstract fun createInitialConText(): Context

    abstract fun createInitialState(): State

    abstract fun onTriggerEvent(event: Event)

    protected fun setState(reduce: State.() -> State) {
        val newState = currentState.reduce()
        _uiState.value = newState
    }

    // Call api not return
    protected suspend fun <T> call(
        callFlow: Flow<T>,
        completionHandler: (collect: T) -> Unit = {},
    ) {
        callFlow
            .catch { }
            .collect {
                completionHandler.invoke(it)
            }
    }

}
