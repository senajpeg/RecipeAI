package com.senaaksoy.recipeai.data.repository

import com.senaaksoy.recipeai.data.remote.api.AuthApi
import com.senaaksoy.recipeai.data.remote.dto.*
import com.senaaksoy.recipeai.domain.model.User
import com.senaaksoy.recipeai.utills.Resource
import com.senaaksoy.recipeai.utills.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                val result = apiCall()
                Resource.Success(result)
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Bağlantı hatası")
            }
        }
    }

    suspend fun register(name: String, email: String, password: String): Resource<User> =
        safeApiCall {
            val response = authApi.register(RegisterRequest(name, email, password))
            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Yanıt boş")
                User(
                    id = body.user.id,
                    name = body.user.name,
                    email = body.user.email,
                    createdAt = body.user.createdAt
                )
            } else {
                throw Exception(response.errorBody()?.string() ?: "Bilinmeyen hata")
            }
        }

    suspend fun login(email: String, password: String): Resource<User> =
        safeApiCall {
            val response = authApi.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Yanıt boş")
                User(
                    id = body.user.id,
                    name = body.user.name,
                    email = body.user.email,
                    createdAt = body.user.createdAt
                )
            } else {
                throw Exception(response.errorBody()?.string() ?: "Bilinmeyen hata")
            }
        }

    suspend fun forgotPassword(email: String): Resource<String> =
        safeApiCall {
            val response = authApi.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                response.body()?.message ?: throw Exception("Yanıt boş")
            } else {
                throw Exception(response.errorBody()?.string() ?: "Email gönderilemedi")
            }
        }

    suspend fun resetPassword(token: String, password: String): Resource<String> =
        safeApiCall {
            val response = authApi.resetPassword(token, ResetPasswordRequest(password))
            if (response.isSuccessful) {
                response.body()?.message ?: throw Exception("Yanıt boş")
            } else {
                throw Exception(response.errorBody()?.string() ?: "Şifre sıfırlanamadı")
            }
        }

    suspend fun googleSignIn(idToken: String): Resource<User> =
        safeApiCall {
            val response = authApi.googleSignIn(GoogleSignInRequest(idToken))
            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Yanıt boş")
                User(
                    id = body.user.id,
                    name = body.user.name,
                    email = body.user.email,
                    createdAt = body.user.createdAt
                )
            } else {
                throw Exception(response.errorBody()?.string() ?: "Google ile giriş başarısız")
            }
        }

    suspend fun updateProfilePicture(base64Image: String): Resource<MessageResponse> =
        safeApiCall {
            val token = tokenManager.getToken() ?: throw Exception("Token bulunamadı")
            val response = authApi.updateProfilePicture(
                token = "Bearer $token",
                request = UpdateProfilePictureRequest(base64Image)
            )
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception(response.message() ?: "Bilinmeyen hata")
            }
        }

    suspend fun getUserProfile(): Resource<UserProfileResponse> =
        safeApiCall {
            val token = tokenManager.getToken() ?: throw Exception("Token bulunamadı")
            val response = authApi.getUserProfile("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception(response.message() ?: "Bilinmeyen hata")
            }
        }


}
