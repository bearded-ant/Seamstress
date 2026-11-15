package com.example.seamstress.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

/** Весь граф экранов; заменяет `navigation.xml` + `NavHostFragment`. */
@Composable
fun SeamstressNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = CustomersList) {
        customerGraph(navController)
        measurementGraph(navController)
        settingsGraph(navController)
    }
}
