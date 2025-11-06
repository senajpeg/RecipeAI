package com.senaaksoy.recipeai.data.remote.dto

data class AuthResponse(
    val message: String,
    val user: UserDto,
    val token: String
)

data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val createdAt: String
)