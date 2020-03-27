package com.fondova.finance.api.news

import android.arch.lifecycle.LiveData
import com.fondova.finance.api.Resource
import com.fondova.finance.api.model.news.NewsPageSnapResponse
import com.fondova.finance.api.model.news.NewsWatchResponse
import com.fondova.finance.api.socket.WebsocketResponseHandler

interface OnNewsArticleReceivedListener {
    fun onNewsArticleReceived(response: Resource<NewsPageSnapResponse>)
}

interface NewsService: WebsocketResponseHandler {

    fun getNewsLiveData(): LiveData<Resource<NewsWatchResponse>>

    fun watchNewsQuery(query: String)

    fun watchSymbolNews(symbol: String, limit: Int)

    fun getNewsArticle(id: String, listener: OnNewsArticleReceivedListener?)
}