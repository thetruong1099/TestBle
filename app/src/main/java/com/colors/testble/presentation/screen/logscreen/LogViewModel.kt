package com.colors.testble.presentation.screen.logscreen

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.colors.testble.data.local.entity.LogEntity
import com.colors.testble.presentation.base.BaseViewModel
import com.colors.testble.presentation.base.IViewEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val realmDataBase: Realm,
) : BaseViewModel<LogViewState, LogViewEvent>() {
    override fun createInitialConText(): Context = appContext

    override fun createInitialState(): LogViewState = LogViewState()

    override fun onTriggerEvent(event: LogViewEvent) {
        viewModelScope.launch {
            when (event) {
                is LogViewEvent.GetLogList -> getLog()
            }
        }
    }

    private suspend fun getLog() {
        realmDataBase.query<LogEntity>().asFlow().map { it.list }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), currentState.logList)
            .collect {
                setState { copy(logList = it) }
            }
    }
}

sealed class LogViewEvent : IViewEvent {
    data object GetLogList : LogViewEvent()
}