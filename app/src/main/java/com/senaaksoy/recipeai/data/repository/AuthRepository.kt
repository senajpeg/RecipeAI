package com.senaaksoy.recipeai.data.repository


import com.senaaksoy.recipeai.data.remote.api.AuthApi
import com.senaaksoy.recipeai.data.remote.dto.ForgotPasswordRequest
import com.senaaksoy.recipeai.data.remote.dto.GoogleSignInRequest
import com.senaaksoy.recipeai.data.remote.dto.LoginRequest
import com.senaaksoy.recipeai.data.remote.dto.RegisterRequest
import com.senaaksoy.recipeai.data.remote.dto.ResetPasswordRequest
import com.senaaksoy.recipeai.domain.model.User
import com.senaaksoy.recipeai.utills.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi
) {

    suspend fun register(name: String, email: String, password: String): Resource<User> {
        return withContext(Dispatchers.IO) {
            try {
                val request = RegisterRequest(name, email, password)
                val response = authApi.register(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        val user = User(
                            id = authResponse.user.id,
                            name = authResponse.user.name,
                            email = authResponse.user.email,
                            createdAt = authResponse.user.createdAt
                        )
                        Resource.Success(user, authResponse.token)
                    } else {
                        Resource.Error("Yanıt boş")
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Bilinmeyen hata"
                    Resource.Error(errorMessage)
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Bağlantı hatası")
            }
        }
    }

    suspend fun login(email: String, password: String): Resource<User> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email, password)
                val response = authApi.login(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        val user = User(
                            id = authResponse.user.id,
                            name = authResponse.user.name,
                            email = authResponse.user.email,
                            createdAt = authResponse.user.createdAt
                        )
                        Resource.Success(user, authResponse.token)
                    } else {
                        Resource.Error("Yanıt boş")
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Bilinmeyen hata"
                    Resource.Error(errorMessage)
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Bağlantı hatası")
            }
        }
    }

    // ⭐ YENİ: Şifre sıfırlama isteği
    suspend fun forgotPassword(email: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ForgotPasswordRequest(email)
                val response = authApi.forgotPassword(request)

                if (response.isSuccessful) {
                    val messageResponse = response.body()
                    if (messageResponse != null) {
                        Resource.Success(messageResponse.message)
                    } else {
                        Resource.Error("Yanıt boş")
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Email gönderilemedi"
                    Resource.Error(errorMessage)
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Bağlantı hatası")
            }
        }
    }

    // ⭐ YENİ: Şifre sıfırlama (token ile)
    suspend fun resetPassword(token: String, password: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ResetPasswordRequest(password)
                val response = authApi.resetPassword(token, request)

                if (response.isSuccessful) {
                    val messageResponse = response.body()
                    if (messageResponse != null) {
                        Resource.Success(messageResponse.message)
                    } else {
                        Resource.Error("Yanıt boş")
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Şifre sıfırlanamadı"
                    Resource.Error(errorMessage)
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Bağlantı hatası")
            }
        }
    }
    // ⭐ YENİ: Google Sign-In
    suspend fun googleSignIn(idToken: String): Resource<User> {
        return withContext(Dispatchers.IO) {
            try {
                val request = GoogleSignInRequest(idToken)
                val response = authApi.googleSignIn(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        val user = User(
                            id = authResponse.user.id,
                            name = authResponse.user.name,
                            email = authResponse.user.email,
                            createdAt = authResponse.user.createdAt
                        )
                        Resource.Success(user, authResponse.token)
                    } else {
                        Resource.Error("Yanıt boş")
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Google ile giriş başarısız"
                    Resource.Error(errorMessage)
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Bağlantı hatası")
            }
        }
    }
}