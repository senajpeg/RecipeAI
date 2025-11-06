package com.senaaksoy.recipeai.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.senaaksoy.recipeai.presentation.screens.auth.SignInScreen
import com.senaaksoy.recipeai.presentation.screens.auth.SignUpScreen
import com.senaaksoy.recipeai.presentation.screens.splash.SplashScreen

@Composable
fun RecipeAiNavigation() {

    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = currentBackStackEntry?.destination?.route ?: Screen.HomeScreen.route

    Scaffold(
        topBar = {},
        bottomBar = {}
    ) { paddingValues ->
        NavHost(
            modifier = Modifier.padding(paddingValues),
            navController = navController,
            startDestination = Screen.SplashScreen.route
        ) {
            composable(route = Screen.SplashScreen.route) {
                SplashScreen(navController = navController)
            }
            composable(route = Screen.HomeScreen.route) {

            }
            composable(route = Screen.SignInScreen.route) {
                SignInScreen(navController = navController)
            }
            composable(route = Screen.SignUpScreen.route) {
                SignUpScreen(navController = navController)
            }
            composable(route = Screen.AddRecipeScreen.route) {

            }
            composable(route = Screen.RecipeDetailScreen.route) {

            }
            composable(route = Screen.FavoritesScreen.route) {

            }
            composable(route = Screen.SearchScreen.route) {

            }
            composable(route = Screen.ProfileScreen.route) {

            }
            composable(route = Screen.ResetPasswordScreen.route) {

            }
            composable(route = Screen.ForgotPasswordScreen.route) {

            }


        }

    }


}