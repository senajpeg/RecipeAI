package com.senaaksoy.recipeai.data.remote.dto

import com.google.gson.annotations.SerializedName

data class Content(
    @SerializedName("parts")
    val parts: List<Part>
)

data class Part(
    @SerializedName("text")
    val text: String
)

data class GeminiResponse(
    @SerializedName("candidates")
    val candidates: List<Candidate>?
)

data class Candidate(
    @SerializedName("content")
    val content: ResponseContent?
)

data class ResponseContent(
    @SerializedName("parts")
    val parts: List<ResponsePart>?
)

data class ResponsePart(
    @SerializedName("text")
    val text: String?
)

// AI'dan gelen tarifte parse edilmiş model
data class AiGeneratedRecipe(
    val name: String,
    val description: String,
    val ingredients: List<String>,
    val instructions: String,
    val cookingTime: Int,
    val difficulty: String,
    val suggestions: List<String> = emptyList(),
    val canBeMade: Boolean = true
)