package com.smartscan.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.smartscan.ai.ui.detail.ImageDetailScreenRoute
import com.smartscan.ai.ui.main.MainScreenRoute
import com.smartscan.ai.ui.navigation.Screen
import com.smartscan.ai.ui.settings.SettingsScreenRoute
import com.smartscan.ai.ui.theme.SmartScanTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartScanTheme {
                Surface {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Main.route
                    ) {
                        composable(Screen.Main.route) {
                            MainScreenRoute(
                                onNavigateToSettings = {
                                    navController.navigate(Screen.Settings.route)
                                },
                                onImageClick = { imageId ->
                                    navController.navigate(Screen.ImageDetail.createRoute(imageId))
                                }
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreenRoute(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            route = Screen.ImageDetail.route,
                            arguments = listOf(
                                navArgument("imageId") { type = NavType.LongType }
                            )
                        ) { backStackEntry ->
                            val imageId = backStackEntry.arguments?.getLong("imageId") ?: 0L
                            ImageDetailScreenRoute(
                                imageId = imageId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

