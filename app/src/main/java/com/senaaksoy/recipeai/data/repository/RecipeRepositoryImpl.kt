package com.senaaksoy.recipeai.data.repository

import com.senaaksoy.recipeai.data.local.dao.RecipeDao
import com.senaaksoy.recipeai.data.remote.Resource
import com.senaaksoy.recipeai.data.remote.api.RecipeApiService
import com.senaaksoy.recipeai.data.remote.dto.toEntity
import com.senaaksoy.recipeai.data.remote.dto.toRecipe
import com.senaaksoy.recipeai.data.remote.safeApiCall
import com.senaaksoy.recipeai.domain.model.Recipe
import com.senaaksoy.recipeai.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val api: RecipeApiService,
    private val dao: RecipeDao
) : RecipeRepository {

    // Local'den Flow ile tarifleri dinle
    override fun getAllRecipesFromLocal(): Flow<List<Recipe>> {
        return dao.getAllRecipes().map { entities ->
            entities.map { it.toRecipe() }
        }
    }

    // API'den tarifleri çek ve local'e kaydet
    override suspend fun syncRecipesFromApi(): Resource<List<Recipe>> {
        return when (val result = safeApiCall { api.getAllRecipes() }) {
            is Resource.Success -> {
                val recipes = result.data ?: emptyList()
                // API'den gelen verileri local'e kaydet
                val entities = recipes.map { it.toEntity() }
                dao.insertRecipes(entities)

                Resource.Success(recipes.map { it.toRecipe() })
            }
            is Resource.Error -> {
                Resource.Error(result.message ?: "Bilinmeyen hata")
            }
            is Resource.Loading -> {
                Resource.Loading()
            }
        }
    }

    // ID'ye göre tarif getir (önce local, yoksa API)
    override suspend fun getRecipeById(id: Int): Resource<Recipe> {
        return try {
            // Önce local'den dene
            val localRecipe = dao.getRecipeById(id)
            if (localRecipe != null) {
                return Resource.Success(localRecipe.toRecipe())
            }

            // Local'de yoksa API'den çek
            when (val result = safeApiCall { api.getRecipeById(id) }) {
                is Resource.Success -> {
                    val recipe = result.data?.toRecipe()
                    if (recipe != null) {
                        // API'den gelen veriyi local'e kaydet
                        dao.insertRecipe(result.data.toEntity())
                        Resource.Success(recipe)
                    } else {
                        Resource.Error("Tarif bulunamadı")
                    }
                }
                is Resource.Error -> {
                    Resource.Error(result.message ?: "Tarif getirilemedi")
                }
                is Resource.Loading -> {
                    Resource.Loading()
                }
            }
        } catch (e: Exception) {
            Resource.Error("Hata: ${e.localizedMessage}")
        }
    }

    // Yeni tarif oluştur
    override suspend fun createRecipe(recipe: Recipe): Resource<Recipe> {
        return try {
            // Önce local'e kaydet (offline çalışabilmesi için)
            val entity = recipe.toEntity()
            val localId = dao.insertRecipe(entity)

            // Sonra API'ye gönder (arka planda senkronize et)
            // TODO: API'ye POST isteği gönder
            // Şimdilik sadece local'e kaydediyoruz

            Resource.Success(recipe.copy(id = localId.toInt()))
        } catch (e: Exception) {
            Resource.Error("Tarif kaydedilemedi: ${e.localizedMessage}")
        }
    }

    // Tarif güncelle
    override suspend fun updateRecipe(recipe: Recipe): Resource<Recipe> {
        return try {
            // Local'i güncelle
            dao.insertRecipe(recipe.toEntity())

            // API'yi güncelle (arka planda)
            // TODO: API'ye PUT isteği gönder

            Resource.Success(recipe)
        } catch (e: Exception) {
            Resource.Error("Tarif güncellenemedi: ${e.localizedMessage}")
        }
    }

    // Tarif sil
    override suspend fun deleteRecipe(recipe: Recipe): Resource<Unit> {
        return try {
            // Local'den sil
            dao.deleteRecipe(recipe.toEntity())

            // API'den sil (arka planda)
            // TODO: API'ye DELETE isteği gönder

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Tarif silinemedi: ${e.localizedMessage}")
        }
    }
}