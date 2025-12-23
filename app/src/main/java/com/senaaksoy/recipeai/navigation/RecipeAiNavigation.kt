package com.senaaksoy.recipeai.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import com.senaaksoy.recipeai.presentation.screens.favorite.FavoritesScreen
import com.senaaksoy.recipeai.presentation.screens.home.HomeScreen
import com.senaaksoy.recipeai.presentation.screens.profile.ProfileScreen
import com.senaaksoy.recipeai.presentation.screens.recipe.AddRecipeScreen
import com.senaaksoy.recipeai.presentation.screens.recipe.RecipeDetailScreen
import com.senaaksoy.recipeai.presentation.screens.splash.SplashScreen

@Composable
fun RecipeAiNavigation() {

    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: Screen.HomeScreen.route

    var topBarTitle by remember { mutableStateOf<String?>(null) }

    val showFab = currentRoute in listOf(
        Screen.HomeScreen.route,
        Screen.FavoritesScreen.route
    )

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
                    Screen.ProfileScreen.route
                )
            ) {
                BottomNavigationBar(navController = navController, currentRoute = currentRoute)
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.AddRecipeScreen.route)
                    },
                    containerColor = Color(0xFF667EEA),
                    contentColor = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(4.dp)
                    )
                }
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
                AddRecipeScreen()
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
                        topBarTitle = name
                    }
                )
            }
            composable(route = Screen.FavoritesScreen.route) {
                FavoritesScreen(navController = navController)
            }
            composable(route = Screen.ProfileScreen.route) {
                ProfileScreen(navController = navController)
            }
            composable(
                route = Screen.ResetPasswordScreen.route,
                arguments = listOf(
                    navArgument("token") {
                        type = NavType.StringType
                        defaultValue=""
                        nullable=true
                    }
                ),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "recipeai://reset-password/{token}"
                    },
                    navDeepLink {
                        uriPattern = "http://10.0.2.2:3000/reset-password.html?token={token}"
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