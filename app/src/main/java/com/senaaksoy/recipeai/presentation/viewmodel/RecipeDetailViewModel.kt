package com.senaaksoy.recipeai.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senaaksoy.recipeai.data.repository.RecipeRepository
import com.senaaksoy.recipeai.presentation.state.RecipeDetailState
import com.senaaksoy.recipeai.utills.Resource
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
        savedStateHandle.get<Int>("recipeId")?.let { recipeId ->
            Log.d("RecipeDetailVM", "🔵 Loading recipe ID: $recipeId")
            loadRecipe(recipeId)
        }
    }

    private fun loadRecipe(recipeId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            Log.d("RecipeDetailVM", "⏳ Fetching recipe...")

            when (val result = repository.getRecipeById(recipeId)) {
                is Resource.Success -> {
                    Log.d("RecipeDetailVM", "✅ Recipe loaded: ${result.data?.name}")
                    _state.value = _state.value.copy(
                        recipe = result.data,
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    Log.e("RecipeDetailVM", "❌ Error: ${result.message}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    Log.d("RecipeDetailVM", "⏳ Still loading...")
                }
            }
        }
    }

}