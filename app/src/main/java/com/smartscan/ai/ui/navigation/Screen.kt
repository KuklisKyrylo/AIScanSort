package com.smartscan.ai.ui.navigation

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Settings : Screen("settings")
    data object ImageDetail : Screen("imageDetail/{imageId}") {
        fun createRoute(imageId: Long) = "imageDetail/$imageId"
    }
}

