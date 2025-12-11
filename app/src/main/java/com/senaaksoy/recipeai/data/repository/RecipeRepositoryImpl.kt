package com.senaaksoy.recipeai.data.repository

import android.util.Log
import com.senaaksoy.recipeai.data.local.dao.RecipeDao
import com.senaaksoy.recipeai.data.remote.api.MealDbApi
import com.senaaksoy.recipeai.data.remote.api.RecipeApiService
import com.senaaksoy.recipeai.data.remote.dto.MealDbResponse
import com.senaaksoy.recipeai.data.remote.dto.RecipeDto
import com.senaaksoy.recipeai.data.remote.dto.toRecipe
import com.senaaksoy.recipeai.domain.model.Recipe
import com.senaaksoy.recipeai.utills.Resource
import com.senaaksoy.recipeai.utills.TranslationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Response
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val backendApi: RecipeApiService,
    private val mealDbApi: MealDbApi,
    private val dao: RecipeDao,
    private val translationManager: TranslationManager
) : RecipeRepository {

    private suspend fun <T, R> safeRecipeRepoCall(
        operation: String,
        transform: (T) -> R,
        onSuccess: (R) -> Unit = {},
        call: suspend () -> Response<T>
    ): Resource<R> {
        return try {
            Log.d("RecipeRepository", "Starting: $operation")

            val response = call()
            Log.d("RecipeRepository", "Response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val rawData = response.body()!!
                val transformedData = transform(rawData)
                onSuccess(transformedData)

                Log.d("RecipeRepository", "✅ $operation successful")
                Resource.Success(transformedData)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("RecipeRepository", "❌ $operation failed: $errorBody")
                Resource.Error("$operation başarısız: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "❌ $operation error: ${e.message}", e)
            Resource.Error(e.localizedMessage ?: "Hata oluştu")
        }
    }

    private suspend fun safeTranslate(text: String, type: String = "text"): String {
        return try {
            translationManager.translate(text)
        } catch (e: Exception) {
            Log.e("RecipeRepository", "⚠️ $type çeviri hatası: ${e.message}")
            text
        }
    }

    private suspend fun safeTranslateList(items: List<String>?, type: String = "items"): List<String> {
        return try {
            items?.map { item ->
                safeTranslate(item, type)
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("RecipeRepository", "❌ $type çeviri listesi hatası: ${e.message}")
            items ?: emptyList()
        }
    }

    override fun getAllRecipesFromLocal(): Flow<List<Recipe>> {
        return dao.getAllRecipes().map { entities ->
            entities.map { it.toRecipe() }
        }
    }

    override suspend fun syncRecipesFromApi(): Resource<List<Recipe>> {
        return try {
            Log.d("RecipeRepository", "🔵 MealDB'den tarifler çekiliyor...")

            val categories = listOf("Chicken", "Beef", "Pasta", "Seafood", "Dessert")
            val allRecipes = mutableListOf<Recipe>()

            for (category in categories) {
                val result = safeRecipeRepoCall(
                    operation = "Fetch category: $category",
                    transform = { response: MealDbResponse ->
                        response.meals?.take(3) ?: emptyList()
                    }
                ) {
                    mealDbApi.getMealsByCategory(category)
                }

                if (result is Resource.Success) {
                    val recipes = result.data?.mapNotNull { mealDto ->
                        try {
                            val recipe = mealDto.toRecipe()
                            val translatedName = safeTranslate(recipe.name, "Name")
                            recipe.copy(name = translatedName)
                        } catch (e: Exception) {
                            Log.e("RecipeRepository", "❌ Tarif dönüştürme hatası: ${e.message}")
                            null
                        }
                    } ?: emptyList()

                    allRecipes.addAll(recipes)
                    Log.d("RecipeRepository", "✅ $category: ${recipes.size} tarif eklendi")
                } else if (result is Resource.Error) {
                    Log.e("RecipeRepository", "❌ $category hatası: ${result.message}")
                }
            }

            if (allRecipes.isNotEmpty()) {
                Log.d("RecipeRepository", "✅ Toplam ${allRecipes.size} tarif yüklendi")
                Resource.Success(allRecipes)
            } else {
                Log.e("RecipeRepository", "❌ Hiç tarif yüklenemedi!")
                Resource.Error("Tarifler yüklenemedi")
            }

        } catch (e: Exception) {
            Log.e("RecipeRepository", "❌ FATAL: ${e.message}", e)
            Resource.Error(e.localizedMessage ?: "Bağlantı hatası")
        }
    }

    override suspend fun getRecipeById(id: Int): Resource<Recipe> {
        return if (id < 0 || id > 50000000) {
            safeRecipeRepoCall(
                operation = "🤖 Fetch backend recipe: $id",
                transform = { dto: RecipeDto -> dto.toRecipe() },
                onSuccess = { recipe ->
                    Log.d("RecipeRepository", "✅ Backend tarif bulundu: ${recipe.name}")
                    Log.d("RecipeRepository", "   Ingredients: ${recipe.ingredients?.size ?: 0}")
                }
            ) {
                backendApi.getRecipeById(id)
            }
        } else {
            val fetchResult = safeRecipeRepoCall(
                operation = "🍔 Fetch MealDB recipe: $id",
                transform = { response: MealDbResponse ->
                    val meal = response.meals?.firstOrNull()
                        ?: throw Exception("MealDB'de tarif bulunamadı")
                    meal.toRecipe()
                }
            ) {
                mealDbApi.getMealById(id.toString())
            }
            when (fetchResult) {
                is Resource.Success -> {
                    val recipe = fetchResult.data ?: return Resource.Error("Tarif bulunamadı")
                    try {
                        val translatedRecipe = recipe.copy(
                            name = safeTranslate(recipe.name, "Name"),
                            instructions = safeTranslate(recipe.instructions, "Instructions"),
                            ingredients = safeTranslateList(recipe.ingredients, "Ingredients")
                        )
                        Resource.Success(translatedRecipe)
                    } catch (e: Exception) {
                        Log.e("RecipeRepository", "Translation failed, returning original", e)
                        Resource.Success(recipe)
                    }
                }
                is Resource.Error -> fetchResult
                is Resource.Loading -> fetchResult
            }
        }
    }

    suspend fun getRandomRecipes(count: Int = 3): Resource<List<Recipe>> {
        return try {
            Log.d("RecipeRepository", "🎲 $count adet random tarif çekiliyor...")
            val recipes = mutableListOf<Recipe>()

            repeat(count) { index ->
                val fetchResult = safeRecipeRepoCall(
                    operation = "Fetch random recipe ${index + 1}",
                    transform = { response: MealDbResponse ->
                        val mealDto = response.meals?.firstOrNull()
                            ?: throw Exception("Random tarif bulunamadı")
                        mealDto.toRecipe()
                    }
                ) {
                    mealDbApi.getRandomMeal()
                }

                if (fetchResult is Resource.Success) {
                    val recipe = fetchResult.data
                    if (recipe != null) {
                        try {
                            val translatedRecipe = recipe.copy(
                                name = safeTranslate(recipe.name, "Name"),
                                ingredients = safeTranslateList(recipe.ingredients, "Ingredients")
                            )
                            recipes.add(translatedRecipe)
                            Log.d("RecipeRepository", "✅ Random tarif ${index + 1}: ${translatedRecipe.name}")
                        } catch (e: Exception) {
                            Log.e("RecipeRepository", "Translation failed, using original", e)
                            recipes.add(recipe)
                        }
                    }
                } else if (fetchResult is Resource.Error) {
                    Log.e("RecipeRepository", "❌ Random tarif ${index + 1} hatası: ${fetchResult.message}")
                }
            }

            if (recipes.isNotEmpty()) {
                Resource.Success(recipes)
            } else {
                Resource.Error("Random tarifler yüklenemedi")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Hata oluştu")
        }
    }

    override suspend fun createRecipe(recipe: Recipe): Resource<Recipe> {
        return Resource.Error("Henüz desteklenmiyor")
    }
}