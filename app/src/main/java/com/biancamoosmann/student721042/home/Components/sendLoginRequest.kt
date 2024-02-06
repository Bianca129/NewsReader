package com.biancamoosmann.student721042.home.Components

import android.content.Context
import android.widget.Toast
import com.biancamoosmann.student721042.R
import com.biancamoosmann.student721042.home.Screens.LoginResponse
import com.biancamoosmann.student721042.home.ViewModel.HomeViewModel
import com.biancamoosmann.student721042.home.ViewModel.LoggedInViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request


suspend fun sendLoginRequest(
    username: String,
    password: String,
    context: Context,
    viewModel: LoggedInViewModel,
    onComplete: (Result<String>) -> Unit
) {
    val  homeViewModel= HomeViewModel()
    val apiUrl = "https://inhollandbackend.azurewebsites.net/api/Users/login"

    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val authToken = getAuthTokenFromSharedPreferences(context)

            val requestWithAuth = originalRequest.newBuilder()
                .apply {
                    authToken?.let {
                        header("x-authtoken", it)
                    }
                }
                .build()

            chain.proceed(requestWithAuth)
        }
        .build()

    val requestBody = FormBody.Builder()
        .add("UserName", username)
        .add("Password", password)
        .build()

    val request = Request.Builder()
        .url(apiUrl)
        .post(requestBody)
        .build()

    val response = withContext(Dispatchers.IO) {
        client.newCall(request).execute()
    }

    if (response.isSuccessful) {
        val responseBody = response.body?.string()
        val gson = Gson()
        if (responseBody != null) {
            val responseJson = gson.fromJson(responseBody, LoginResponse::class.java)
            if (responseJson.AuthToken != null) {
                val authToken = responseJson.AuthToken

                viewModel.setLoggedIn(true)

                val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putBoolean("isLoggedIn", true)
                editor.putString("authToken", authToken)
                editor.putString("username", username)
                editor.apply()
                homeViewModel.fetchData(context, shouldLoadLikedArticles = true)
                Toast.makeText(context, R.string.login200, Toast.LENGTH_SHORT).show()
                onComplete(Result.success(authToken))
            } else {
                // Handle login failure here
                onComplete(Result.failure(Exception("Login failed")))
            }
        }
    } else {
        onComplete(Result.failure(Exception("HTTP request failure")))
    }
}


