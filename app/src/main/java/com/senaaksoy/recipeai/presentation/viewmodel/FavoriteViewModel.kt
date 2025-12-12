package com.senaaksoy.recipeai.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senaaksoy.recipeai.data.repository.FavoriteRepository
import com.senaaksoy.recipeai.domain.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val repository: FavoriteRepository
) : ViewModel() {

    val favorites: StateFlow<List<Recipe>> = repository.favorites
    val favoriteStates: StateFlow<Map<Int, Boolean>> = repository.favoriteStates

    val favoriteCount: StateFlow<Int> = repository.favorites
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 0
        )

    init {
        Log.d("FavoriteViewModel", "🚀 ViewModel initialized")
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            Log.d("FavoriteViewModel", "📥 Loading favorites...")
            repository.loadFavorites()
        }
    }

    fun checkFavorite(recipeId: Int) {
        viewModelScope.launch {
            Log.d("FavoriteViewModel", "🔍 Checking favorite: $recipeId")
            repository.checkFavorite(recipeId)
        }
    }

    fun toggleFavorite(recipe: Recipe) {
        viewModelScope.launch {
            Log.d("FavoriteViewModel", "⭐ Toggling favorite: ${recipe.name}")
            repository.toggleFavorite(recipe)
        }
    }
}