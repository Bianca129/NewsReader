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
import com.biancamoosmann.student721042.home.Components.sendRegistrationRequest
import com.biancamoosmann.student721042.home.ViewModel.HomeViewModel
import com.biancamoosmann.student721042.home.ViewModel.LoggedInViewModel
import com.biancamoosmann.student721042.home.Components.showToast
import com.biancamoosmann.student721042.home.data.model.UserRegistration
import com.biancamoosmann.student721042.home.navigation.Screens
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPage(
    navController: NavHostController,
    viewModel: LoggedInViewModel,
    context: Context,
    newViewModel : HomeViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        ClickableText(
            text = AnnotatedString(text = stringResource(id = R.string.loginHere)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp),
            onClick = {
                // Navigate to RegisterPage when the "Sign up here" text is clicked
                navController.navigate(Screens.Login.route)
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

        Text(text = stringResource(id = R.string.reg), style = TextStyle(fontSize = 40.sp))

        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            label = { Text(text = stringResource(id = R.string.labelUser)) },
            value = username.value,
            onValueChange = { username.value = it })

        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            label = { Text(text = stringResource(id = R.string.labelPass)) },
            value = password.value,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            onValueChange = { password.value = it })

        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            Button(
                onClick = {
                    val usernameText = username.value.text
                    val passwordText = password.value.text

                    viewModel.viewModelScope.launch {
                        // Send registrationrequest
                        val userRegistration = UserRegistration(usernameText, passwordText)
                        sendRegistrationRequest(userRegistration, navController, viewModel, context)

                        // if successful go to Homescreen
                        //navController.navigate(Screens.Home.route)

                        newViewModel.fetchData(appContext, shouldLoadLikedArticles = true) { fetchResult ->
                            if (fetchResult != null) {

                                if (!fetchResult.isSuccess) {
                                    showToast(context.getString(R.string.error_fetchData) + " ${fetchResult.exceptionOrNull()?.localizedMessage}", context)
                                }
                            } else {
                                showToast(R.string.error_null, context)
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = stringResource(id = R.string.reg))
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
