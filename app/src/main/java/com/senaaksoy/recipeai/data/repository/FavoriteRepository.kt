package com.senaaksoy.recipeai.data.repository

import android.util.Log
import com.senaaksoy.recipeai.data.remote.api.FavoriteApi
import com.senaaksoy.recipeai.data.remote.dto.AddFavoriteRequest
import com.senaaksoy.recipeai.data.remote.dto.RecipeDto
import com.senaaksoy.recipeai.data.remote.dto.toRecipe
import com.senaaksoy.recipeai.domain.model.Recipe
import com.senaaksoy.recipeai.utills.Resource
import com.senaaksoy.recipeai.utills.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val api: FavoriteApi,
    private val tokenManager: TokenManager
) {

    private val _favorites = MutableStateFlow<List<Recipe>>(emptyList())
    val favorites: StateFlow<List<Recipe>> = _favorites.asStateFlow()

    private val _favoriteStates = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val favoriteStates: StateFlow<Map<Int, Boolean>> = _favoriteStates.asStateFlow()

    private fun getAuthToken(): String {
        val token = tokenManager.getToken()
        Log.d("FavoriteRepo", "Token: ${token?.take(20)}...")
        return "Bearer $token"
    }
    private suspend fun <T, R> safeFavoriteCall(
        operation: String,
        transform: (T) -> R,
        onSuccess: (R) -> Unit = {},
        call: suspend () -> Response<T>
    ): Resource<R> {
        return try {
            Log.d("FavoriteRepo", "Starting: $operation")
            val response = call()
            Log.d("FavoriteRepo", "Response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val rawData = response.body()!!
                val transformedData = transform(rawData)
                onSuccess(transformedData)
                Log.d("FavoriteRepo", "✅ $operation successful")
                Resource.Success(transformedData)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("FavoriteRepo", "❌ $operation failed: $errorBody")
                Resource.Error("$operation başarısız: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("FavoriteRepo", "❌ $operation error: ${e.message}", e)
            Resource.Error(e.localizedMessage ?: "Hata oluştu")
        }
    }

    suspend fun loadFavorites(): Resource<List<Recipe>> {
        return safeFavoriteCall(
            operation = "Loading favorites",
            transform = { dtoList: List<RecipeDto> ->
                dtoList.map { it.toRecipe() }
            },
            onSuccess = { recipes ->
                _favorites.value = recipes

                val newStates = recipes.associate { it.id to true }
                _favoriteStates.value = _favoriteStates.value + newStates

                Log.d("FavoriteRepo", "${recipes.size} favori yüklendi")
                recipes.forEach {
                    Log.d("FavoriteRepo", "  - ${it.name} (${it.ingredients?.size ?: 0} malzeme)")
                }
            }
        ) {
            api.getFavorites(getAuthToken())
        }
    }

    suspend fun checkFavorite(recipeId: Int): Boolean {
        return try {
            Log.d("FavoriteRepo", "Checking favorite status for recipe: $recipeId")
            val response = api.isFavorite(recipeId, getAuthToken())

            if (response.isSuccessful) {
                val isFav = response.body() ?: false
                _favoriteStates.value = _favoriteStates.value + (recipeId to isFav)
                Log.d("FavoriteRepo", "Recipe $recipeId favorite status: $isFav")
                isFav
            } else {
                Log.e("FavoriteRepo", "Check favorite failed: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("FavoriteRepo", "Check favorite error: ${e.message}", e)
            false
        }
    }

    suspend fun toggleFavorite(recipe: Recipe): Resource<Unit> {
        val currentlyFavorite = _favoriteStates.value[recipe.id] ?: false

        Log.d("FavoriteRepo", "=== TOGGLE FAVORITE ===")
        Log.d("FavoriteRepo", "Recipe: ${recipe.name} (ID: ${recipe.id})")
        Log.d("FavoriteRepo", "Current state: $currentlyFavorite → ${!currentlyFavorite}")

        val result = if (currentlyFavorite) {

            safeFavoriteCall(
                operation = "Removing favorite: ${recipe.name}",
                transform = { _: Any -> Unit },
                onSuccess = {
                    _favoriteStates.value = _favoriteStates.value + (recipe.id to false)
                }
            ) {
                api.removeFavorite(recipe.id, getAuthToken())
            }
        } else {
            val request = AddFavoriteRequest(
                id = recipe.id,
                name = recipe.name,
                description = recipe.description,
                instructions = recipe.instructions,
                cookingTime = recipe.cookingTime,
                difficulty = recipe.difficulty,
                imageUrl = recipe.imageUrl,
                ingredients = recipe.ingredients
            )

            Log.d("FavoriteRepo", "Request: ${request.name} with ${request.ingredients?.size ?: 0} ingredients")

            safeFavoriteCall(
                operation = "Adding favorite: ${recipe.name}",
                transform = { _: Any -> Unit },
                onSuccess = {
                    _favoriteStates.value = _favoriteStates.value + (recipe.id to true)
                }
            ) {
                api.addFavorite(recipe.id, getAuthToken(), request)
            }
        }


        if (result is Resource.Success) {
            loadFavorites()
        }

        return result
    }
}