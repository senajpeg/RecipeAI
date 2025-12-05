package com.senaaksoy.recipeai.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senaaksoy.recipeai.data.remote.Resource
import com.senaaksoy.recipeai.data.repository.RecipeRepositoryImpl
import com.senaaksoy.recipeai.domain.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val repository: RecipeRepositoryImpl
) : ViewModel() {

    private val _discoverRecipes = MutableStateFlow<Resource<List<Recipe>>>(Resource.Loading())
    val discoverRecipes: StateFlow<Resource<List<Recipe>>> = _discoverRecipes.asStateFlow()

    private val _dailySuggestions = MutableStateFlow<Resource<List<Recipe>>>(Resource.Loading())
    val dailySuggestions: StateFlow<Resource<List<Recipe>>> = _dailySuggestions.asStateFlow()

    private val _searchResults = MutableStateFlow<Resource<List<Recipe>>?>(null)
    val searchResults: StateFlow<Resource<List<Recipe>>?> = _searchResults.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        Log.d("RecipeListViewModel", "=================================")
        Log.d("RecipeListViewModel", "🏠 ViewModel oluşturuldu")
        Log.d("RecipeListViewModel", "=================================")
        loadRecipes()
        loadDailySuggestions()
    }

    private fun loadRecipes() {
        viewModelScope.launch {
            Log.d("RecipeListViewModel", "🔵 Keşfet tarifleri yükleniyor...")
            _discoverRecipes.value = Resource.Loading()

            val result = repository.syncRecipesFromApi()

            when (result) {
                is Resource.Success -> {
                    Log.d("RecipeListViewModel", "✅ Keşfet: ${result.data?.size} tarif yüklendi")
                }
                is Resource.Error -> {
                    Log.e("RecipeListViewModel", "❌ Keşfet hatası: ${result.message}")
                }
                is Resource.Loading -> {}
            }

            _discoverRecipes.value = result
        }
    }

    private fun loadDailySuggestions() {
        viewModelScope.launch {
            Log.d("RecipeListViewModel", "🔵 Günün önerisi yükleniyor...")
            _dailySuggestions.value = Resource.Loading()

            val result = repository.getRandomRecipes(3)

            when (result) {
                is Resource.Success -> {
                    Log.d("RecipeListViewModel", "✅ Günün önerisi: ${result.data?.size} tarif yüklendi")
                }
                is Resource.Error -> {
                    Log.e("RecipeListViewModel", "❌ Günün önerisi hatası: ${result.message}")
                }
                is Resource.Loading -> {}
            }

            _dailySuggestions.value = result
        }
    }

    fun searchRecipes(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            _searchResults.value = null
            return
        }

        viewModelScope.launch {
            Log.d("RecipeListViewModel", "🔍 Arama yapılıyor: $query")
            _searchResults.value = Resource.Loading()
            _searchResults.value = repository.searchRecipes(query)
        }
    }

    fun refreshRecipes() {
        Log.d("RecipeListViewModel", "🔄 Tarifler yenileniyor...")
        loadRecipes()
        loadDailySuggestions()
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = null
    }
}