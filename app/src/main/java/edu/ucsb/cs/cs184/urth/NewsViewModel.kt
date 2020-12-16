package edu.ucsb.cs.cs184.urth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NewsViewModel : ViewModel() {
    private val _textString = MutableLiveData<String>(). apply {
        value = "test -- unused"
    }

    private val _news = MutableLiveData<Array<NewsObject?>>(). apply {
        value = Array<NewsObject?>(0) {_ -> null}
    }

    fun getNews(): Array<NewsObject?>?{
        return _news.value
    }

    fun changeTextVal(newVal: String) {
        _textString.value = newVal
    }

    fun setNews(newNews: Array<NewsObject?>) {
        _news.value = newNews
    }

    val textString: LiveData<String> = _textString
    val news: LiveData<Array<NewsObject?>> = _news
}