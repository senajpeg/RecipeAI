package com.senaaksoy.recipeai.presentation.screens.auth

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.senaaksoy.recipeai.R
import com.senaaksoy.recipeai.components.EditTextField
import com.senaaksoy.recipeai.navigation.Screen
import com.senaaksoy.recipeai.utills.Resource

@Composable
fun SignInScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val loginState by viewModel.loginState.collectAsState()
    val googleSignInState by viewModel.googleSignInState.collectAsState()


    //Google Sign-In Client
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    //Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { idToken ->
                    viewModel.googleSignIn(idToken)
                } ?: run {
                    Toast.makeText(context, context.getString(R.string.toast_google_token_failed), Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(context, context.getString(R.string.toast_google_signin_failed, e.message ?: ""), Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("GoogleSignIn", "❌ Result code başarısız: ${result.resultCode}")
        }
    }

    // Login & Google state observer
    LaunchedEffect(loginState, googleSignInState) {
        when (loginState) {
            is Resource.Success -> {
                Toast.makeText(context, context.getString(R.string.toast_login_success), Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.HomeScreen.route) {
                    popUpTo(Screen.SignInScreen.route) { inclusive = true }
                }
                viewModel.resetLoginState()
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    (loginState as Resource.Error).message ?: context.getString(R.string.toast_login_failed),
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetLoginState()
            }
            else -> {}
        }

        // ⭐ Google Sign-In State
        when (googleSignInState) {
            is Resource.Success -> {
                Toast.makeText(context, context.getString(R.string.toast_google_success), Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.HomeScreen.route) {
                    popUpTo(Screen.SignInScreen.route) { inclusive = true }
                }
                viewModel.resetGoogleSignInState()
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    (googleSignInState as Resource.Error).message ?: context.getString(R.string.toast_google_failed),
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetGoogleSignInState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF87CEF3),
                        Color(0xFF6F7DD7)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Logo
            Icon(
                painter = painterResource(R.drawable.recipeai_logo),
                tint = Color.Unspecified,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            // Email
            EditTextField(
                value = viewModel.signInEmail,
                onValueChange = { viewModel.updateSignInEmail(it) },
                label = R.string.email,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color(0xFFcfccf0)
                    )
                },
                isError = viewModel.signInEmailSupportText(),
                supportingText = if (viewModel.signInEmailSupportText()) {
                    { Text(viewModel.signInEmailError) }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(focusedLabelColor = Color.White)
            )

            // Password
            EditTextField(
                value = viewModel.signInPassword,
                onValueChange = { viewModel.updateSignInPassword(it) },
                label = R.string.sifre,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFcfccf0)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { viewModel.toggleSignInPasswordVisibility() }) {
                        Icon(
                            imageVector = if (viewModel.signInPasswordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (viewModel.signInPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                isError = viewModel.signInPasswordSupportText(),
                supportingText = if (viewModel.signInPasswordSupportText()) {
                    { Text(viewModel.signInPasswordError) }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(focusedLabelColor = Color.White)
            )

            // Forgot password
            Text(
                text = stringResource(R.string.sifremi_unuttum),
                color = Color(0xFFaea0e4),
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(Screen.ForgotPasswordScreen.route)
                    },
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login button
            Button(
                onClick = { viewModel.performSignIn() },
                enabled = loginState !is Resource.Loading,
                modifier = Modifier
                    .widthIn(max = 350.dp)
                    .fillMaxWidth(0.6f)
                    .clip(shape = RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFA065E3),
                                Color(0xFF5E8BCB)
                            )
                        )
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                if (loginState is Resource.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.height(24.dp)
                    )
                } else {
                    Text(text = stringResource(R.string.giris_yap))
                }
            }

            // Google button
            Button(
                onClick = {
                    Log.d("GoogleSignIn", "🔵 Google butonu tıklandı")

                    // Google Sign-In öncesi logout
                    googleSignInClient.signOut().addOnCompleteListener {
                        Log.d("GoogleSignIn", "Çıkış yapıldı, şimdi sign-in intent başlatılıyor")
                        val signInIntent = googleSignInClient.signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    }
                },
                modifier = Modifier
                    .widthIn(max = 350.dp)
                    .fillMaxWidth(0.6f)
                    .clip(shape = RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.google),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .width(18.dp)
                        .height(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.google_ile_devam_et))
            }

            // Sign up link
            TextButton(onClick = {
                navController.navigate(Screen.SignUpScreen.route) {
                    popUpTo(Screen.SignInScreen.route) { inclusive = true }
                }
            }) {
                Text(
                    text = stringResource(R.string.have_an_account_sign_up),
                    color = Color(0xFFaea0e4)
                )
            }
        }
    }
}