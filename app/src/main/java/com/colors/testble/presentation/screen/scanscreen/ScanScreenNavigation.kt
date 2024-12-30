package com.colors.testble.presentation.screen.scanscreen

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.colors.testble.presentation.utils.Screen

fun NavGraphBuilder.scanScreen() {
    composable<Screen.ScanScreen> {
        val viewModel = hiltViewModel<ScanViewModel>()
        val viewState by viewModel.uiState.collectAsStateWithLifecycle()
        ScanScreen(
            viewState = viewState,
            onEvent = viewModel::onTriggerEvent
        )
    }
}