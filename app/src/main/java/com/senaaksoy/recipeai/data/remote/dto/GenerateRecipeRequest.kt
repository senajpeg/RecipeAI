package com.senaaksoy.recipeai.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GenerateRecipeRequest(
    @SerializedName("ingredients")
    val ingredients: List<String>
)