package com.senaaksoy.recipeai.presentation.screens.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.senaaksoy.recipeai.R
import com.senaaksoy.recipeai.navigation.Screen
import com.senaaksoy.recipeai.navigation.navigateSingleTopClear
import com.senaaksoy.recipeai.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel= hiltViewModel()
) {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0A1F44), Color(0xFF123C7A)),
                    start = androidx.compose.ui.geometry.Offset(offsetAnim, 0f),
                    end = androidx.compose.ui.geometry.Offset(0f, offsetAnim)
                )
            )
            .padding(20.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF123C7A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(55.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
            val userName = authViewModel.getUserName()
            val userEmail = authViewModel.getUserEmail()
            Text(
                text = userName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = userEmail,
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(Modifier.height(40.dp))

            ProfileInfoCard(
                title = stringResource(R.string.recipes),
                count = 12,
                icon = Icons.Default.Restaurant,
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(Modifier.height(20.dp))

            ProfileInfoCard(
                title = stringResource(R.string.my_favorites),
                count = 25,
                icon = Icons.Default.Favorite,
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(Modifier.height(40.dp))
        }

        val coroutineScope = rememberCoroutineScope()
        var pressed by remember { mutableStateOf(false) }

        val buttonScale by animateFloatAsState(
            targetValue = if (pressed) 0.94f else 1f,
            animationSpec = tween(150)
        )

        val buttonGradientShift by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 600f,
            animationSpec = infiniteRepeatable(
                animation = tween(6000, easing = LinearEasing)
            )
        )

        Button(
            onClick = {
                pressed = true
                coroutineScope.launch {
                    delay(150)
                    pressed = false
                    authViewModel.logout()
                    navController.navigateSingleTopClear(Screen.SignInScreen.route)
                }
            },
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer(scaleX = buttonScale, scaleY = buttonScale)
                .fillMaxWidth(0.6f)
                .widthIn(max = 200.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF3C7EB4),
                            Color(0xFF5E8BCB)
                        ),
                        startX = buttonGradientShift,
                        endX = buttonGradientShift + 300f
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
        ) {
            Text(
                text = stringResource(R.string.logout),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ProfileInfoCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF77B6FF),
            modifier = Modifier.size(40.dp)
        )

        Spacer(Modifier.height(10.dp))

        Text(text = title, color = Color.White, fontSize = 17.sp)

        Text(
            text = count.toString(),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
fun ProfilePreview() {
    val navController = rememberNavController()
    ProfileScreen(navController = navController)
}
