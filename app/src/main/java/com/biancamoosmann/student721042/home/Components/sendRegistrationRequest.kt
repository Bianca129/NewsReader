package com.biancamoosmann.student721042.home.Components

import android.content.Context
import android.widget.Toast
import androidx.navigation.NavHostController
import com.biancamoosmann.student721042.R
import com.biancamoosmann.student721042.home.ViewModel.LoggedInViewModel
import com.biancamoosmann.student721042.home.data.model.RegistrationResponse
import com.biancamoosmann.student721042.home.data.model.UserRegistration
import com.biancamoosmann.student721042.home.navigation.Screens
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.HttpException
import java.net.SocketTimeoutException

@OptIn(DelicateCoroutinesApi::class)
fun sendRegistrationRequest(
    userRegistration: UserRegistration,
    navController: NavHostController,
    viewModel: LoggedInViewModel,
    context: Context
) {
    val apiUrl = "https://inhollandbackend.azurewebsites.net/api/Users/register"

    GlobalScope.launch(Dispatchers.Main) {
        try {
            val response = withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(apiUrl)
                    .post(
                        FormBody.Builder()
                            .add("UserName", userRegistration.userName)
                            .add("Password", userRegistration.password)
                            .build()
                    )
                    .build()

                client.newCall(request).execute()
            }

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val gson = Gson()
                val responseJson = gson.fromJson(responseBody, RegistrationResponse::class.java)
                if (responseJson?.Message != null) {
                    if (responseJson.Message == "User registered") {
                        //Registration was successful --> perform Login
                        val loginResult = performLoginAfterRegistration(userRegistration.userName, userRegistration.password, context, viewModel)

                        if (loginResult.isSuccess) {
                            // navigate to HomePage
                            navController.navigate(Screens.Home.route)
                            Toast.makeText(context, R.string.registration200, Toast.LENGTH_SHORT).show()
                        } else {
                            // Handle login failure here
                            showToast(R.string.error_authentication_required, context)
                        }
                    } else {
                        //Error Message from the JSON
                        showToast(responseJson.Message, context)
                    }
                } else {
                    // Registration was successful; you can navigate to the next screen or take other actions
                    navController.navigate(Screens.Login.route)
                    // Now, perform login after registration
                    val loginResult = performLoginAfterRegistration(userRegistration.userName, userRegistration.password, context, viewModel)
                    if (loginResult.isFailure) {
                        when (val loginError = loginResult.exceptionOrNull()) {
                            is HttpException -> {
                                val errorMessage = when (loginError.code()) {
                                    401 -> R.string.error_authentication_required
                                    403 -> R.string.error_access_denied
                                    404 -> R.string.error_data_not_found
                                    500 -> R.string.error_internal_server
                                    else -> R.string.error_unknown
                                }
                                // Display the appropriate toast message
                                showToast(errorMessage, context)
                            }
                            is SocketTimeoutException -> {
                                // Handle timeout error
                                showToast(R.string.error_timeout, context)
                            }
                            else -> {
                                // Handle other errors (generic error)
                                showToast(R.string.error_unknown_generic, context)
                            }
                        }
                    }

                }
            } else {
                // Handle HTTP request failure and show an appropriate error message to the user
                showToast(R.string.error_unknown_register, context)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exceptions here, e.g., network issues
            showToast(R.string.error_network_issue, context)
        }
    }
}