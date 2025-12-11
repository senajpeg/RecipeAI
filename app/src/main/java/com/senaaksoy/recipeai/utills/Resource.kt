package com.senaaksoy.recipeai.utills

sealed class Resource<T>(
    val data: T? = null,
    val token: String? = null,
    val message: String? = null
) {
    class Success<T>(data: T, token: String? = null) : Resource<T>(data, token)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, null, message)
    class Loading<T> : Resource<T>()
}



