package com.senaaksoy.recipeai.presentation.screens.profile

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.senaaksoy.recipeai.R
import com.senaaksoy.recipeai.navigation.Screen
import com.senaaksoy.recipeai.navigation.navigateSingleTopClear
import com.senaaksoy.recipeai.presentation.viewmodel.AuthViewModel
import com.senaaksoy.recipeai.utills.ImageUtils
import com.senaaksoy.recipeai.utills.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State'ler
    val userProfile by authViewModel.userProfile.collectAsState()
    val profilePictureState by authViewModel.profilePictureState.collectAsState()

    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showToast by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Profil bilgilerini yükle - Her açılışta
    LaunchedEffect(Unit) {
        Log.d("ProfileScreen", "Loading user profile")
        authViewModel.loadUserProfile()
    }

    // Profil fotoğrafını yükle ve göster
    LaunchedEffect(userProfile) {
        val base64 = userProfile?.profile_picture
        Log.d("ProfileScreen", "UserProfile changed")
        Log.d("ProfileScreen", "Profile picture available: ${base64 != null}")
        Log.d("ProfileScreen", "Profile picture length: ${base64?.length ?: 0}")

        if (!base64.isNullOrEmpty()) {
            Log.d("ProfileScreen", "First 100 chars: ${base64.take(100)}")
            val bitmap = ImageUtils.base64ToBitmap(base64)
            if (bitmap != null) {
                profileBitmap = bitmap
                Log.d("ProfileScreen", "Bitmap loaded successfully: ${bitmap.width}x${bitmap.height}")
            } else {
                Log.e("ProfileScreen", "Failed to convert base64 to bitmap")
            }
        } else {
            Log.w("ProfileScreen", "No profile picture available")
            profileBitmap = null
        }
    }

    // Profil fotoğrafı güncelleme sonucunu dinle
    LaunchedEffect(profilePictureState) {
        when (val state = profilePictureState) {
            is Resource.Success -> {
                showToast = "Profil fotoğrafı güncellendi"
                isLoading = false
                Log.d("ProfileScreen", "Upload successful, reloading profile")
                // Biraz bekle ve profili yeniden yükle
                delay(500)
                authViewModel.loadUserProfile()
            }
            is Resource.Error -> {
                showToast = state.message ?: "Hata oluştu"
                isLoading = false
                Log.e("ProfileScreen", "Upload error: ${state.message}")
            }
            is Resource.Loading -> {
                isLoading = true
                Log.d("ProfileScreen", "Upload in progress")
            }
            else -> {
                isLoading = false
            }
        }
    }

    // Galeri launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                Log.d("ProfileScreen", "Image selected: $uri")
                val base64 = ImageUtils.uriToBase64(context, it)
                if (base64 != null) {
                    Log.d("ProfileScreen", "Base64 created (length: ${base64.length}), uploading...")
                    authViewModel.updateProfilePicture(base64)
                } else {
                    showToast = "Fotoğraf yüklenemedi"
                    Log.e("ProfileScreen", "Failed to convert URI to base64")
                }
            }
        }
    }

    // İzin launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            showToast = "Galeri izni gerekli"
        }
    }

    // Animasyonlar
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val offsetAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing)
        ),
        label = "offset"
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

            // Profil Fotoğrafı
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF123C7A))
                    .clickable {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (profileBitmap != null) {
                    Image(
                        bitmap = profileBitmap!!.asImageBitmap(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(55.dp)
                    )
                }

                // Loading indicator
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(30.dp),
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            val userName = userProfile?.name ?: authViewModel.getUserName()
            val userEmail = userProfile?.email ?: authViewModel.getUserEmail()

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

        // Logout Button
        var pressed by remember { mutableStateOf(false) }

        val buttonScale by animateFloatAsState(
            targetValue = if (pressed) 0.94f else 1f,
            animationSpec = tween(150),
            label = "buttonScale"
        )

        val buttonGradientShift by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 600f,
            animationSpec = infiniteRepeatable(
                animation = tween(6000, easing = LinearEasing)
            ),
            label = "buttonGradient"
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

    // Toast mesajı
    showToast?.let { message ->
        LaunchedEffect(message) {
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
            delay(2000)
            showToast = null
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