package com.senaaksoy.recipeai.utills

import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationManager @Inject constructor() {

    private var translator: Translator? = null
    private var isModelDownloaded = false

    init {
        initializeTranslator()
    }

    private fun initializeTranslator() {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.TURKISH)
            .build()

        translator = Translation.getClient(options)

        // Model'i indir
        val conditions = DownloadConditions.Builder()
            .requireWifi() // Sadece WiFi'da indir (opsiyonel)
            .build()

        translator?.downloadModelIfNeeded(conditions)
            ?.addOnSuccessListener {
                isModelDownloaded = true
                Log.d("TranslationManager", "Translation model downloaded successfully")
            }
            ?.addOnFailureListener { exception ->
                Log.e("TranslationManager", "Failed to download translation model", exception)
            }
    }

    suspend fun translate(text: String): String {
        return try {
            if (text.isBlank()) return text

            // Model henüz indirilmediyse bekle
            if (!isModelDownloaded) {
                translator?.downloadModelIfNeeded()?.await()
                isModelDownloaded = true
            }

            // Çeviri yap
            translator?.translate(text)?.await() ?: text

        } catch (e: Exception) {
            Log.e("TranslationManager", "Translation failed: ${e.message}")
            text // Hata durumunda orijinal metni döndür
        }
    }
}