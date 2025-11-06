package com.senaaksoy.recipeai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senaaksoy.recipeai.data.remote.Resource
import com.senaaksoy.recipeai.domain.repository.RecipeRepository
import com.senaaksoy.recipeai.presentation.state.RecipeListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeListState())
    val state: StateFlow<RecipeListState> = _state.asStateFlow()

    init {
        loadRecipes()
    }

    // Local'den tarifleri yükle (Flow ile sürekli dinle)
    private fun loadRecipes() {
        viewModelScope.launch {
            repository.getAllRecipesFromLocal()
                .onStart {
                    _state.value = _state.value.copy(isLoading = true)
                }
                .catch { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = exception.localizedMessage
                    )
                }
                .collect { recipes ->
                    _state.value = _state.value.copy(
                        recipes = recipes,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    // API'den senkronize et (Pull-to-refresh için)
    fun refreshRecipes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)

            when (val result = repository.syncRecipesFromApi()) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isRefreshing = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isRefreshing = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    // Loading state zaten set edildi
                }
            }
        }
    }

    // Hata mesajını temizle
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}