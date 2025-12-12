package com.senaaksoy.recipeai.data.repository

import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.senaaksoy.recipeai.data.local.dao.RecipeDao
import com.senaaksoy.recipeai.data.remote.api.FavoriteApi
import com.senaaksoy.recipeai.data.remote.dto.RecipeDto
import com.senaaksoy.recipeai.data.remote.dto.toEntity
import com.senaaksoy.recipeai.data.remote.dto.toRecipe
import com.senaaksoy.recipeai.domain.model.Recipe
import com.senaaksoy.recipeai.utills.NetworkUtils
import com.senaaksoy.recipeai.utills.Resource
import com.senaaksoy.recipeai.utills.TokenManager
import com.senaaksoy.recipeai.worker.FavoriteSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val api: FavoriteApi,
    private val tokenManager: TokenManager,
    private val dao: RecipeDao,
    private val networkUtils: NetworkUtils,
    private val workManager: WorkManager // ✅ WorkManager enjekte edildi
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val favorites: StateFlow<List<Recipe>> = dao.getFavoriteRecipes()
        .map { entities -> entities.map { it.toRecipe() } }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _favoriteStates = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val favoriteStates: StateFlow<Map<Int, Boolean>> = _favoriteStates

    init {
        repositoryScope.launch {
            dao.getFavoriteRecipes().collect { entities ->
                val states = entities.associate { it.id to true }
                _favoriteStates.value = states
                Log.d("FavoriteRepo", "📊 Favori states güncellendi: ${states.size} tarif")
            }
        }
    }

    private fun getAuthToken(): String {
        val token = tokenManager.getToken()
        return "Bearer $token"
    }

    private suspend fun <T, R> safeFavoriteCall(
        operation: String,
        transform: (T) -> R,
        call: suspend () -> Response<T>
    ): Resource<R> {
        return try {
            Log.d("FavoriteRepo", "Starting: $operation")
            val response = call()
            Log.d("FavoriteRepo", "Response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val rawData = response.body()!!
                val transformedData = transform(rawData)
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
        if (!networkUtils.isNetworkAvailable()) {
            val currentFavorites = favorites.value
            Log.d(
                "FavoriteRepo",
                "🔴 İnternet yok, ${currentFavorites.size} favori Room'dan yüklendi"
            )

            return if (currentFavorites.isNotEmpty()) {
                Resource.Success(currentFavorites)
            } else {
                Resource.Error("İnternet bağlantısı yok ve yerel favori bulunamadı")
            }
        }

        val result = safeFavoriteCall(
            operation = "Loading favorites from API",
            transform = { dtoList: List<RecipeDto> ->
                dtoList.map { it.toRecipe() }
            }
        ) {
            api.getFavorites(getAuthToken())
        }

        if (result is Resource.Success) {
            val recipes = result.data ?: emptyList()
            Log.d("FavoriteRepo", "🌐 API'den ${recipes.size} favori geldi")


            recipes.forEach { apiRecipe ->

                val localRecipe = dao.getRecipeById(apiRecipe.id)

                if (localRecipe != null && !localRecipe.isSynced) {

                    Log.d(
                        "FavoriteRepo",
                        "🛡️ Sync bekleyen veri korundu, API'den gelen ezilmedi: ${localRecipe.name}"
                    )
                } else {

                    dao.insertRecipe(apiRecipe.toEntity(isFavorite = true).copy(isSynced = true))
                }
            }

            val newStates = recipes.associate { it.id to true }
            _favoriteStates.value = newStates

            Log.d(
                "FavoriteRepo",
                "✅ Veritabanı akıllıca güncellendi (Conflict Resolution uygulandı)"
            )
        }

        return result
    }

    suspend fun checkFavorite(recipeId: Int): Boolean {
        val localRecipe = dao.getRecipeById(recipeId)
        val isFav = localRecipe?.isFavorite ?: false
        Log.d("FavoriteRepo", "🔍 Recipe $recipeId local favorite status: $isFav")
        _favoriteStates.value = _favoriteStates.value + (recipeId to isFav)

        if (!networkUtils.isNetworkAvailable() || localRecipe == null) {
            return isFav
        }

        repositoryScope.launch {
            try {
                val response = api.isFavorite(recipeId, getAuthToken())
                if (response.isSuccessful) {
                    val apiIsFav = response.body() ?: false
                    if (apiIsFav != isFav) {
                        dao.updateFavoriteStatus(recipeId, apiIsFav)
                        _favoriteStates.value = _favoriteStates.value + (recipeId to apiIsFav)
                        Log.d(
                            "FavoriteRepo",
                            "🔄 Recipe $recipeId favorite status API'den güncellendi: $apiIsFav"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("FavoriteRepo", "API check failed: ${e.message}")
            }
        }
        return isFav
    }

    suspend fun toggleFavorite(recipe: Recipe): Resource<Unit> {
        val currentlyFavorite = _favoriteStates.value[recipe.id] ?: false
        val newState = !currentlyFavorite

        Log.d("FavoriteRepo", "=== TOGGLE FAVORITE ===")
        Log.d("FavoriteRepo", "Recipe: ${recipe.name} (ID: ${recipe.id})")
        Log.d("FavoriteRepo", "State: $currentlyFavorite → $newState")

        // ✅ ÖNCE LOCAL'İ GÜNCELLE
        // isSynced = false olarak işaretliyoruz
        dao.insertRecipe(recipe.toEntity(isFavorite = newState).copy(isSynced = false))

        _favoriteStates.value = _favoriteStates.value + (recipe.id to newState)

        delay(100)

        // 🔄 OFFLINE-FIRST: WorkManager'a Devret
        Log.d("FavoriteRepo", "🔄 Sync işlemi WorkManager'a devrediliyor...")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequest.Builder(FavoriteSyncWorker::class.java)
            .setConstraints(constraints)
            .build()

        workManager.enqueue(syncRequest)

        Log.d(
            "FavoriteRepo",
            "✅ WorkManager kuyruğuna eklendi (İnternet gelince senkronize olacak)"
        )

        return Resource.Success(Unit)
    }
}