package com.senaaksoy.recipeai.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    val message: String,
    val user: UserDto,
    val token: String
)

data class MessageResponse(
    val message: String
)
data class UserProfileResponse(
    val id: Int,
    val name: String,
    val email: String,

    @SerializedName("profile_picture")
    val profile_picture: String?,

    @SerializedName("created_at")
    val created_at: String?,

    @SerializedName("is_verified")
    val is_verified: Boolean?
)