package com.senaaksoy.recipeai.data.remote.api

import com.senaaksoy.recipeai.data.remote.dto.AuthResponse
import com.senaaksoy.recipeai.data.remote.dto.ForgotPasswordRequest
import com.senaaksoy.recipeai.data.remote.dto.GoogleSignInRequest
import com.senaaksoy.recipeai.data.remote.dto.LoginRequest
import com.senaaksoy.recipeai.data.remote.dto.MessageResponse
import com.senaaksoy.recipeai.data.remote.dto.RegisterRequest
import com.senaaksoy.recipeai.data.remote.dto.ResetPasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path


interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    //  Şifre sıfırlama isteği
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>

    //  Şifre sıfırlama (token ile)
    @POST("auth/reset-password/{token}")
    suspend fun resetPassword(
        @Path("token") token: String,
        @Body request: ResetPasswordRequest
    ): Response<MessageResponse>
    @POST("auth/google-signin")
    suspend fun googleSignIn(@Body request: GoogleSignInRequest): Response<AuthResponse>
}