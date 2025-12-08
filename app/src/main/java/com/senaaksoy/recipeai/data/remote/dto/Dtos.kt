package com.senaaksoy.recipeai.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.senaaksoy.recipeai.data.local.entity.RecipeEntity
import com.senaaksoy.recipeai.domain.model.Recipe

// -------------------- USER DTO --------------------

data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val createdAt: String
)

// -------------------- GEMINI MODELS --------------------

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

// -------------------- AI GENERATED RECIPE --------------------

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

// ✅ AI → Recipe
fun AiGeneratedRecipe.toRecipe(): Recipe {
    return Recipe(
        id = name.hashCode(),
        name = name,
        description = description,
        instructions = instructions,
        cookingTime = cookingTime,
        difficulty = difficulty,
        imageUrl = null,
        createdAt = System.currentTimeMillis(),
        ingredients = ingredients
    )
}

// -------------------- MEAL DB MODELS --------------------

data class MealDbResponse(
    @SerializedName("meals")
    val meals: List<MealDto>?
)

data class MealDto(
    @SerializedName("idMeal")
    val idMeal: String,

    @SerializedName("strMeal")
    val name: String,

    @SerializedName("strCategory")
    val category: String?,

    @SerializedName("strArea")
    val area: String?,

    @SerializedName("strInstructions")
    val instructions: String?,

    @SerializedName("strMealThumb")
    val imageUrl: String?,

    @SerializedName("strTags")
    val tags: String?,

    @SerializedName("strIngredient1") val ingredient1: String?,
    @SerializedName("strIngredient2") val ingredient2: String?,
    @SerializedName("strIngredient3") val ingredient3: String?,
    @SerializedName("strIngredient4") val ingredient4: String?,
    @SerializedName("strIngredient5") val ingredient5: String?,
    @SerializedName("strIngredient6") val ingredient6: String?,
    @SerializedName("strIngredient7") val ingredient7: String?,
    @SerializedName("strIngredient8") val ingredient8: String?,
    @SerializedName("strIngredient9") val ingredient9: String?,
    @SerializedName("strIngredient10") val ingredient10: String?,
    @SerializedName("strIngredient11") val ingredient11: String?,
    @SerializedName("strIngredient12") val ingredient12: String?,
    @SerializedName("strIngredient13") val ingredient13: String?,
    @SerializedName("strIngredient14") val ingredient14: String?,
    @SerializedName("strIngredient15") val ingredient15: String?,
    @SerializedName("strIngredient16") val ingredient16: String?,
    @SerializedName("strIngredient17") val ingredient17: String?,
    @SerializedName("strIngredient18") val ingredient18: String?,
    @SerializedName("strIngredient19") val ingredient19: String?,
    @SerializedName("strIngredient20") val ingredient20: String?,

    @SerializedName("strMeasure1") val measure1: String?,
    @SerializedName("strMeasure2") val measure2: String?,
    @SerializedName("strMeasure3") val measure3: String?,
    @SerializedName("strMeasure4") val measure4: String?,
    @SerializedName("strMeasure5") val measure5: String?,
    @SerializedName("strMeasure6") val measure6: String?,
    @SerializedName("strMeasure7") val measure7: String?,
    @SerializedName("strMeasure8") val measure8: String?,
    @SerializedName("strMeasure9") val measure9: String?,
    @SerializedName("strMeasure10") val measure10: String?,
    @SerializedName("strMeasure11") val measure11: String?,
    @SerializedName("strMeasure12") val measure12: String?,
    @SerializedName("strMeasure13") val measure13: String?,
    @SerializedName("strMeasure14") val measure14: String?,
    @SerializedName("strMeasure15") val measure15: String?,
    @SerializedName("strMeasure16") val measure16: String?,
    @SerializedName("strMeasure17") val measure17: String?,
    @SerializedName("strMeasure18") val measure18: String?,
    @SerializedName("strMeasure19") val measure19: String?,
    @SerializedName("strMeasure20") val measure20: String?
)

// ✅ Ölçülü malzemeler
fun MealDto.getIngredientsWithMeasures(): List<String> {
    val ingredientsList = listOfNotNull(
        ingredient1, ingredient2, ingredient3, ingredient4, ingredient5,
        ingredient6, ingredient7, ingredient8, ingredient9, ingredient10,
        ingredient11, ingredient12, ingredient13, ingredient14, ingredient15,
        ingredient16, ingredient17, ingredient18, ingredient19, ingredient20
    )

    val measuresList = listOfNotNull(
        measure1, measure2, measure3, measure4, measure5,
        measure6, measure7, measure8, measure9, measure10,
        measure11, measure12, measure13, measure14, measure15,
        measure16, measure17, measure18, measure19, measure20
    )

    val result = mutableListOf<String>()

    ingredientsList.forEachIndexed { index, ingredient ->
        if (ingredient.isNotBlank()) {
            val measure = measuresList.getOrNull(index)
            if (!measure.isNullOrBlank()) {
                result.add("${measure.trim()} ${ingredient.trim()}")
            } else {
                result.add(ingredient.trim())
            }
        }
    }
    return result
}

// ✅ Meal → Recipe
fun MealDto.toRecipe(): Recipe {
    val ingredients = getIngredientsWithMeasures()

    return Recipe(
        id = idMeal.toIntOrNull() ?: idMeal.hashCode(),
        name = name,
        description = category ?: "Lezzetli bir tarif",
        instructions = instructions ?: "Tarif yükleniyor...",
        cookingTime = null,
        difficulty = when {
            ingredients.size <= 5 -> "Kolay"
            ingredients.size <= 10 -> "Orta"
            else -> "Zor"
        },
        imageUrl = imageUrl,
        createdAt = System.currentTimeMillis(),
        ingredients = ingredients
    )
}

// -------------------- BACKEND RECIPE DTO --------------------

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

    @SerializedName("ingredients")
    val ingredients: List<String>? = null,

    @SerializedName("created_at")
    val createdAt: String?
)








