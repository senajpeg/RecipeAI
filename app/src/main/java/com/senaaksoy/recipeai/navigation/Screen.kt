package com.senaaksoy.recipeai.navigation

enum class Screen(val route: String) {
    SplashScreen(route = "SplashScreen"),
    SignInScreen(route = "SignInScreen"),
    SignUpScreen(route = "SignUpScreen"),
    HomeScreen(route = "HomeScreen"),
    RecipeDetailScreen(route = "RecipeDetailScreen/{recipeId}"),
    AddRecipeScreen(route = "AddRecipeScreen"),
    FavoritesScreen(route = "FavoritesScreen"),
    ForgotPasswordScreen(route = "ForgotPasswordScreen"),
    ProfileScreen(route = "ProfileScreen"),
    ResetPasswordScreen(route = "ResetPasswordScreen?oobCode={token}");

    companion object {
        fun createRecipeDetailRoute(recipeId: Int): String {
            return "RecipeDetailScreen/$recipeId"
        }
        //Reset password route oluşturucu
        fun createResetPasswordRoute(token: String): String {
            return "ResetPasswordScreen/$token"
        }
    }
}