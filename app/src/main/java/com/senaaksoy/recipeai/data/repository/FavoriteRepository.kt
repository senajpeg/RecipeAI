package com.senaaksoy.recipeai.data.repository

import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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
    private val workManager: WorkManager
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val favorites: StateFlow<List<Recipe>> = dao.getFavoriteRecipes()
        .map { entities ->
            entities.map { it.toRecipe() }.also {
                Log.d("FavoriteRepo", "📋 Local favoriler: ${it.size} adet")
            }
        }
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
                Log.d("FavoriteRepo", "🔄 Favorite states güncellendi: ${states.size} adet")
            }
        }
    }

    private fun getAuthToken(): String {
        return "Bearer ${tokenManager.getToken()}"
    }

    private suspend fun <T, R> safeFavoriteCall(
        operation: String,
        transform: (T) -> R,
        call: suspend () -> Response<T>
    ): Resource<R> {
        return try {
            val response = call()
            Log.d("FavoriteRepo", "🌐 $operation -> Response Code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(transform(response.body()!!))
            } else {
                Resource.Error("$operation başarısız: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("FavoriteRepo", "❌ $operation hatası", e)
            Resource.Error(e.localizedMessage ?: "Hata oluştu")
        }
    }

    suspend fun loadFavorites(): Resource<List<Recipe>> {
        Log.d("FavoriteRepo", "🔄 loadFavorites() çağrıldı")

        if (!networkUtils.isNetworkAvailable()) {
            Log.w("FavoriteRepo", "⚠️ İnternet yok, local veriler kullanılıyor")
            val currentFavorites = favorites.value
            return if (currentFavorites.isNotEmpty()) Resource.Success(currentFavorites)
            else Resource.Error("İnternet bağlantısı yok")
        }

        Log.d("FavoriteRepo", "🌐 Backend'den favoriler çekiliyor...")
        val result = safeFavoriteCall(
            operation = "Loading favorites",
            transform = { list: List<RecipeDto> -> list.map { it.toRecipe() } }
        ) { api.getFavorites(getAuthToken()) }

        if (result is Resource.Success) {
            val apiRecipes = result.data ?: emptyList()
            Log.d("FavoriteRepo", "✅ Backend'den ${apiRecipes.size} favori geldi")

            val apiIds = apiRecipes.map { it.id }.toSet()
            val currentLocalList = favorites.value

            Log.d("FavoriteRepo", "📋 Mevcut local favoriler: ${currentLocalList.size} adet")

            // ZOMBIE SAVAR MANTIK
            apiRecipes.forEach { apiRecipe ->
                val localRecipe = dao.getRecipeById(apiRecipe.id)

                if (localRecipe != null && !localRecipe.isFavorite) {
                    Log.d("FavoriteRepo", "🛡️ ZOMBIE ENGELLENDİ: ${localRecipe.name} (ID: ${localRecipe.id})")
                    return@forEach
                }

                // Yeni favori veya güncelleme
                Log.d("FavoriteRepo", "💾 Kaydediliyor: ${apiRecipe.name} (ID: ${apiRecipe.id})")
                dao.insertRecipe(apiRecipe.toEntity(isFavorite = true).copy(isSynced = true))
            }

            // Backend'de olmayanları temizle
            currentLocalList.forEach { localRecipe ->
                if (localRecipe.id !in apiIds) {
                    val entity = dao.getRecipeById(localRecipe.id)
                    if (entity != null && entity.isSynced) {
                        Log.d("FavoriteRepo", "🗑️ Backend'de yok, siliniyor: ${localRecipe.name} (ID: ${localRecipe.id})")
                        dao.updateFavoriteStatus(localRecipe.id, false)
                    }
                }
            }

            val updatedList = dao.getFavoriteRecipes().first()
            _favoriteStates.value = updatedList.associate { it.id to true }
            Log.d("FavoriteRepo", "✅ Senkronizasyon tamamlandı. Toplam favori: ${updatedList.size}")
        } else {
            Log.e("FavoriteRepo", "❌ Backend'den veri alınamadı: ${(result as? Resource.Error)?.message}")
        }

        return result
    }

    suspend fun checkFavorite(recipeId: Int): Boolean {
        val localRecipe = dao.getRecipeById(recipeId)
        val isFav = localRecipe?.isFavorite ?: false
        Log.d("FavoriteRepo", "🔍 Check Favorite ID:$recipeId -> $isFav")
        return isFav
    }

    suspend fun toggleFavorite(recipe: Recipe): Resource<Unit> {
        val isCurrentlyFav = _favoriteStates.value[recipe.id] ?: false
        val newState = !isCurrentlyFav

        Log.d("FavoriteRepo", "⭐ toggleFavorite: ${recipe.name} (ID:${recipe.id}) -> $newState")

        // 1. LOCAL'E YAZ (isSynced = false)
        dao.insertRecipe(recipe.toEntity(isFavorite = newState).copy(isSynced = false))
        _favoriteStates.value = _favoriteStates.value + (recipe.id to newState)

        Log.d("FavoriteRepo", "💾 Local'e yazıldı: isFavorite=$newState, isSynced=false")

        // 2. WORKMANAGER TETİKLE
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequest.Builder(FavoriteSyncWorker::class.java)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "sync_fav_work",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )

        Log.d("FavoriteRepo", "🚀 WorkManager tetiklendi")

        return Resource.Success(Unit)
    }
}