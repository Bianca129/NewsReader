package com.biancamoosmann.student721042.home.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.biancamoosmann.student721042.R
import com.biancamoosmann.student721042.home.ViewModel.HomeViewModel
import com.biancamoosmann.student721042.home.data.model.ArticleItem

@Composable
fun ShowArticlesByCategory(
    navController: NavController,
    articleViewModel: HomeViewModel,
    category: Int,
    categoryName: String
) {
    val articlesState = articleViewModel.listArticle
    val articlesByCategory = articlesState.filter { article ->
        article.Categories.any { it.Id == category }
    }

    Column {
        CategoryScreenTopBar(categoryName){
            navController.popBackStack()
        }
        if (articlesByCategory.isEmpty()) {
            Text(
                text = stringResource(id = R.string.noArticlesCategory),
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(0.dp)
            ) {
                items(articlesByCategory) { article ->
                    CreateCard(article, navController, articleViewModel)
                }
            }
        }
    }
}

@Composable
fun CategoryScreenTopBar(categoryName: String, onBackClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp)
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    onBackClicked()
                }
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }

            Text(
                text = categoryName,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}


@Composable
fun CategoryButtons(article: ArticleItem, navController: NavController) {
    LazyRow(
        modifier = Modifier.padding(16.dp),
        content = {
            items(article.Categories) { category ->
                Button(
                    onClick = {
                        //Category Id and name for the Navigation
                        val route = "category/${category.Id}/${category.Name}"
                        navController.navigate(route)
                    },
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text(text = category.Name)
                }
            }
        }
    )
}