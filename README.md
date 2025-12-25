🥗 RecipeAI - AI Powered Recipe Assistant

RecipeAI is a modern Android application that combines the power of **Google Gemini AI** with a rich culinary database. It allows users to generate creative recipes based on ingredients, browse thousands of meals, and manage their favorite recipes seamlessly with **Offline-First** capability.

## 🚀 Key Features

* **🤖 AI Chef:** Generate custom recipes using Gemini AI by entering ingredients.
* **📡 Offline-First:** Save favorites locally and auto-sync with the server when back online (powered by WorkManager).
* **🌍 Multi-Source Data:** Integration with TheMealDB API and a custom backend.
* **🔐 Secure Auth:** Custom Authentication (JWT) + Google Sign-In support.
* **🗣️ Dynamic Translation:** Auto-translation of recipes for localized experience.
* **🎨 Modern UI:** Built entirely with Jetpack Compose & Material 3.

## 🛠️ Tech Stack & Architecture

This project follows **Modern Android Development (MAD)** standards:

* **Language:** Kotlin
* **UI:** Jetpack Compose, Material 3, Navigation Compose
* **Architecture:** MVVM, Clean Architecture, Repository Pattern
* **DI:** Hilt (Dagger)
* **Network:** Retrofit, OkHttp, Gson
* **Local Data:** Room Database, DataStore
* **Background:** WorkManager (for reliable data sync)
* **AI:** Google Gemini API
* **Image Loading:** Coil
