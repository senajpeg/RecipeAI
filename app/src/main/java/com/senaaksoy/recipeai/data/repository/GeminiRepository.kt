package com.senaaksoy.recipeai.data.repository

import android.util.Log
import com.google.gson.Gson
import com.senaaksoy.recipeai.data.remote.Resource
import com.senaaksoy.recipeai.data.remote.api.RecipeApiService
import com.senaaksoy.recipeai.data.remote.dto.AiGeneratedRecipe
import com.senaaksoy.recipeai.data.remote.dto.GenerateRecipeRequest
import javax.inject.Inject

class GeminiRepository @Inject constructor(
    private val api: RecipeApiService,
    private val gson: Gson
) {

    suspend fun generateRecipe(ingredients: List<String>): Resource<AiGeneratedRecipe> {
        return try {
            Log.d("GeminiRepository", "Generating recipe with ingredients: $ingredients")

            val request = GenerateRecipeRequest(ingredients)
            Log.d("GeminiRepository", "Request: ${gson.toJson(request)}")

            val response = api.generateRecipe(request)
            Log.d("GeminiRepository", "Response code: ${response.code()}")
            Log.d("GeminiRepository", "Response message: ${response.message()}")

            if (response.isSuccessful) {
                val recipe = response.body()
                Log.d("GeminiRepository", "Recipe body: ${gson.toJson(recipe)}")

                if (recipe != null) {
                    Resource.Success(recipe)
                } else {
                    Log.e("GeminiRepository", "Recipe body is null")
                    Resource.Error("Tarif oluşturulamadı - Body null")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GeminiRepository", "API Error Body: $errorBody")
                Resource.Error("API hatası: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Exception: ${e.message}", e)
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Bilinmeyen hata: ${e.message}")
        }
    }
}