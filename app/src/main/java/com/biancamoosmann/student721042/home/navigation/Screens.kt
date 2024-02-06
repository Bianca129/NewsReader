package com.biancamoosmann.student721042.home.navigation


sealed class Screens(val route: String) {
    object Home : Screens("Home")
    object Favorites : Screens("Favorites")
    object Login : Screens("Login")
    object Account : Screens("Account")
    object Register : Screens("Register")
    object ArticleDetail : Screens("article_detail/{articleId}")
    data class Category(val categoryId: Int, val categoryName: String) : Screens("category/{categoryId}/{categoryName}") {
        companion object {
            val route: String = "category/{categoryId}/{categoryName}"
        }
    }
}
