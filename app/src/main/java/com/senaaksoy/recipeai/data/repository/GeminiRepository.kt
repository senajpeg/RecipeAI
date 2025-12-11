package com.senaaksoy.recipeai.data.repository

import android.util.Log
import com.google.gson.Gson
import com.senaaksoy.recipeai.data.remote.api.RecipeApiService
import com.senaaksoy.recipeai.data.remote.dto.AiGeneratedRecipe
import com.senaaksoy.recipeai.data.remote.dto.GenerateRecipeRequest
import com.senaaksoy.recipeai.utills.Resource
import retrofit2.Response
import javax.inject.Inject

class GeminiRepository @Inject constructor(
    private val api: RecipeApiService,
    private val gson: Gson
) {
    private suspend fun <T, R> safeGeminiCall(
        operation: String,
        transform: (T) -> R,
        onSuccess: (R) -> Unit = {},
        call: suspend () -> Response<T>
    ): Resource<R> {
        return try {
            Log.d("GeminiRepository", "Starting: $operation")

            val response = call()
            Log.d("GeminiRepository", "Response code: ${response.code()}")
            Log.d("GeminiRepository", "Response message: ${response.message()}")

            if (response.isSuccessful && response.body() != null) {
                val rawData = response.body()!!
                Log.d("GeminiRepository", "Response body: ${gson.toJson(rawData)}")

                val transformedData = transform(rawData)
                onSuccess(transformedData)

                Log.d("GeminiRepository", "✅ $operation successful")
                Resource.Success(transformedData)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GeminiRepository", "❌ $operation failed: $errorBody")
                Resource.Error("API hatası: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Log.e("GeminiRepository", "❌ $operation error: ${e.message}", e)
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Bilinmeyen hata: ${e.message}")
        }
    }

    suspend fun generateRecipe(ingredients: List<String>): Resource<AiGeneratedRecipe> {
        Log.d("GeminiRepository", "Generating recipe with ingredients: $ingredients")

        val request = GenerateRecipeRequest(ingredients)
        Log.d("GeminiRepository", "Request: ${gson.toJson(request)}")

        return safeGeminiCall(
            operation = "Generate recipe with ${ingredients.size} ingredients",
            transform = { recipe: AiGeneratedRecipe -> recipe },
            onSuccess = { recipe ->
                Log.d("GeminiRepository", "Generated: ${recipe.name}")
                Log.d("GeminiRepository", "Ingredients: ${recipe.ingredients.size}")
                Log.d("GeminiRepository", "Can be made: ${recipe.canBeMade}")
            }
        ) {
            api.generateRecipe(request)
        }
    }
}