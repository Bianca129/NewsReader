package com.biancamoosmann.student721042.home.ViewModel

import android.content.Context
import android.net.ConnectivityManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.biancamoosmann.student721042.MyApp
import com.biancamoosmann.student721042.R
import com.biancamoosmann.student721042.home.data.model.ArticleItem
import com.biancamoosmann.student721042.home.data.model.ArticleResults
import com.biancamoosmann.student721042.home.data.SharedPreferencesManager
import com.biancamoosmann.student721042.home.network.ArticlesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit


@Suppress("DEPRECATION")
class HomeViewModel : ViewModel(){

    var listArticle by mutableStateOf<List<ArticleItem>>(emptyList())
    var listArticleMore by mutableStateOf<List<ArticleItem>>(emptyList())
    var listLikedArticle by mutableStateOf<List<ArticleItem>>(emptyList())

    private val dataUpdated = MutableStateFlow(false)

    init {
        listArticle = emptyList()
    }



    private val liveDataLikedArticles: MutableLiveData<List<ArticleItem>> = MutableLiveData()
    private var liveAuthToken = MutableLiveData<String>()

    val isLoading = MutableLiveData<Boolean>().apply { value = false }
    val isLoadingMoreArticles = MutableLiveData<Boolean>().apply { value = false }
    val _loadingState = mutableStateOf(true)

    //Lightmode/DarkMode
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    //Error handling
    val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    val _nextIdForMoreArticles = MutableLiveData<Int>()

    private val _loadingMoreState = MutableLiveData<Boolean>()





    //LoadingIndicator
    private fun showLoading() {
        isLoading.value = true
        isLoading.postValue(true)
    }

    private fun showLoadingMoreArticles() {
        isLoadingMoreArticles.value = true
    }


    fun hideLoading() {
        isLoading.value = false
        isLoading.postValue(false)
    }

    fun hideLoadingMoreArticles() {
        isLoadingMoreArticles.value = false
        isLoadingMoreArticles.postValue(false)
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }


