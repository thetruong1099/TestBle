package com.colors.testble.presentation.utils

import kotlinx.serialization.Serializable

@Serializable
sealed class Graph {
    @Serializable
    data object ROOT : Graph()
}
@Serializable
sealed class Screen {
    @Serializable
    data object MainScreen : Screen()
    @Serializable
    data object LogScreen : Screen()
    @Serializable
    data object ScanScreen : Screen()
}