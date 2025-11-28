package com.senaaksoy.recipeai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senaaksoy.recipeai.data.remote.Resource
import com.senaaksoy.recipeai.data.repository.GeminiRepository
import com.senaaksoy.recipeai.presentation.state.AddRecipeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddRecipeState())
    val state: StateFlow<AddRecipeState> = _state.asStateFlow()

    fun addIngredient(ingredient: String) {
        val currentIngredients = _state.value.ingredients.toMutableList()
        if (!currentIngredients.contains(ingredient)) {
            currentIngredients.add(ingredient)
            _state.value = _state.value.copy(ingredients = currentIngredients)
        }
    }

    fun removeIngredient(ingredient: String) {
        val currentIngredients = _state.value.ingredients.toMutableList()
        currentIngredients.remove(ingredient)
        _state.value = _state.value.copy(ingredients = currentIngredients)
    }

    fun generateRecipe() {
        val ingredients = _state.value.ingredients

        if (ingredients.isEmpty()) {
            _state.value = _state.value.copy(error = "Lütfen en az bir malzeme ekleyin")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = geminiRepository.generateRecipe(ingredients)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        generatedRecipe = result.data,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    // Loading zaten set edildi
                }
            }
        }
    }

    fun resetState() {
        _state.value = AddRecipeState()
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}