    fun removeLikedArticle(article: ArticleItem) {
        liveDataLikedArticles.value = liveDataLikedArticles.value?.filter { it != article }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun fetchData(appContext: Context, shouldLoadLikedArticles: Boolean) {
        liveAuthToken.value = SharedPreferencesManager.getAuthToken()
        val client: OkHttpClient
        showLoading()
        try {
            if (!isNetworkAvailable(appContext)) {
                // no network connection, show error
                _errorMessage.value = appContext.getString(R.string.error_network_issue)
                _loadingState.value = false
                return
            }

            client = OkHttpClient.Builder().apply {
                connectTimeout(20, TimeUnit.SECONDS)
                readTimeout(20, TimeUnit.SECONDS)

                // if authtoken available and user want to load the liked articles
                if (shouldLoadLikedArticles && liveAuthToken.value != null) {
                    addInterceptor { chain ->
                        val newRequest = chain.request().newBuilder()
                            .addHeader("x-authtoken", liveAuthToken.value.toString())
                            .build()
                        chain.proceed(newRequest)
                    }
                }
            }.build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://inhollandbackend.azurewebsites.net/api/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(ArticlesApi::class.java)

            val allArticles = api.getArticles()

            allArticles.enqueue(object : Callback<ArticleResults> {
                override fun onResponse(
                    call: Call<ArticleResults>,
                    response: Response<ArticleResults>
                ) {
                    val articlesList = response.body()?.Results
                    if (response.code() == 200 && articlesList != null) {
                        listArticle = articlesList
                        _nextIdForMoreArticles.value = response.body()?.NextId
                    } else {
                        listArticle = emptyList()
                    }
                    hideLoading()
                }

                override fun onFailure(call: Call<ArticleResults>, t: Throwable) {
                    listArticle = emptyList()
                    when (t) {
                        is SocketTimeoutException -> {
                            _errorMessage.value = appContext.getString(R.string.error_timeout)
                        }

                        is HttpException -> {
                            val errorMessage = when (t.code()) {
                                401 -> appContext.getString(R.string.error_authentication_required)
                                403 -> appContext.getString(R.string.error_access_denied)
                                404 -> appContext.getString(R.string.error_data_not_found)
                                500 -> appContext.getString(R.string.error_internal_server)
                                else -> appContext.getString(R.string.error_unknown)
                            }
                            _errorMessage.value = errorMessage
                        }

                        else -> {
                            val errorMessage = appContext.getString(R.string.error_unknown_generic, t.message)
                            _errorMessage.value = errorMessage
                        }
                    }
                    _loadingState.value = false
                }
            })
        } catch (e: Exception) {
            val errorMessage = appContext.getString(R.string.error_unknown)
            _errorMessage.value = errorMessage
            _loadingState.value = false
        }
    }


    fun fetchData(
        appContext: Context,
        shouldLoadLikedArticles: Boolean,
        onComplete: (Result<List<ArticleItem>>?) -> Unit // Callback for success or fault
    ) {
        liveAuthToken.value = SharedPreferencesManager.getAuthToken()
        val client: OkHttpClient
        showLoading()
        try {
            if (!isNetworkAvailable(appContext)) {
                // no network connection, show error
                _errorMessage.value = appContext.getString(R.string.error_network_issue)
                _loadingState.value = false
                onComplete(Result.failure(Exception(appContext.getString(R.string.error_network_issue))))
                    return
            }

            client = OkHttpClient.Builder().apply {
                connectTimeout(20, TimeUnit.SECONDS)
                readTimeout(20, TimeUnit.SECONDS)

                // if authtoken available and user want to load the liked articles
                if (shouldLoadLikedArticles && liveAuthToken.value != null) {
                    addInterceptor { chain ->
                        val newRequest = chain.request().newBuilder()
                            .addHeader("x-authtoken", liveAuthToken.value.toString())
                            .build()
                        chain.proceed(newRequest)
                    }
                }
            }.build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://inhollandbackend.azurewebsites.net/api/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(ArticlesApi::class.java)

            val allArticles = api.getArticles()

            allArticles.enqueue(object : Callback<ArticleResults> {
                override fun onResponse(
                    call: Call<ArticleResults>,
                    response: Response<ArticleResults>
                ) {
                    val articlesList = response.body()?.Results
                    if (response.code() == 200 && articlesList != null) {
                        listArticle = articlesList
                        hideLoading()
                        onComplete(Result.success(listArticle))
                    } else {
                        listArticle = emptyList()
                        hideLoading()

                        val errorMessage = appContext.getString(R.string.error_articles)
                        onComplete(Result.failure(Exception(errorMessage)))
                    }
                }

                override fun onFailure(call: Call<ArticleResults>, t: Throwable) {
                    listArticle = emptyList()
                    when (t) {
                        is SocketTimeoutException -> {
                            _errorMessage.value = appContext.getString(R.string.error_timeout)
                        }

                        is HttpException -> {
                            val errorMessage = when (t.code()) {
                                401 -> appContext.getString(R.string.error_authentication_required)
                                403 -> appContext.getString(R.string.error_access_denied)
                                404 -> appContext.getString(R.string.error_data_not_found)
                                500 -> appContext.getString(R.string.error_internal_server)
                                else -> appContext.getString(R.string.error_unknown)
                            }
                            _errorMessage.value = errorMessage
                        }

                        else -> {
                            val errorMessage = appContext.getString(R.string.error_unknown_generic, t.message)
                            _errorMessage.value = errorMessage
                        }
                    }
                    _loadingState.value = false
                    onComplete(Result.failure(t))
                }
            })
        } catch (e: Exception) {
            val errorMessage = appContext.getString(R.string.error_unknown)
            _errorMessage.value = errorMessage
            _loadingState.value = false
            onComplete(Result.failure(e))
        }
        dataUpdated.value = true
    }

    private var fetchMoreArticlesCounter = 0
    var remainingArticlesToLoad by mutableIntStateOf(20)


    fun fetchMoreArticles(appContext: Context, nextId: Int, remainingAttempts: Int) {
        fetchMoreArticlesCounter++


        if (fetchMoreArticlesCounter == 1) {
            showLoadingMoreArticles()
        }else{
            hideLoading()
        }
        var nextIdNew = nextId

        liveAuthToken.value = SharedPreferencesManager.getAuthToken()
        val client: OkHttpClient
        runBlocking {
            try {
                if (!isNetworkAvailable(appContext)) {
                    // no network connection, show error
                    _errorMessage.value = appContext.getString(R.string.error_network_issue)
                    _loadingMoreState.value = false
                    hideLoadingMoreArticles()
                    return@runBlocking
                }

                client = OkHttpClient.Builder().apply {
                    connectTimeout(20, TimeUnit.SECONDS)
                    readTimeout(20, TimeUnit.SECONDS)

                    // add auth token if available
                    liveAuthToken.value?.let { authToken ->
                        addInterceptor { chain ->
                            val newRequest = chain.request().newBuilder()
                                .addHeader("x-authtoken", authToken)
                                .build()
                            chain.proceed(newRequest)
                        }
                    }
                }.build()

                val retrofit = Retrofit.Builder()
                    .baseUrl("https://inhollandbackend.azurewebsites.net/api/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(ArticlesApi::class.java)

                val articlesCall = api.getArticle(nextIdNew)

                articlesCall.enqueue(object : Callback<ArticleResults> {
                    override fun onResponse(
                        call: Call<ArticleResults>,
                        response: Response<ArticleResults>
                    ) {
                        val newArticlesList = response.body()?.Results
                        if (response.code() == 200 && newArticlesList != null) {
                            // Append new articles to the existing list
                            listArticleMore = (listArticleMore + newArticlesList).distinctBy { it.Id }
                            _nextIdForMoreArticles.value = response.body()?.NextId
                            nextIdNew = response.body()!!.NextId

                            if (remainingAttempts > 1) {
                                // Start the next API call with a recursive call
                                fetchMoreArticles(appContext, nextIdNew, remainingAttempts - 1)
                            } else {
                                listArticle = (listArticle + listArticleMore).distinctBy { it.Id }
                                remainingArticlesToLoad -= newArticlesList.size
                                _loadingMoreState.value = false
                                hideLoadingMoreArticles()
                                fetchMoreArticlesCounter = 0
                            }
                        }
                    }

                    override fun onFailure(call: Call<ArticleResults>, t: Throwable) {
                        listArticleMore = emptyList()
                        when (t) {
                            is SocketTimeoutException -> {
                                _errorMessage.value = appContext.getString(R.string.error_timeout)
                            }

                            is HttpException -> {
                                val errorMessage = when (t.code()) {
                                    401 -> appContext.getString(R.string.error_authentication_required)
                                    403 -> appContext.getString(R.string.error_access_denied)
                                    404 -> appContext.getString(R.string.error_data_not_found)
                                    500 -> appContext.getString(R.string.error_internal_server)
                                    else -> appContext.getString(R.string.error_unknown)
                                }
                                _errorMessage.value = errorMessage
                            }

                            else -> {
                                val errorMessage = appContext.getString(R.string.error_unknown_generic, t.message)
                                _errorMessage.value = errorMessage
                            }
                        }
                        hideLoadingMoreArticles()
                    }
                })


            } catch (e: Exception) {
                // Handle exceptions
                _loadingMoreState.value = false
                hideLoadingMoreArticles()
                fetchMoreArticlesCounter = 0
            }
        }

    }


    fun fetchLikedArticles(appContext: Context) {
        _loadingState.value = true
        liveAuthToken.value = SharedPreferencesManager.getAuthToken()

        val authToken = SharedPreferencesManager.getAuthToken()
        if (authToken.isNullOrEmpty()) {
            // if there is no token, do not execute the function
            return
        } else {

            try {
                showLoading()
                val client = OkHttpClient.Builder().addInterceptor { chain ->
                    val newRequest = chain.request().newBuilder()
                        .addHeader("x-authtoken", liveAuthToken.value.toString())
                        .build()
                    chain.proceed(newRequest)
                }
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS).build()
                MyApp.retrofit = Retrofit.Builder()
                    .baseUrl("https://inhollandbackend.azurewebsites.net/api/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val api = MyApp.retrofit.create(ArticlesApi::class.java)

                val allArticles = api.getLikedArticles()

                allArticles.enqueue(object : Callback<ArticleResults> {
                    override fun onResponse(
                        call: Call<ArticleResults>,
                        response: Response<ArticleResults>
                    ) {
                        val articlesLiked = response.body()?.Results
                        if (response.code() == 200 && articlesLiked != null) {
                            listLikedArticle = articlesLiked
                            _loadingState.value = false
                        } else {
                            listLikedArticle = emptyList()
                            _loadingState.value = false
                        }
                        hideLoading()
                    }

                    override fun onFailure(call: Call<ArticleResults>, t: Throwable) {
                        listLikedArticle = emptyList()
                        when (t) {
                            is SocketTimeoutException -> {
                                _errorMessage.value = appContext.getString(R.string.error_timeout)
                            }

                            is HttpException -> {

                                val errorMessage = when (t.code()) {
                                    401 -> appContext.getString(R.string.error_authentication_required)
                                    403 -> appContext.getString(R.string.error_access_denied)
                                    404 -> appContext.getString(R.string.error_data_not_found)
                                    500 -> appContext.getString(R.string.error_internal_server)
                                    else -> appContext.getString(R.string.error_unknown)
                                }
                                _errorMessage.value = errorMessage
                            }

                            else -> {
                                val errorMessage = appContext.getString(R.string.error_unknown_generic, t.message)
                                _errorMessage.value = errorMessage
                            }
                        }
                        _loadingState.value = false
                        hideLoading()
                    }

                })
            } catch (e: Exception) {
                val errorMessage = appContext.getString(R.string.error_unknown)
                _errorMessage.value = errorMessage
                _loadingState.value = false
                hideLoading()
            }

        }
    }

    fun toggleFavoriteStatus(article: ArticleItem, callback: (Boolean) -> Unit) {
        liveAuthToken.value = SharedPreferencesManager.getAuthToken()

        val client: OkHttpClient = if (liveAuthToken.value != null) {
            OkHttpClient.Builder().addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("x-authtoken", liveAuthToken.value.toString())
                    .build()
                chain.proceed(newRequest)
            }
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()
        } else {
            OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS).build()
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://inhollandbackend.azurewebsites.net/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ArticlesApi::class.java)

        val call: Call<Unit> = if (article.IsLiked) {
            api.dislikeArticle(article.Id)
        } else {
            api.likeArticle(article.Id)
        }

        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    article.IsLiked = !article.IsLiked
                    callback(true)
                } else {
                    callback(false)
                    handleApiCallFailure(response, callback)
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                handleApiCallFailure(t, callback)
            }
        })
    }


    private fun handleApiCallFailure(response: Response<Unit>?, callback: (Boolean) -> Unit) {
        if (response != null) {
            _errorMessage.value = MyApp.provideContext().getString(R.string.error_timeout)
        } else {
            _errorMessage.value = MyApp.provideContext().getString(R.string.error_unknown_generic, "Unknown error")
        }

        _loadingState.value = false
        callback(false)
    }

    private fun handleApiCallFailure(error: Throwable, callback: (Boolean) -> Unit) {
        when (error) {
            is SocketTimeoutException -> {
                _errorMessage.value = MyApp.provideContext().getString(R.string.error_timeout)
            }

            is HttpException -> {

                val errorMessage = when (error.code()) {
                    401 -> MyApp.provideContext().getString(R.string.error_authentication_required)
                    403 -> MyApp.provideContext().getString(R.string.error_access_denied)
                    404 -> MyApp.provideContext().getString(R.string.error_data_not_found)
                    500 -> MyApp.provideContext().getString(R.string.error_internal_server)
                    else -> MyApp.provideContext().getString(R.string.error_unknown)
                }
                _errorMessage.value = errorMessage
            }

            else -> {
                _errorMessage.value = MyApp.provideContext().getString(R.string.error_unknown_generic, error.message)
            }
        }

        _loadingState.value = false
        callback(false)
    }

    fun resetData() {
        listArticle = emptyList()
        listArticleMore = emptyList()
    }

}





