package com.senaaksoy.recipeai.presentation.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senaaksoy.recipeai.domain.model.User
import com.senaaksoy.recipeai.utills.Resource
import com.senaaksoy.recipeai.utills.TokenManager
import com.yourname.recipeai.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // Register State
    private val _registerState = MutableStateFlow<Resource<User>?>(null)
    val registerState: StateFlow<Resource<User>?> = _registerState

    // Login State
    private val _loginState = MutableStateFlow<Resource<User>?>(null)
    val loginState: StateFlow<Resource<User>?> = _loginState

    // SignUp Form States
    var signUpName by mutableStateOf("")
        private set
    var signUpEmail by mutableStateOf("")
        private set
    var signUpPassword by mutableStateOf("")
        private set
    var signUpPasswordVisible by mutableStateOf(false)
        private set

    // SignUp Validation Errors
    var signUpNameError by mutableStateOf("")
        private set
    var signUpEmailError by mutableStateOf("")
        private set
    var signUpPasswordError by mutableStateOf("")
        private set

    // SignIn Form States
    var signInEmail by mutableStateOf("")
        private set
    var signInPassword by mutableStateOf("")
        private set
    var signInPasswordVisible by mutableStateOf(false)
        private set

    // SignIn Validation Errors
    var signInEmailError by mutableStateOf("")
        private set
    var signInPasswordError by mutableStateOf("")
        private set

    // SignUp Functions
    fun updateSignUpName(name: String) {
        signUpName = name
        if (signUpNameError.isNotEmpty()) {
            signUpNameError = ""
        }
    }

    fun updateSignUpEmail(email: String) {
        signUpEmail = email
        if (signUpEmailError.isNotEmpty()) {
            signUpEmailError = ""
        }
    }

    fun updateSignUpPassword(password: String) {
        signUpPassword = password
        if (signUpPasswordError.isNotEmpty()) {
            signUpPasswordError = ""
        }
    }

    fun toggleSignUpPasswordVisibility() {
        signUpPasswordVisible = !signUpPasswordVisible
    }

    private fun validateSignUpInputs(): Boolean {
        var isValid = true

        // Name validation
        if (signUpName.isBlank()) {
            signUpNameError = "Ad soyad boş olamaz"
            isValid = false
        } else if (signUpName.length < 3) {
            signUpNameError = "Ad soyad en az 3 karakter olmalı"
            isValid = false
        }

        // Email validation
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        if (signUpEmail.isBlank()) {
            signUpEmailError = "Email boş olamaz"
            isValid = false
        } else if (!signUpEmail.matches(emailRegex)) {
            signUpEmailError = "Geçersiz email formatı"
            isValid = false
        }

        // Password validation
        if (signUpPassword.isBlank()) {
            signUpPasswordError = "Şifre boş olamaz"
            isValid = false
        } else if (signUpPassword.length < 6) {
            signUpPasswordError = "Şifre en az 6 karakter olmalı"
            isValid = false
        }

        return isValid
    }

    fun performSignUp() {
        if (validateSignUpInputs()) {
            register(signUpName, signUpEmail, signUpPassword)
        }
    }

    // SignIn Functions
    fun updateSignInEmail(email: String) {
        signInEmail = email
        if (signInEmailError.isNotEmpty()) {
            signInEmailError = ""
        }
    }

    fun updateSignInPassword(password: String) {
        signInPassword = password
        if (signInPasswordError.isNotEmpty()) {
            signInPasswordError = ""
        }
    }

    fun toggleSignInPasswordVisibility() {
        signInPasswordVisible = !signInPasswordVisible
    }

    private fun validateSignInInputs(): Boolean {
        var isValid = true

        // Email validation
        if (signInEmail.isBlank()) {
            signInEmailError = "Email boş olamaz"
            isValid = false
        }

        // Password validation
        if (signInPassword.isBlank()) {
            signInPasswordError = "Şifre boş olamaz"
            isValid = false
        }

        return isValid
    }

    fun performSignIn() {
        if (validateSignInInputs()) {
            login(signInEmail, signInPassword)
        }
    }

    // Helper Functions (UI Support)
    fun signUpEmailSupportText() = signUpEmailError.isNotEmpty() && signUpEmail.isNotBlank()
    fun signUpPasswordSupportText() = signUpPasswordError.isNotEmpty() && signUpPassword.isNotBlank()
    fun signUpNameSupportText() = signUpNameError.isNotEmpty() && signUpName.isNotBlank()

    fun signInEmailSupportText() = signInEmailError.isNotEmpty() && signInEmail.isNotBlank()
    fun signInPasswordSupportText() = signInPasswordError.isNotEmpty() && signInPassword.isNotBlank()

    // Core Auth Functions
    private fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()

            val result = repository.register(name, email, password)

            if (result is Resource.Success) {
                result.token?.let { tokenManager.saveToken(it) }
                result.data?.let { user ->
                    tokenManager.saveUser(user.id, user.name, user.email)
                }
            }

            _registerState.value = result
        }
    }

    private fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()

            val result = repository.login(email, password)

            if (result is Resource.Success) {
                result.token?.let { tokenManager.saveToken(it) }
                result.data?.let { user ->
                    tokenManager.saveUser(user.id, user.name, user.email)
                }
            }

            _loginState.value = result
        }
    }

    fun logout() {
        tokenManager.clearAll()
        clearAllStates()
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    fun resetRegisterState() {
        _registerState.value = null
    }

    fun resetLoginState() {
        _loginState.value = null
    }

    private fun clearAllStates() {
        // Clear SignUp states
        signUpName = ""
        signUpEmail = ""
        signUpPassword = ""
        signUpPasswordVisible = false
        signUpNameError = ""
        signUpEmailError = ""
        signUpPasswordError = ""

        // Clear SignIn states
        signInEmail = ""
        signInPassword = ""
        signInPasswordVisible = false
        signInEmailError = ""
        signInPasswordError = ""
    }
}