package com.senaaksoy.recipeai.navigation

enum class Screen(val route: String) {
    SplashScreen(route = "SplashScreen"),
    SignInScreen(route = "SignInScreen"),
    SignUpScreen(route = "SignUpScreen"),
    HomeScreen(route = "HomeScreen"),
    RecipeDetailScreen(route = "RecipeDetailScreen/{recipeId}"),
    AddRecipeScreen(route = "AddRecipeScreen"),
    FavoritesScreen(route = "FavoritesScreen"),
    SearchScreen(route = "SearchScreen"),
    ForgotPasswordScreen(route = "ForgotPasswordScreen"),
    ProfileScreen(route = "ProfileScreen"),
    ResetPasswordScreen(route = "ResetPasswordScreen?oobCode={oobCode}");

    companion object {
        fun createRecipeDetailRoute(recipeId: Int): String {
            return "RecipeDetailScreen/$recipeId"
        }
    }
}