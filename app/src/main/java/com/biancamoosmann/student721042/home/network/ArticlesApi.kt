package com.biancamoosmann.student721042.home.network


import com.biancamoosmann.student721042.home.Screens.LoginResponse
import com.biancamoosmann.student721042.home.data.model.ArticleResults
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface ArticlesApi {

    //All articles
    @GET("articles")
    fun getArticles(): Call<ArticleResults>


    //Article Detail
    @GET("articles/{id}")
    fun getArticle(@Path("id") articleId: Int): Call<ArticleResults>

    @PUT("articles/{id}/like")
    fun likeArticle(@Path("id") articleId: Int): Call<Unit>


    @DELETE("articles/{id}/like")
    fun dislikeArticle(@Path("id") articleId: Int): Call<Unit>

    //my favorites
    @GET("Articles/liked")
    fun getLikedArticles(): Call<ArticleResults>

    @POST("Users/login")
    fun login(): Call<LoginResponse>


}