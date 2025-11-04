package com.senaaksoy.recipeai.data.remote

import retrofit2.Response

suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<T>
): Resource<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Resource.Success(body)
            } else {
                Resource.Error("Yanıt boş")
            }
        } else {
            Resource.Error("Hata: ${response.code()} - ${response.message()}")
        }
    } catch (e: Exception) {
        Resource.Error("Bağlantı hatası: ${e.localizedMessage}")
    }
}