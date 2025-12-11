package com.senaaksoy.recipeai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senaaksoy.recipeai.data.repository.RecipeRepositoryImpl
import com.senaaksoy.recipeai.domain.model.Recipe
import com.senaaksoy.recipeai.utills.Resource
import com.senaaksoy.recipeai.utills.TranslationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val repository: RecipeRepositoryImpl,
    private val translationManager: TranslationManager
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
        loadRecipes()
        loadDailySuggestions()
    }

    private fun loadRecipes() {
        viewModelScope.launch {
            _discoverRecipes.value = Resource.Loading()
            val result = repository.syncRecipesFromApi()
            _discoverRecipes.value = result
        }
    }

    private fun loadDailySuggestions() {
        viewModelScope.launch {
            _dailySuggestions.value = Resource.Loading()
            val result = repository.getRandomRecipes(3)
            _dailySuggestions.value = result
        }
    }

    fun filterRecipes(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            _searchResults.value = null
            return
        }

        viewModelScope.launch {
            _searchResults.value = Resource.Loading()

            val translatedQuery = translationManager.translate(query).lowercase().trim()

            val allRecipes = mutableListOf<Recipe>()
            (discoverRecipes.value as? Resource.Success)?.data?.let { allRecipes.addAll(it) }
            (dailySuggestions.value as? Resource.Success)?.data?.let { allRecipes.addAll(it) }

            val filtered = allRecipes.filter { recipe ->
                listOf(
                    recipe.name,
                    recipe.description ?: "",
                    recipe.instructions,
                    recipe.difficulty ?: "",
                    recipe.cookingTime?.toString() ?: ""
                ).any { it.lowercase().contains(translatedQuery) } ||
                        recipe.ingredients.any { it.lowercase().contains(translatedQuery) }
            }

            _searchResults.value = if (filtered.isNotEmpty())
                Resource.Success(filtered)
            else
                Resource.Error("‘$query’ için tarif bulunamadı")
        }
    }

}