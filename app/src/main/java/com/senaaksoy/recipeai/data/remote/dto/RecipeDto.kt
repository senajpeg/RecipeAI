package com.senaaksoy.recipeai.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RecipeDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("instructions")
    val instructions: String,

    @SerializedName("cooking_time")
    val cookingTime: Int?,

    @SerializedName("difficulty")
    val difficulty: String?,

    @SerializedName("image_url")
    val imageUrl: String?,

    @SerializedName("created_at")
    val createdAt: String?
)