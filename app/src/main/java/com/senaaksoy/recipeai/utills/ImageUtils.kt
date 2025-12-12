package com.senaaksoy.recipeai.utills

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream

object ImageUtils {
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) {
                Log.e("ImageUtils", "Failed to decode bitmap from URI")
                return null
            }

            val scaledBitmap = scaleBitmap(bitmap, 800)

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

            val bytes = outputStream.toByteArray()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

            Log.d("ImageUtils", "Base64 created - length: ${base64.length}")
            base64
        } catch (e: Exception) {
            Log.e("ImageUtils", "uriToBase64 error", e)
            null
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxSize && height <= maxSize) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (ratio > 1) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun base64ToBitmap(base64: String?): Bitmap? {
        if (base64.isNullOrEmpty()) {
            Log.w("ImageUtils", "Base64 string is null or empty")
            return null
        }

        return try {
            var cleanBase64 = base64
                .trim()
                .replace("\n", "")
                .replace("\r", "")
                .replace(" ", "")
                .replace("\t", "")

            if (cleanBase64.startsWith("data:image")) {
                val commaIndex = cleanBase64.indexOf(",")
                if (commaIndex != -1) {
                    cleanBase64 = cleanBase64.substring(commaIndex + 1)
                    Log.d("ImageUtils", "Removed data URI prefix")
                }
            }

            Log.d("ImageUtils", "Attempting to decode base64 - length: ${cleanBase64.length}")
            Log.d("ImageUtils", "First 50 chars: ${cleanBase64.take(50)}")

            val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            Log.d("ImageUtils", "Decoded ${bytes.size} bytes")

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            if (bitmap == null) {
                Log.e("ImageUtils", "Failed to decode bitmap from bytes")
            } else {
                Log.d("ImageUtils", "Bitmap decoded successfully: ${bitmap.width}x${bitmap.height}")
            }

            bitmap
        } catch (e: IllegalArgumentException) {
            Log.e("ImageUtils", "Invalid base64 format", e)
            null
        } catch (e: Exception) {
            Log.e("ImageUtils", "base64ToBitmap error", e)
            null
        }
    }
}