package com.senaaksoy.recipeai.presentation.screens.auth


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.senaaksoy.recipeai.R
import com.senaaksoy.recipeai.components.EditTextField
import com.senaaksoy.recipeai.presentation.viewmodel.AuthViewModel
import com.senaaksoy.recipeai.utills.Resource

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val forgotPasswordState by viewModel.forgotPasswordState.collectAsState()

    // Forgot password state observer
    LaunchedEffect(forgotPasswordState) {
        when (forgotPasswordState) {
            is Resource.Success -> {
                Toast.makeText(
                    context,
                    (forgotPasswordState as Resource.Success).data ?: context.getString(R.string.forgot_password_success),
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetForgotPasswordState()
                navController.popBackStack()
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    (forgotPasswordState as Resource.Error).message ?: context.getString(R.string.forgot_password_error),
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetForgotPasswordState()
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
                        Color(0xFF5D10A2),
                        Color(0xFF6257E7)
                    )
                )
            )
    ) {
        // Back button
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Geri",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Text(
                text = "🔐",
                fontSize = 80.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Title
            Text(
                text = stringResource(R.string.forgot_password_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Description
            Text(
                text = stringResource(R.string.forgot_password_description),
                fontSize = 14.sp,
                color = Color(0xFFaea0e4),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .widthIn(max = 300.dp)
            )

            // Email
            EditTextField(
                value = viewModel.forgotPasswordEmail,
                onValueChange = { viewModel.updateForgotPasswordEmail(it) },
                label = R.string.email,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Email
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color(0xFFcfccf0)
                    )
                },
                isError = viewModel.forgotPasswordEmailSupportText(),
                supportingText = if (viewModel.forgotPasswordEmailSupportText()) {
                    { Text(viewModel.forgotPasswordEmailError) }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(focusedLabelColor = Color.White)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Send button
            Button(
                onClick = { viewModel.performForgotPassword() },
                enabled = forgotPasswordState !is Resource.Loading,
                modifier = Modifier
                    .widthIn(max = 350.dp)
                    .fillMaxWidth()
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
                if (forgotPasswordState is Resource.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.height(24.dp)
                    )
                } else {
                    Text(text = stringResource(R.string.forgot_password_button))
                }
            }
        }
    }
}