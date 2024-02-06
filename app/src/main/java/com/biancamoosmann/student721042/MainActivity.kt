package com.biancamoosmann.student721042


import RegisterPage
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.biancamoosmann.student721042.home.Screens.AccountPage
import com.biancamoosmann.student721042.home.Screens.ArticleList
import com.biancamoosmann.student721042.home.Screens.DetailScreen
import com.biancamoosmann.student721042.home.Screens.LoginPage
import com.biancamoosmann.student721042.home.Screens.ShowArticlesByCategory
import com.biancamoosmann.student721042.home.Screens.ShowFav
import com.biancamoosmann.student721042.home.ViewModel.HomeViewModel
import com.biancamoosmann.student721042.home.ViewModel.LoggedInViewModel
import com.biancamoosmann.student721042.home.data.model.BottomNavigationItem
import com.biancamoosmann.student721042.home.data.SharedPreferencesManager
import com.biancamoosmann.student721042.home.navigation.Screens
import com.biancamoosmann.student721042.ui.theme.Student721042Theme
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels()


    @SuppressLint("CoroutineCreationDuringComposition", "RememberReturnType")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferencesManager.init(this)
        lifecycleScope.launch {
            // only load by starting the app
            viewModel.fetchData(applicationContext, shouldLoadLikedArticles = true)


        }
        val newViewModel = HomeViewModel()
        newViewModel.fetchData(applicationContext, shouldLoadLikedArticles = true)


        setContent {
            Student721042Theme() {
                val navController = rememberNavController()
                val loggedInViewModel = viewModel<LoggedInViewModel>()
                var selectedItemIndex by remember { mutableIntStateOf(0) }


                val items = listOf(
                    BottomNavigationItem(
                        title = stringResource(id = R.string.home),
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home,
                        hasNews = false
                    ),
                    BottomNavigationItem(
                        title = stringResource(id = R.string.favorite),
                        selectedIcon = Icons.Filled.Favorite,
                        unselectedIcon = Icons.Outlined.FavoriteBorder,
                        hasNews = true
                    ),
                    BottomNavigationItem(
                        title = stringResource(id = R.string.account),
                        selectedIcon = Icons.Filled.AccountCircle,
                        unselectedIcon = Icons.Outlined.AccountCircle,
                        hasNews = false
                    ),
                )

                viewModel.errorMessage.observe(this) { errorMessage ->
                    if (!errorMessage.isNullOrEmpty()) {
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    content = { innerPadding ->
                        Column(
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = Screens.Home.route,
                            ) {


                                composable(Screens.Home.route) {
                                    ArticleList(navController, viewModel, applicationContext)
                                    selectedItemIndex = 0
                                }


                                composable(Screens.Favorites.route) {
                                    ShowFav(navController, viewModel, applicationContext)
                                    selectedItemIndex = 1
                                }

                                composable(Screens.Account.route) {

                                    AccountPage(navController, loggedInViewModel, applicationContext, newViewModel)
                                    selectedItemIndex = 2
                                }

                                composable(Screens.Login.route) {
                                    LoginPage(navController, loggedInViewModel, applicationContext, newViewModel)
                                    selectedItemIndex = 2
                                }

                                composable(Screens.Register.route) {
                                   RegisterPage(navController, loggedInViewModel, applicationContext, newViewModel)
                                    selectedItemIndex = 2
                                }

                                composable(Screens.Category.route) { backStackEntry ->
                                    val arguments = requireNotNull(backStackEntry.arguments)
                                    val categoryIdString = arguments.getString("categoryId")
                                    val categoryName = arguments.getString("categoryName")
                                    val categoryId = categoryIdString?.toIntOrNull()
                                    if (categoryId != null) {
                                        if (categoryName != null) {
                                            ShowArticlesByCategory(navController, viewModel, categoryId, categoryName)
                                        }
                                    } else {
                                        val errorMessage = applicationContext.resources.getString(R.string.error_show_articles_category)
                                        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
                                    }
                                }

                                composable(Screens.ArticleDetail.route) { backStackEntry ->
                                    val arguments = requireNotNull(backStackEntry.arguments)
                                    val articleId = arguments.getString("articleId")

                                    val article = viewModel.listArticle.find { it.Id == articleId?.toIntOrNull() }

                                    if (article != null) {
                                        DetailScreen(article = article, navController = navController, viewModel)
                                    }
                                }
                            }
                        }
                    },

                    bottomBar = {
                        NavigationBar (
                            containerColor = MaterialTheme.colorScheme.background
                        )
                        {
                            items.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    selected = selectedItemIndex == index || (selectedItemIndex == 2 && (index == 3 || index == 4)),
                                    onClick = {
                                        selectedItemIndex = index
                                        when (index) {
                                            0 -> navController.navigate(Screens.Home.route)
                                            1 -> navController.navigate(Screens.Favorites.route)
                                            2 -> navController.navigate(Screens.Account.route)
                                            3 -> navController.navigate(Screens.Login.route)
                                            4 -> navController.navigate(Screens.Register.route)
                                            5 -> navController.navigate(Screens.Category.route)
                                        }
                                    },
                                    label = {
                                        Text(text = item.title)
                                    },
                                    alwaysShowLabel = true,

                                    icon = {
                                        Icon(
                                            imageVector = if (index == selectedItemIndex) {
                                                item.selectedIcon
                                            } else item.unselectedIcon,
                                            contentDescription = item.title,
                                            tint = if (index == selectedItemIndex) {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}


