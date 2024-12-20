package com.colors.testble.presentation.screen.mainscreen

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.colors.testble.presentation.utils.Screen

fun NavGraphBuilder.mainScreen(
    onNavigateToLogScreen: () -> Unit,
) {
    composable<Screen.MainScreen> {
        val viewModel = hiltViewModel<MainViewModel>()

        MainScreen(
            onEvent = viewModel::onTriggerEvent,
            onNavigateToLogScreen = onNavigateToLogScreen,
        )
    }
}