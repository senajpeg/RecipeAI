package com.senaaksoy.recipeai.presentation.screens.splash

import android.window.SplashScreen
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.senaaksoy.recipeai.R
import com.senaaksoy.recipeai.navigation.Screen
import com.senaaksoy.recipeai.navigation.navigateSingleTopClear
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    modifier: Modifier=Modifier
){
    val context = LocalContext.current
    val view = LocalView.current

    DisposableEffect(Unit) {
        val window = (context as ComponentActivity).window
        val windowInsetsController = WindowCompat.getInsetsController(window, view)

        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
        }
    }

    LaunchedEffect(key1 = true) {
        delay(3000)
        navController.navigateSingleTopClear(route = Screen.SignUpScreen.route)
    }




    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF87CEF3),
                        Color(0xFF6F7DD7)
                    )
                )
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.recipeai_logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }














}
@Preview
@Composable
fun SplashPreview(
){
    val navController= rememberNavController()
    SplashScreen(navController = navController)
}