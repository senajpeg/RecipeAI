package com.senaaksoy.recipeai.presentation.viewmodel


import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.senaaksoy.recipeai.data.remote.dto.MessageResponse
import com.senaaksoy.recipeai.data.remote.dto.UserProfileResponse
import com.senaaksoy.recipeai.domain.model.User
import com.senaaksoy.recipeai.utills.Resource
import com.senaaksoy.recipeai.utills.TokenManager
import com.senaaksoy.recipeai.data.repository.AuthRepository
import com.senaaksoy.recipeai.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val recipeRepository: RecipeRepository,
    private val tokenManager: TokenManager,
    private val workManager: WorkManager

) : ViewModel() {

    // States
    private val _registerState = MutableStateFlow<Resource<User>?>(null)
    val registerState: StateFlow<Resource<User>?> = _registerState

    private val _loginState = MutableStateFlow<Resource<User>?>(null)
    val loginState: StateFlow<Resource<User>?> = _loginState

    private val _googleSignInState = MutableStateFlow<Resource<User>?>(null)
    val googleSignInState: StateFlow<Resource<User>?> = _googleSignInState

    private val _forgotPasswordState = MutableStateFlow<Resource<String>?>(null)
    val forgotPasswordState: StateFlow<Resource<String>?> = _forgotPasswordState

    private val _resetPasswordState = MutableStateFlow<Resource<String>?>(null)
    val resetPasswordState: StateFlow<Resource<String>?> = _resetPasswordState

    private val _profilePictureState = MutableStateFlow<Resource<MessageResponse>?>(null)
    val profilePictureState: StateFlow<Resource<MessageResponse>?> = _profilePictureState

    private val _userProfile = MutableStateFlow<UserProfileResponse?>(null)
    val userProfile: StateFlow<UserProfileResponse?> = _userProfile

    // Forgot Password
    var forgotPasswordEmail by mutableStateOf("")
        private set
    var forgotPasswordEmailError by mutableStateOf("")
        private set

    // Reset Password
    var resetPasswordNewPassword by mutableStateOf("")
        private set
    var resetPasswordConfirmPassword by mutableStateOf("")
        private set
    var resetPasswordVisible by mutableStateOf(false)
        private set
    var resetPasswordConfirmVisible by mutableStateOf(false)
        private set
    var resetPasswordError by mutableStateOf("")
        private set
    var resetPasswordConfirmError by mutableStateOf("")
        private set

    // SignUp
    var signUpName by mutableStateOf("")
        private set
    var signUpEmail by mutableStateOf("")
        private set
    var signUpPassword by mutableStateOf("")
        private set
    var signUpPasswordVisible by mutableStateOf(false)
        private set
    var signUpNameError by mutableStateOf("")
        private set
    var signUpEmailError by mutableStateOf("")
        private set
    var signUpPasswordError by mutableStateOf("")
        private set

    // SignIn
    var signInEmail by mutableStateOf("")
        private set
    var signInPassword by mutableStateOf("")
        private set
    var signInPasswordVisible by mutableStateOf(false)
        private set
    var signInEmailError by mutableStateOf("")
        private set
    var signInPasswordError by mutableStateOf("")
        private set

    // ========== VALIDATION HELPERS ==========

    private companion object {
        val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        const val MIN_PASSWORD_LENGTH = 6
        const val MIN_NAME_LENGTH = 3
    }

    private fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Email boş olamaz"
        !email.matches(EMAIL_REGEX) -> "Geçersiz email formatı"
        else -> null
    }

    private fun validatePassword(password: String): String? = when {
        password.isBlank() -> "Şifre boş olamaz"
        password.length < MIN_PASSWORD_LENGTH -> "Şifre en az $MIN_PASSWORD_LENGTH karakter olmalı"
        else -> null
    }

    private fun validateName(name: String): String? = when {
        name.isBlank() -> "Ad soyad boş olamaz"
        name.length < MIN_NAME_LENGTH -> "Ad soyad en az $MIN_NAME_LENGTH karakter olmalı"
        else -> null
    }

    private fun shouldShowError(error: String, value: String) =
        error.isNotEmpty() && value.isNotBlank()

    // ========== AUTH SUCCESS HANDLER ==========

    private fun handleAuthSuccess(result: Resource.Success<User>) {

        val token = result.token

        Log.d("TOKEN_DEBUG", "GELEN TOKEN: $token")

        token?.let {
            tokenManager.saveToken(it)
            Log.d("TOKEN_KAYDEDILDI", it)
            Log.d("TOKEN_OKUNAN", tokenManager.getToken().toString())
        }

        result.data?.let { user ->
            tokenManager.saveUser(user.id, user.name, user.email)
        }
    }


    // ========== FORGOT PASSWORD ==========

    fun updateForgotPasswordEmail(email: String) {
        forgotPasswordEmail = email
        forgotPasswordEmailError = ""
    }

    fun performForgotPassword() {
        validateEmail(forgotPasswordEmail)?.let { error ->
            forgotPasswordEmailError = error
            return
        }

        viewModelScope.launch {
            _forgotPasswordState.value = Resource.Loading()
            _forgotPasswordState.value = repository.forgotPassword(forgotPasswordEmail)
        }
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = null
        forgotPasswordEmail = ""
        forgotPasswordEmailError = ""
    }

    fun forgotPasswordEmailSupportText() =
        shouldShowError(forgotPasswordEmailError, forgotPasswordEmail)

    // ========== RESET PASSWORD ==========

    fun updateResetPasswordNewPassword(password: String) {
        resetPasswordNewPassword = password
        resetPasswordError = ""
    }

    fun updateResetPasswordConfirmPassword(password: String) {
        resetPasswordConfirmPassword = password
        resetPasswordConfirmError = ""
    }

    fun toggleResetPasswordVisibility() {
        resetPasswordVisible = !resetPasswordVisible
    }

    fun toggleResetPasswordConfirmVisibility() {
        resetPasswordConfirmVisible = !resetPasswordConfirmVisible
    }

    fun performResetPassword(token: String) {
        var isValid = true

        validatePassword(resetPasswordNewPassword)?.let { error ->
            resetPasswordError = error
            isValid = false
        }

        if (resetPasswordConfirmPassword.isBlank()) {
            resetPasswordConfirmError = "Şifre tekrarı boş olamaz"
            isValid = false
        } else if (resetPasswordNewPassword != resetPasswordConfirmPassword) {
            resetPasswordConfirmError = "Şifreler eşleşmiyor"
            isValid = false
        }

        if (!isValid) return

        viewModelScope.launch {
            _resetPasswordState.value = Resource.Loading()
            _resetPasswordState.value = repository.resetPassword(token, resetPasswordNewPassword)
        }
    }

    fun resetResetPasswordState() {
        _resetPasswordState.value = null
        resetPasswordNewPassword = ""
        resetPasswordConfirmPassword = ""
        resetPasswordVisible = false
        resetPasswordConfirmVisible = false
        resetPasswordError = ""
        resetPasswordConfirmError = ""
    }

    fun resetPasswordSupportText() =
        shouldShowError(resetPasswordError, resetPasswordNewPassword)

    fun resetPasswordConfirmSupportText() =
        shouldShowError(resetPasswordConfirmError, resetPasswordConfirmPassword)

    // ========== SIGN UP ==========

    fun updateSignUpName(name: String) {
        signUpName = name
        signUpNameError = ""
    }

    fun updateSignUpEmail(email: String) {
        signUpEmail = email
        signUpEmailError = ""
    }

    fun updateSignUpPassword(password: String) {
        signUpPassword = password
        signUpPasswordError = ""
    }

    fun toggleSignUpPasswordVisibility() {
        signUpPasswordVisible = !signUpPasswordVisible
    }

    fun performSignUp() {
        var isValid = true

        validateName(signUpName)?.let { error ->
            signUpNameError = error
            isValid = false
        }

        validateEmail(signUpEmail)?.let { error ->
            signUpEmailError = error
            isValid = false
        }

        validatePassword(signUpPassword)?.let { error ->
            signUpPasswordError = error
            isValid = false
        }

        if (isValid) {
            register(signUpName, signUpEmail, signUpPassword)
        }
    }

    fun signUpNameSupportText() = shouldShowError(signUpNameError, signUpName)
    fun signUpEmailSupportText() = shouldShowError(signUpEmailError, signUpEmail)
    fun signUpPasswordSupportText() = shouldShowError(signUpPasswordError, signUpPassword)

    // ========== SIGN IN ==========

    fun updateSignInEmail(email: String) {
        signInEmail = email
        signInEmailError = ""
    }

    fun updateSignInPassword(password: String) {
        signInPassword = password
        signInPasswordError = ""
    }

    fun toggleSignInPasswordVisibility() {
        signInPasswordVisible = !signInPasswordVisible
    }

    fun performSignIn() {
        var isValid = true

        if (signInEmail.isBlank()) {
            signInEmailError = "Email boş olamaz"
            isValid = false
        }

        if (signInPassword.isBlank()) {
            signInPasswordError = "Şifre boş olamaz"
            isValid = false
        }

        if (isValid) {
            login(signInEmail, signInPassword)
        }
    }

    fun signInEmailSupportText() = shouldShowError(signInEmailError, signInEmail)
    fun signInPasswordSupportText() = shouldShowError(signInPasswordError, signInPassword)

    // ========== CORE AUTH ==========

    private fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            val result = repository.register(name, email, password)

            if (result is Resource.Success) {
                handleAuthSuccess(result)
            }

            _registerState.value = result
        }
    }

    private fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val result = repository.login(email, password)

            if (result is Resource.Success) {
                handleAuthSuccess(result)
            }

            _loginState.value = result
        }
    }

    fun googleSignIn(idToken: String) {
        viewModelScope.launch {
            _googleSignInState.value = Resource.Loading()
            val result = repository.googleSignIn(idToken)

            if (result is Resource.Success) {
                handleAuthSuccess(result)
            }

            _googleSignInState.value = result
        }
    }

    fun logout() {
        viewModelScope.launch {

            // 3. Sync İşlemlerini İptal Et
            workManager.cancelAllWork()
            Log.d("AuthViewModel", "🔴 Çıkış yapıldı ve temizlik tamamlandı.")

            // 1. Veritabanını temizle
            recipeRepository.clearLocalData()

            // 2. Token ve State'leri temizle
            tokenManager.clearAll()
            clearAllStates()


        }
    }

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    //Helper

    fun updateProfilePicture(base64Image: String) {
        viewModelScope.launch {
            _profilePictureState.value = Resource.Loading()
            _profilePictureState.value = repository.updateProfilePicture(base64Image)
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            when (val result = repository.getUserProfile()) {
                is Resource.Success -> {
                    _userProfile.value = result.data
                }

                is Resource.Error -> {
                }

                is Resource.Loading -> {}
            }
        }
    }


    fun getUserName(): String {
        return _userProfile.value?.name ?: tokenManager.getUserName() ?: "Kullanıcı"
    }

    fun getUserEmail(): String {
        return _userProfile.value?.email ?: tokenManager.getUserEmail() ?: "example@email.com"
    }


    // STATE RESET

    fun resetRegisterState() {
        _registerState.value = null
    }

    fun resetLoginState() {
        _loginState.value = null
    }

    fun resetGoogleSignInState() {
        _googleSignInState.value = null
    }

    private fun clearAllStates() {
        // SignUp
        signUpName = ""
        signUpEmail = ""
        signUpPassword = ""
        signUpPasswordVisible = false
        signUpNameError = ""
        signUpEmailError = ""
        signUpPasswordError = ""

        // SignIn
        signInEmail = ""
        signInPassword = ""
        signInPasswordVisible = false
        signInEmailError = ""
        signInPasswordError = ""

        // Forgot Password
        forgotPasswordEmail = ""
        forgotPasswordEmailError = ""

        // Reset Password
        resetPasswordNewPassword = ""
        resetPasswordConfirmPassword = ""
        resetPasswordVisible = false
        resetPasswordConfirmVisible = false
        resetPasswordError = ""
        resetPasswordConfirmError = ""
    }
}