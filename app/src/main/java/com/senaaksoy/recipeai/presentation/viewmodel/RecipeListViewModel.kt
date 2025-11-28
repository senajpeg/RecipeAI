package com.senaaksoy.recipeai.presentation.viewmodel

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

    // Keşfet (Discover) tarifleri
    private val _discoverRecipes = MutableStateFlow<Resource<List<Recipe>>>(Resource.Loading())
    val discoverRecipes: StateFlow<Resource<List<Recipe>>> = _discoverRecipes.asStateFlow()

    // Günün Önerisi (Random tarifleri)
    private val _dailySuggestions = MutableStateFlow<Resource<List<Recipe>>>(Resource.Loading())
    val dailySuggestions: StateFlow<Resource<List<Recipe>>> = _dailySuggestions.asStateFlow()

    // Arama sonuçları
    private val _searchResults = MutableStateFlow<Resource<List<Recipe>>?>(null)
    val searchResults: StateFlow<Resource<List<Recipe>>?> = _searchResults.asStateFlow()

    // Arama query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadRecipes()
        loadDailySuggestions()
    }

    // Keşfet tariflerini yükle
    private fun loadRecipes() {
        viewModelScope.launch {
            _discoverRecipes.value = Resource.Loading()
            _discoverRecipes.value = repository.syncRecipesFromApi()
        }
    }

    // Günün Önerisi yükle (3 random tarif)
    private fun loadDailySuggestions() {
        viewModelScope.launch {
            _dailySuggestions.value = Resource.Loading()
            _dailySuggestions.value = repository.getRandomRecipes(3)
        }
    }

    // Arama yap
    fun searchRecipes(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            _searchResults.value = null
            return
        }

        viewModelScope.launch {
            _searchResults.value = Resource.Loading()
            _searchResults.value = repository.searchRecipes(query)
        }
    }

    // Tarifleri yenile (Pull-to-refresh)
    fun refreshRecipes() {
        loadRecipes()
        loadDailySuggestions()
    }

    // Arama sonuçlarını temizle
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = null
    }
}