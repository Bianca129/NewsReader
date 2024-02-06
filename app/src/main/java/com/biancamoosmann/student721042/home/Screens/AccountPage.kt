package com.biancamoosmann.student721042.home.Screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.biancamoosmann.student721042.R
import com.biancamoosmann.student721042.home.ViewModel.HomeViewModel
import com.biancamoosmann.student721042.home.ViewModel.LoggedInViewModel
import com.biancamoosmann.student721042.home.data.SharedPreferencesManager
import com.biancamoosmann.student721042.home.data.UserData
import com.biancamoosmann.student721042.home.navigation.Screens

@Composable
fun AccountPage(
    navController: NavHostController,
    viewModel: LoggedInViewModel = LoggedInViewModel(),
    context: Context,
    homeViewModel: HomeViewModel
) {
    val isLoggedIn = SharedPreferencesManager.sharedPreferences.getBoolean("isLoggedIn", false)
    if (isLoggedIn) {
        // display for user who is logged in
        LoggedInView(navController, context)
    } else {
        // show display for login
        LoginPage(navController, viewModel, context, homeViewModel)
    }
}



@Composable
fun LoggedInView(navController: NavHostController, context: Context) {
    val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
    val username = sharedPref.getString("username", "DefaultUsername")
    if (isLoggedIn) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // show username
            Text(
                text = stringResource(id = R.string.hello),
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                text = username.toString(),
                style = MaterialTheme.typography.displaySmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Logout-Button
            Button(
                onClick = {
                    val editor = sharedPref.edit()
                    editor.putBoolean("isLoggedIn", false)
                    editor.apply()
                    // delete username and authToken
                    UserData.username = ""
                    clearAuth(context)
                    navController.navigate(Screens.Login.route)
                }
            ) {
                Text(text = "Logout")
            }
        }
    } else {
        // If user is not logged in:
        Text(text = stringResource(id = R.string.notLoggedIn),
            modifier = Modifier.padding(16.dp))
    }
}




fun clearAuth(context: Context) {
    val sharedPrefAuth = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val editorAuth = sharedPrefAuth.edit()
    // Delete authToken and username from SharedPreferences
    editorAuth.remove("authToken")
    editorAuth.remove("username")
    // Save the changes
    editorAuth.apply()
    HomeViewModel().resetData()
}