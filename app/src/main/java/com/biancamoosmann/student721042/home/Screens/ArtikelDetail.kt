package com.biancamoosmann.student721042.home.Screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.biancamoosmann.student721042.R
import com.biancamoosmann.student721042.home.ViewModel.HomeViewModel
import com.biancamoosmann.student721042.home.data.model.ArticleItem
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

fun formatDateToCustomFormat(dateString: String): String? {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
    val outputFormat = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.ENGLISH)
    val date = inputFormat.parse(dateString)
    return date?.let { outputFormat.format(it) }
}


@Composable
fun DetailScreen(article: ArticleItem?, navController: NavHostController, viewModel: HomeViewModel) {
    val context = LocalContext.current

    if (article != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
                .verticalScroll(rememberScrollState())
        ) {
            DetailScreenTopBar(navController) {
                // Get Back to the previous page
                navController.popBackStack()
            }
            AsyncImage(
                model = article.Image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(0.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween){
                Box(
                    modifier = Modifier.weight(1f)
                        .padding(16.dp)
                ){
                    val customFormattedDate = formatDateToCustomFormat(article.PublishDate)
                    if (customFormattedDate != null) {
                        Text(customFormattedDate)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    val annotatedString = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = Color.Blue,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            //Link get the name "Open in Browser" instead of put the whole URL there
                            append("Open in Browser")
                            addStringAnnotation(
                                tag = "URL",
                                annotation = article.Url,
                                start = 0,
                                end = "Open in Browser".length
                            )
                        }
                    }
                    val textToShow = annotatedString.toString()
                    Text(
                        text = textToShow,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(16.dp).clickable {

                            val uri = Uri.parse(article.Url)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        },
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    )

                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = article.Title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text = article.Summary,
                fontSize = 16.sp,
                modifier = Modifier.padding(16.dp)
            )

            CategoryButtons(article, navController)
            Actions(article, viewModel)
            if (article.Related.isNotEmpty()) {
                RelatedArticles(article.Related)
            }
        }
    } else {
        val errorMessage = context.resources.getString(R.string.error_article_detail)
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun RelatedArticles(relatedUrls: List<String>?) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(id=R.string.relatedArticles),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp)
        )
        // Proof it related array is not empty
        relatedUrls?.let {
            for (url in it) {
                val annotatedString = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(url)
                        addStringAnnotation(
                            tag = "URL",
                            annotation = url,
                            start = 0,
                            end = url.length
                        )
                    }
                }
                Text(
                    text = annotatedString,
                    modifier = Modifier.padding(16.dp).clickable {
                        val uri = Uri.parse(url)
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}


@Composable
fun DetailScreenTopBar(navController: NavHostController, onBackClicked: () -> Unit) {
    Box() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start= 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp)
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    //get to the previous page
                    onBackClicked()
                }
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
            // Title Topbar
            Text(
                text = stringResource(id=R.string.article_detail),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 16.dp) // Padding to the arrow
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}


@OptIn(DelicateCoroutinesApi::class)
@Composable
fun Actions(article: ArticleItem, viewModel: HomeViewModel) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
    val sharedPrefAuth = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val auth = sharedPrefAuth.getString("authToken", null)
    var isFavorite by remember { mutableStateOf(article.IsLiked) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Icon Share
        IconButton(
            onClick = {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Share this article: ${article.Title}\n${article.Url}")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share"))
            }
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        if (isLoggedIn && auth != null) {
            IconButton(
                onClick = {
                    GlobalScope.launch(Dispatchers.Main) {
                        viewModel.toggleFavoriteStatus(article) { success ->
                            if (success) {
                                //Change State from isFavorite
                                isFavorite = article.IsLiked

                                //Delete Article from the LikedArticle list
                                if (!isFavorite) {
                                    viewModel.removeLikedArticle(article)
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Fehler beim Liken des Artikels",
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
            //Toast.makeText(context, "You are not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}



