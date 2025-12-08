package com.senaaksoy.recipeai.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ForgotPasswordRequest(
    val email: String
)

data class GenerateRecipeRequest(
    @SerializedName("ingredients")
    val ingredients: List<String>
)
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)
data class ResetPasswordRequest(
    val password: String
)
//Google Sign-In Request
data class GoogleSignInRequest(
    val idToken: String
)
data class UpdateProfilePictureRequest(
    val profilePicture: String // Base64 string
)
// ✅ Backend'e gönderilecek request modeli
data class AddFavoriteRequest(
    val id: Int,
    val name: String,
    val description: String?,
    val instructions: String,
    val cookingTime: Int?,
    val difficulty: String?,
    val imageUrl: String?,
    val ingredients: List<String>? = null

)
