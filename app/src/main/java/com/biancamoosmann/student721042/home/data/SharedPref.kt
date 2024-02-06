package com.biancamoosmann.student721042.home.data

import android.content.Context
import android.content.SharedPreferences

object  SharedPreferencesManager {
    lateinit var sharedPreferences: SharedPreferences
    fun init(context: Context){
        sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    fun getAuthToken() : String?{
        val authToken = sharedPreferences.getString("authToken", null)
        return authToken
    }

    fun LoginStatus(): Boolean {
        val loggedIn = sharedPreferences.getBoolean("isLoggedIn", false) // Hier verwenden wir getBoolean und den Standardwert (false)
        return loggedIn
    }

}