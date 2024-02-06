package com.biancamoosmann.student721042.home.Components

import android.content.Context

fun getAuthTokenFromSharedPreferences(context: Context): String? {
    val sharedPrefAuth = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return sharedPrefAuth.getString("authToken", null)
}