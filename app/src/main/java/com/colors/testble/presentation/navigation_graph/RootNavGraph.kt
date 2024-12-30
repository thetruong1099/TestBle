package com.colors.testble.presentation.navigation_graph

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.colors.testble.presentation.screen.logscreen.logScreen
import com.colors.testble.presentation.screen.mainscreen.mainScreen
import com.colors.testble.presentation.screen.scanscreen.scanScreen
import com.colors.testble.presentation.utils.Graph
import com.colors.testble.presentation.utils.Screen

@Composable
fun RootNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        route = Graph.ROOT::class,
        startDestination = Screen.MainScreen,
    ) {
        mainScreen(
            onNavigateToLogScreen = {
                navController.navigate(Screen.LogScreen)
            },

            onNavigateToScanScreen = {
                navController.navigate(Screen.ScanScreen)
            }
        )

        logScreen()

        scanScreen()
    }
}