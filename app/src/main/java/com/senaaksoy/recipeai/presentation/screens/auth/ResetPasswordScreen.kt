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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.senaaksoy.recipeai.R
import com.senaaksoy.recipeai.components.EditTextField
import com.senaaksoy.recipeai.navigation.Screen
import com.senaaksoy.recipeai.presentation.viewmodel.AuthViewModel
import com.senaaksoy.recipeai.utills.Resource

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    token: String,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val resetPasswordState by viewModel.resetPasswordState.collectAsState()

    // Reset password state observer
    LaunchedEffect(resetPasswordState) {
        when (resetPasswordState) {
            is Resource.Success -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.reset_password_success),
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetResetPasswordState()
                navController.navigate(Screen.SignInScreen.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    (resetPasswordState as Resource.Error).message ?: context.getString(R.string.reset_password_error),
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetResetPasswordState()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Text(
                text = stringResource(R.string.lock),
                fontSize = 80.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Title
            Text(
                text = stringResource(R.string.reset_password_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Description
            Text(
                text = stringResource(R.string.reset_password_description),
                fontSize = 14.sp,
                color = Color(0xFFaea0e4),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // New Password
            EditTextField(
                value = viewModel.resetPasswordNewPassword,
                onValueChange = { viewModel.updateResetPasswordNewPassword(it) },
                label = R.string.yeni_sifre,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
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
                    IconButton(onClick = { viewModel.toggleResetPasswordVisibility() }) {
                        Icon(
                            imageVector = if (viewModel.resetPasswordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (viewModel.resetPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                isError = viewModel.resetPasswordSupportText(),
                supportingText = if (viewModel.resetPasswordSupportText()) {
                    { Text(viewModel.resetPasswordError) }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(focusedLabelColor = Color.White)
            )

            // Confirm Password
            EditTextField(
                value = viewModel.resetPasswordConfirmPassword,
                onValueChange = { viewModel.updateResetPasswordConfirmPassword(it) },
                label = R.string.yeni_sifre_tekrar,
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
                    IconButton(onClick = { viewModel.toggleResetPasswordConfirmVisibility() }) {
                        Icon(
                            imageVector = if (viewModel.resetPasswordConfirmVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (viewModel.resetPasswordConfirmVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                isError = viewModel.resetPasswordConfirmSupportText(),
                supportingText = if (viewModel.resetPasswordConfirmSupportText()) {
                    { Text(viewModel.resetPasswordConfirmError) }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(focusedLabelColor = Color.White)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Reset button
            Button(
                onClick = { viewModel.performResetPassword(token) },
                enabled = resetPasswordState !is Resource.Loading,
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
                if (resetPasswordState is Resource.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.height(24.dp)
                    )
                } else {
                    Text(text = stringResource(R.string.reset_password_button))
                }
            }
        }
    }
}

