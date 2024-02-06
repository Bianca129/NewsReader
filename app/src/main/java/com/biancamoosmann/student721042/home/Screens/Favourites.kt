package com.biancamoosmann.student721042.home.Screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.biancamoosmann.student721042.R
import com.biancamoosmann.student721042.home.ViewModel.HomeViewModel
import com.biancamoosmann.student721042.home.data.SharedPreferencesManager

@Composable
fun ShowFav(
    navController: NavController,
    articleViewModel: HomeViewModel,
    appContext: Context
) {

    val login = SharedPreferencesManager.LoginStatus()

    LaunchedEffect(Unit) {
        if (login) {
           articleViewModel.fetchLikedArticles(appContext)
        }
    }
    val likedArticleList by rememberUpdatedState(articleViewModel.listLikedArticle)
    val isLoading by articleViewModel.isLoading.observeAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp)
                    .height(60.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.favPage),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold

                )
                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = {
                    articleViewModel.fetchLikedArticles(appContext)
                }) {
                    // Reload-Button
                    val reload: Painter = painterResource(id = R.drawable.reload)
                    Image(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        painter = reload,
                        contentDescription = null
                    )
                }

            }
            if (!login) {
                Text(
                    text = stringResource(id = R.string.notLoggedIn),
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            } else {
                if (isLoading == true) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .wrapContentSize(Alignment.Center)
                    )
                } else {
                    if (likedArticleList.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.noArticles),
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            items(likedArticleList) { article ->
                                CreateCard(article, navController, articleViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
