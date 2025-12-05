package com.senaaksoy.recipeai.presentation.viewmodel

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

    // ✅ Favori sayısı
    val favoriteCount: StateFlow<Int> = repository.favorites
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = 0
        )

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            repository.loadFavorites()
        }
    }

    fun checkFavorite(recipeId: Int) {
        viewModelScope.launch {
            repository.checkFavorite(recipeId)
        }
    }

    fun toggleFavorite(recipe: Recipe) {
        viewModelScope.launch {
            repository.toggleFavorite(recipe)
        }
    }
}