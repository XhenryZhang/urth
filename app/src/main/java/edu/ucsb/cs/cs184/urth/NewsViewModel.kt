package edu.ucsb.cs.cs184.urth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NewsViewModel : ViewModel() {
    private val _news = MutableLiveData<Array<NewsObject?>>().apply {
        value = Array(0) { null }
    }

    fun getNews(): Array<NewsObject?>? {
        return _news.value
    }

    fun setNews(newNews: Array<NewsObject?>) {
        _news.value = newNews
    }

    val news: LiveData<Array<NewsObject?>> = _news
}
