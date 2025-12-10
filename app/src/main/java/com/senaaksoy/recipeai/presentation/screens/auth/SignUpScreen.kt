package com.senaaksoy.recipeai.presentation.screens.auth

import android.widget.Toast
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.senaaksoy.recipeai.R
import com.senaaksoy.recipeai.components.EditTextField
import com.senaaksoy.recipeai.navigation.Screen
import com.senaaksoy.recipeai.presentation.viewmodel.AuthViewModel
import com.senaaksoy.recipeai.utills.Resource

@Composable
fun SignUpScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val registerState by authViewModel.registerState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(registerState) {
        when (registerState) {
            is Resource.Success -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.toast_register_success),
                    Toast.LENGTH_LONG
                ).show()
                authViewModel.resetRegisterState()
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    registerState?.message ?: context.getString(R.string.toast_register_failed),
                    Toast.LENGTH_LONG
                ).show()
                authViewModel.resetRegisterState()
            }
            else -> Unit
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF87CEF3),
                        Color(0xFF6F7DD7)
                    ),
                )
            )
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.recipeai_logo),
                tint = Color.Unspecified,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            EditTextField(
                value = authViewModel.signUpName,
                onValueChange = { authViewModel.updateSignUpName(it) },
                label = R.string.ad_soyad,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Text
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFFcfccf0)
                    )
                },
                isError = authViewModel.signUpNameSupportText(),
                colors = OutlinedTextFieldDefaults.colors(focusedLabelColor = Color.White),
                supportingText = if (authViewModel.signUpNameSupportText()) {
                    { Text(authViewModel.signUpNameError, color = Color.Red) }
                } else null
            )

            EditTextField(
                value = authViewModel.signUpEmail,
                onValueChange = { authViewModel.updateSignUpEmail(it) },
                label = R.string.email,
                keyboardOptions = KeyboardOptions.Default.copy(
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
                isError = authViewModel.signUpEmailSupportText(),
                colors = OutlinedTextFieldDefaults.colors(focusedLabelColor = Color.White),
                supportingText = if (authViewModel.signUpEmailSupportText()) {
                    { Text(authViewModel.signUpEmailError, color = Color.Red) }
                } else null
            )

            EditTextField(
                value = authViewModel.signUpPassword,
                onValueChange = { authViewModel.updateSignUpPassword(it) },
                label = R.string.sifre,
                keyboardOptions = KeyboardOptions.Default.copy(
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
                    IconButton(onClick = { authViewModel.toggleSignUpPasswordVisibility() }) {
                        Icon(
                            imageVector = if (authViewModel.signUpPasswordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (authViewModel.signUpPasswordVisible)
                                "Şifreyi gizle"
                            else
                                "Şifreyi göster"
                        )
                    }
                },
                visualTransformation = if (authViewModel.signUpPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(focusedLabelColor = Color.White),
                isError = authViewModel.signUpPasswordSupportText(),
                supportingText = if (authViewModel.signUpPasswordSupportText()) {
                    { Text(authViewModel.signUpPasswordError, color = Color.Red) }
                } else null
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { authViewModel.performSignUp() },
                enabled = registerState !is Resource.Loading,
                modifier = modifier
                    .widthIn(max = 300.dp)
                    .fillMaxWidth(0.6f)
                    .clip(shape = RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF3C7EB4),
                                Color(0xFF5E8BCB)
                            )
                        )
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
            ) {
                if (registerState is Resource.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    Text(text = stringResource(R.string.kaydol))
                }
            }

            TextButton(
                onClick = { navController.navigate(Screen.SignInScreen.route) }
            ) {
                Text(
                    text = stringResource(R.string.already_have_account),
                    color = Color(0xFFaea0e4),
                )
            }
        }
    }
}

@Preview
@Composable
fun SignUpPreview() {
    val navController = rememberNavController()
    SignUpScreen(navController = navController)
}