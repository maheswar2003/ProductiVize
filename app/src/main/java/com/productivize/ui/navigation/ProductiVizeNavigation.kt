package com.productivize.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.productivize.ui.screens.tracker.TrackerScreen
import com.productivize.ui.screens.insights.InsightsScreen
import com.productivize.ui.screens.journal.JournalScreen
import com.productivize.ui.screens.settings.SettingsScreen
import com.productivize.ui.screens.main.MainScreen

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Tracker : Screen("tracker")
    object Insights : Screen("insights")
    object Journal : Screen("journal")
    object Settings : Screen("settings")
}

@Composable
fun ProductiVizeNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Main.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }
        
        composable(Screen.Tracker.route) {
            TrackerScreen(navController = navController)
        }
        
        composable(Screen.Insights.route) {
            InsightsScreen(navController = navController)
        }
        
        composable(Screen.Journal.route) {
            JournalScreen(navController = navController)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
} 