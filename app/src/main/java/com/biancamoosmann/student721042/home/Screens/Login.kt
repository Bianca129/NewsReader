package com.biancamoosmann.student721042.home.Screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.biancamoosmann.student721042.MyApp.RetrofitInstance.appContext
import com.biancamoosmann.student721042.R
import com.biancamoosmann.student721042.home.ViewModel.HomeViewModel
import com.biancamoosmann.student721042.home.ViewModel.LoggedInViewModel
import com.biancamoosmann.student721042.home.data.UserData
import com.biancamoosmann.student721042.home.Components.sendLoginRequest
import com.biancamoosmann.student721042.home.Components.showToast
import com.biancamoosmann.student721042.home.navigation.Screens
import kotlinx.coroutines.launch


data class LoginResponse(
    val AuthToken: String?
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(
    navController: NavHostController,
    viewModel: LoggedInViewModel,
    context: Context,
    newViewModel: HomeViewModel
) {
    val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false) // OnCreate (beim Start der APP)
    // State of errorMessage
    val errorMessage = remember { mutableStateOf<String>("") }
    // State of errorMessage
    val isLoading = remember { mutableStateOf(false) }

    if (!isLoggedIn) {
        Box(modifier = Modifier.fillMaxSize()) {
            ClickableText(
                text = AnnotatedString(text = stringResource(id = R.string.sign)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp),
                onClick = {
                    // Navigate to RegisterPage when the "Sign up here" text is clicked
                    navController.navigate(Screens.Register.route)
                },
                style = TextStyle(
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.Underline,
                    color = Color.Blue
                )
            )
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val username = remember { mutableStateOf(TextFieldValue()) }
            val password = remember { mutableStateOf(TextFieldValue()) }

            Text(text = "Login", style = TextStyle(fontSize = 40.sp))

            Spacer(modifier = Modifier.height(50.dp))

            // Textfield for username
            TextField(
                label = { Text(text = stringResource(id = R.string.labelUser)) },
                value = username.value,
                onValueChange = { username.value = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Textfield for password
            TextField(
                label = { Text(text = stringResource(id = R.string.labelPass)) },
                value = password.value,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                onValueChange = { password.value = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Button for Login
            Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
                Button(
                    onClick = {
                        val usernameText = username.value.text
                        val passwordText = password.value.text

                        viewModel.viewModelScope.launch {
                            sendLoginRequest(usernameText, passwordText, context, viewModel) { result ->
                                result.onSuccess {
                                    UserData.username = usernameText
                                    // Navigation to Homepage
                                    navController.navigate(Screens.Home.route)

                                    isLoading.value = true // Active Loading indicator

                                    newViewModel.fetchData(appContext, shouldLoadLikedArticles = true) { fetchResult ->
                                        if (fetchResult != null) {
                                            if (fetchResult.isSuccess) {
                                                isLoading.value = false
                                            } else {
                                                showToast(context.getString(R.string.error_fetchData) + " ${fetchResult.exceptionOrNull()?.localizedMessage}", context)
                                            }
                                        } else {
                                            showToast(R.string.error_general, context)
                                        }
                                    }
                                }
                                result.onFailure {
                                    errorMessage.value = context.getString(R.string.errorLogin)
                                }
                            }
                        }

                    },
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = stringResource(id = R.string.login))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (errorMessage.value.isNotEmpty()) {
                Text(text = errorMessage.value, color = Color.Red)
            }
        }
    } else {
        // if already logged in, show LoggedInView
        LoggedInView(navController, context)
    }
}
