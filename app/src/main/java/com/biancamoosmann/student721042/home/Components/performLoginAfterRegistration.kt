package com.biancamoosmann.student721042.home.Components

import android.content.Context
import com.biancamoosmann.student721042.home.Screens.LoginResponse
import com.biancamoosmann.student721042.home.ViewModel.LoggedInViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request


suspend fun performLoginAfterRegistration(
    username: String,
    password: String,
    context: Context,
    viewModel: LoggedInViewModel
): Result<String> {
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
                // save Login-State in SharedPreferences
                val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putBoolean("isLoggedIn", true)
                editor.apply()
                editor.putString("authToken", authToken)
                editor.putString("username", username)  // save username, authToken in sharedPref
                editor.apply()
                return Result.success(authToken) // successful registration
            } else {
                // Handle login failure here
                return Result.failure(Exception("Login failed"))
            }
        }
    }
    // Handle HTTP request failure here
    return Result.failure(Exception("HTTP request failure"))
}