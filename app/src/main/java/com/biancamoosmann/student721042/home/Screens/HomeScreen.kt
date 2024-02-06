package com.biancamoosmann.student721042.home.Screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.biancamoosmann.student721042.R
import com.biancamoosmann.student721042.home.ViewModel.HomeViewModel
import com.biancamoosmann.student721042.home.data.model.ArticleItem
import com.biancamoosmann.student721042.home.data.SharedPreferencesManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@Composable
fun ArticleList(navController: NavController, viewModel: HomeViewModel, appContext: Context) {
    val login = SharedPreferencesManager.LoginStatus()

    LaunchedEffect(Unit) {
    if(login){
        viewModel.fetchData(appContext, true)
    }else{
        viewModel.fetchData(appContext, false)
    }


    }
    val allArticles by rememberUpdatedState(viewModel.listArticle)
    val isLoading by viewModel.isLoading.observeAsState(true)
    val isLoadingMoreArticles by viewModel.isLoadingMoreArticles.observeAsState(false)
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val listState = rememberLazyListState()

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(end = 0.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                //.background(MaterialTheme.colorScheme.background)
                .padding(end = 0.dp),
        )

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 0.dp, top = 0.dp, bottom = 0.dp)
                    .height(60.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.homePage),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { viewModel.fetchData(appContext, shouldLoadLikedArticles = true) }) {
                    // Reload-Button
                    val reload: Painter = painterResource(id = R.drawable.reload)
                    Image(
                        modifier = Modifier.background(MaterialTheme.colorScheme.background),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        painter = reload,
                        contentDescription = null
                    )
                }

                IconButton(onClick = { viewModel.toggleDarkMode() }, modifier = Modifier.padding(end = 0.dp)) {
                    val iconResource = if (isDarkMode) R.drawable.lightmode else R.drawable.darkmode
                    val mode: Painter = painterResource(id = iconResource)
                    Image(painter = mode, contentDescription = null, Modifier.padding(end = 0.dp))
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .wrapContentSize(Alignment.Center))
            } else if (allArticles.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.noArticles),
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                var openDialog by remember { mutableStateOf(false) }
                val options = listOf("Feed 1", "Feed 2", "Feed 3")
                val tempSelectedOptions = remember { mutableStateListOf<Boolean>() }
                val selectedOptions = remember { mutableStateListOf(true, true, true) }
                val onCloseDialog: () -> Unit = {
                    openDialog = false
                    // Copy temporary data from selectfield
                    selectedOptions.clear()
                    selectedOptions.addAll(tempSelectedOptions)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.filter),
                        contentDescription = null,
                        modifier =
                        Modifier
                            .size(24.dp)
                            .clickable {
                                tempSelectedOptions.clear()
                                tempSelectedOptions.addAll(selectedOptions)
                                openDialog = true
                            },
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                if (openDialog) {
                    AlertDialog(
                        onDismissRequest = { onCloseDialog() },
                        title = { Text("Select Options") },
                        buttons = {
                            Button(
                                onClick = {
                                    onCloseDialog()
                                },
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text("Done")
                            }
                        },
                        text = {
                            Column(modifier = Modifier.padding(16.dp)) {
                                for ((index, option) in options.withIndex()) {
                                    if (option == "Feed 1" || option == "Feed 3") {
                                        Row(
                                            modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    val feedIndex = options.indexOf(option)
                                                    if (feedIndex != -1) {
                                                        tempSelectedOptions[feedIndex] =
                                                            !tempSelectedOptions[feedIndex]
                                                    }
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = tempSelectedOptions[index],
                                                onCheckedChange = { isChecked ->
                                                    val feedIndex = options.indexOf(option)
                                                    if (feedIndex != -1) {
                                                        tempSelectedOptions[feedIndex] = isChecked
                                                    }
                                                }
                                            )
                                            Text(option)
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
                val filteredArticles =
                    viewModel.listArticle.filter { article ->
                        val feedIndex = article.Feed - 1 // Da Indizes bei 0 beginnen, subtrahiere 1

                        if (feedIndex >= 0 && feedIndex < selectedOptions.size) {
                            val selectedOption = selectedOptions[feedIndex]
                            selectedOption
                        } else {
                            // not selected
                            false
                        }
                    }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 35.dp)
                        ) {
                            items(filteredArticles) { article ->
                                CreateCard(article, navController, viewModel)
                            }

                            // Display loading indicator only when articles are being loaded
                            if (isLoadingMoreArticles) {
                                item {
                                    Spacer(modifier = Modifier.height(10.dp)) // Adjustable distance after the last article
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomEnd)
                                            .padding(end = 0.dp, bottom = 0.dp, top = 0.dp)
                                    ) {
                                        ShowLoading()
                                    }
                                }
                            }
                        }
                    }

                    val remainingAttempts = 20

                    LaunchedEffect(listState, isLoadingMoreArticles) {
                        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                            .collect { lastIndex ->
                                val totalArticles = filteredArticles.size
                                val shouldFetchMore = lastIndex == totalArticles - 1 &&
                                        viewModel._nextIdForMoreArticles.value != null &&
                                        !isLoadingMoreArticles!!

                                if (shouldFetchMore) {
                                    viewModel.fetchMoreArticles(
                                        appContext,
                                        viewModel._nextIdForMoreArticles.value!!,
                                        remainingAttempts
                                    )
                                }
                            }
                    }
                }
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun CreateCard(
    article: ArticleItem,
    navController: NavController,
    viewModel: HomeViewModel)
{
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
    val sharedPrefAuth = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val auth = sharedPrefAuth.getString("authToken", null)

    // Use mutableStateOf to track isFavorite
    var isFavorite by remember { mutableStateOf(article.IsLiked) }
    val errorMessageLike = stringResource(id = R.string.error_like)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .padding(bottom = 2.dp)
            .padding(top = 0.dp),
        shape = RoundedCornerShape(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                    navController.navigate("article_detail/${article.Id}")
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bild
            Box(
                modifier = Modifier
                    .size(100.dp)
            ) {
                val painter = rememberAsyncImagePainter(model = article.Image)

                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .clip(MaterialTheme.shapes.medium),
                text = article.Title,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 15.sp,
                maxLines = 4,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Clip
            )

            if (isLoggedIn && auth != null) {
                IconButton(
                    onClick = {
                        GlobalScope.launch(Dispatchers.Main) {
                            viewModel.toggleFavoriteStatus(article) { success ->
                                if (success) {
                                    // Change State from isFavorite
                                    isFavorite = article.IsLiked

                                    if (!isFavorite) {
                                        viewModel.removeLikedArticle(article)
                                    }

                                } else {
                                    Toast.makeText(
                                        context,
                                        errorMessageLike,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                ) {
                    val iconResource =
                        if (isFavorite) {
                            R.drawable.favorite_filled
                        } else {
                            R.drawable.favorite_border
                        }
                    Icon(
                        painter = painterResource(id = iconResource),
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.favorite_border),
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ShowLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 2.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        CircularProgressIndicator()  // A circular loading indicator
    }
}