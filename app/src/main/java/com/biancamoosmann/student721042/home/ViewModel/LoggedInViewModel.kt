package com.biancamoosmann.student721042.home.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.biancamoosmann.student721042.home.data.model.ArticleItem


class LoggedInViewModel : ViewModel() {
    private val _isLoggedIn = MutableLiveData<Boolean>()


    private val _likedArticles = MutableLiveData<List<ArticleItem>>()


    init {
        _isLoggedIn.value = false
        _likedArticles.value = emptyList()
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        _isLoggedIn.value = isLoggedIn
    }

}


