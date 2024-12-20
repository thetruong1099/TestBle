package com.colors.testble.presentation.screen.logscreen

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.colors.testble.presentation.utils.Screen

fun NavGraphBuilder.logScreen() {
    composable<Screen.LogScreen> {
        val viewModel = hiltViewModel<LogViewModel>()
        val viewState by viewModel.uiState.collectAsStateWithLifecycle()
        LogScreen(
            viewState = viewState,
            onEvent = viewModel::onTriggerEvent
        )
    }
}