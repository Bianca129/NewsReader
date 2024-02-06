package com.biancamoosmann.student721042.home.Components

import android.content.Context
import android.widget.Toast

fun showToast(message: String, context: Context) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun showToast(errorMessageResId: Int, context: Context) {
    Toast.makeText(context, errorMessageResId, Toast.LENGTH_SHORT).show()
}