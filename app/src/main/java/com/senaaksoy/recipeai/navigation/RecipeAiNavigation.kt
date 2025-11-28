package com.senaaksoy.recipeai.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.senaaksoy.recipeai.components.BottomNavigationBar
import com.senaaksoy.recipeai.components.RecipeTopAppBar
import com.senaaksoy.recipeai.presentation.screens.auth.ForgotPasswordScreen
import com.senaaksoy.recipeai.presentation.screens.auth.ResetPasswordScreen
import com.senaaksoy.recipeai.presentation.screens.auth.SignInScreen
import com.senaaksoy.recipeai.presentation.screens.auth.SignUpScreen
import com.senaaksoy.recipeai.presentation.screens.detail.RecipeDetailScreen
import com.senaaksoy.recipeai.presentation.screens.home.HomeScreen
import com.senaaksoy.recipeai.presentation.screens.profile.ProfileScreen
import com.senaaksoy.recipeai.presentation.screens.recipe.AddRecipeScreen
import com.senaaksoy.recipeai.presentation.screens.splash.SplashScreen

@Composable
fun RecipeAiNavigation() {

    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: Screen.HomeScreen.route

    var topBarTitle by remember { mutableStateOf<String?>(null) }


    Scaffold(
        topBar = {
            RecipeTopAppBar(
                currentRoute = currentRoute,
                navController = navController,
                title = topBarTitle
            )
        },
        bottomBar = {
            if (currentRoute in listOf(
                    Screen.HomeScreen.route,
                    Screen.FavoritesScreen.route,
                    Screen.SearchScreen.route,
                    Screen.ProfileScreen.route
                )
            ) {
                BottomNavigationBar(navController = navController, currentRoute = currentRoute)
            }
        }
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
                HomeScreen(navController = navController)
            }
            composable(route = Screen.SignInScreen.route) {
                SignInScreen(navController = navController)
            }
            composable(route = Screen.SignUpScreen.route) {
                SignUpScreen(navController = navController)
            }
            composable(route = Screen.AddRecipeScreen.route) {
                AddRecipeScreen(navController = navController)
            }
            composable(
                route = Screen.RecipeDetailScreen.route,
                arguments = listOf(
                    navArgument("recipeId") {
                        type = NavType.IntType
                    }
                )
            ) {
                RecipeDetailScreen(
                    navController = navController,
                    onRecipeLoaded = { name ->
                        topBarTitle=name
                    }
                )
            }
            composable(route = Screen.FavoritesScreen.route) {

            }
            composable(route = Screen.SearchScreen.route) {

            }
            composable(route = Screen.ProfileScreen.route) {
                ProfileScreen(navController = navController)
            }
            composable(
                route = Screen.ResetPasswordScreen.route,
                arguments = listOf(
                    navArgument("token") {
                        type = NavType.StringType
                    }
                ),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "recipeai://reset-password/{token}"
                    }
                )
            ) { backStackEntry ->
                val token = backStackEntry.arguments?.getString("token") ?: ""
                ResetPasswordScreen(
                    navController = navController,
                    token = token
                )
            }
            composable(route = Screen.ForgotPasswordScreen.route) {
                ForgotPasswordScreen(navController = navController)
            }
        }
    }
}