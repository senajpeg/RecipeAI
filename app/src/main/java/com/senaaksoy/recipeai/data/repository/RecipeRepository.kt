package com.senaaksoy.recipeai.data.repository

import com.senaaksoy.recipeai.domain.model.Recipe
import com.senaaksoy.recipeai.utills.Resource
import kotlinx.coroutines.flow.Flow


interface RecipeRepository {

    // Local'den Flow ile sürekli dinle
    fun getAllRecipesFromLocal(): Flow<List<Recipe>>

    // API'den çek ve local'e kaydet
    suspend fun syncRecipesFromApi(): Resource<List<Recipe>>

    // ID'ye göre getir (önce local, yoksa API)
    suspend fun getRecipeById(id: Int): Resource<Recipe>

    // Yeni tarif ekle (önce API, sonra local)
    suspend fun createRecipe(recipe: Recipe): Resource<Recipe>



}