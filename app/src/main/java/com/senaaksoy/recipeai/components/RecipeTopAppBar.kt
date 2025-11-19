package com.senaaksoy.recipeai.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.senaaksoy.recipeai.navigation.Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeTopAppBar(
    currentRoute: String,
    navController: NavController,
    title: String = ""
) {
    val hideTopBarRoutes = listOf(
        Screen.SplashScreen.route,
        Screen.SignInScreen.route,
        Screen.SignUpScreen.route,
        Screen.ForgotPasswordScreen.route,
        Screen.ResetPasswordScreen.route
    )

    if (currentRoute in hideTopBarRoutes) return

    val noBackButtonRoutes = listOf(
        Screen.HomeScreen.route,
        Screen.ProfileScreen.route
    )

    val navigationIconComposable: (@Composable () -> Unit)? = if (currentRoute !in noBackButtonRoutes) {
        {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
            }
        }
    } else {
        null
    }

    if (navigationIconComposable != null) {
        TopAppBar(
            title = { Text(text = if (title.isNotEmpty()) title else currentRoute) },
            navigationIcon = navigationIconComposable,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF0D47A1),
                titleContentColor = Color.White
            )
        )
    }
}


