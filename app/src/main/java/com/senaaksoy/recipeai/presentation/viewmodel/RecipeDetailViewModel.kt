package com.senaaksoy.recipeai.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senaaksoy.recipeai.data.remote.Resource
import com.senaaksoy.recipeai.data.repository.RecipeRepository
import com.senaaksoy.recipeai.presentation.state.RecipeDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val repository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeDetailState())
    val state: StateFlow<RecipeDetailState> = _state.asStateFlow()

    init {
        // recipeId'yi Int olarak al (String değil!)
        savedStateHandle.get<Int>("recipeId")?.let { recipeId ->
            loadRecipe(recipeId)
        }
    }

    private fun loadRecipe(recipeId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            when (val result = repository.getRecipeById(recipeId)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        recipe = result.data,
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> { /* already handled */ }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}