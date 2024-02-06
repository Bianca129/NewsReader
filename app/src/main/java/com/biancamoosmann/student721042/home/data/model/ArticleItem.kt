package com.biancamoosmann.student721042.home.data.model

data class ArticleItem(
    val Id: Int,
    val Feed: Int,
    val Title: String,
    val Summary: String,
    val PublishDate: String,
    val Image: String,
    val Url: String,
    val Related: List<String>,
    val Categories: List<Category>,
    var IsLiked: Boolean,
    val NextId: Int
)