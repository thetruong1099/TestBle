package com.colors.testble.presentation.screen.logscreen

import com.colors.testble.data.local.entity.LogEntity
import com.colors.testble.presentation.base.IViewState

data class LogViewState(
    val logList: List<LogEntity> = emptyList(),
):IViewState
