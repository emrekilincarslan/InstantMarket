package com.fondova.finance.api.news

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.fondova.finance.AppExecutors
import com.fondova.finance.api.Resource
import com.fondova.finance.api.model.news.*
import com.fondova.finance.api.socket.WebsocketService
import java.util.*

class WebsocketNewsService(val websocketService: WebsocketService,
                           val appExecutors: AppExecutors): NewsService {

    private val liveData = MutableLiveData<Resource<NewsWatchResponse>>()
    private var watchId = ""
    private var snapId = ""
    private var newsArticleListener: OnNewsArticleReceivedListener? = null
    private var limit: Int = 3

    override fun getNewsLiveData(): LiveData<Resource<NewsWatchResponse>> {
        return liveData
    }

    override fun watchSymbolNews(symbol: String, limit: Int) {
        this.limit = limit
        appExecutors.mainThread().execute {
            liveData.value = Resource.loading()
        }
        watchId = UUID.randomUUID().toString()
        val request = NewsWatchRequest.createForCharts(watchId, symbol, limit)
        val json = Gson().toJson(request)
        appExecutors.networkIO().execute {
            websocketService.sendMessage(json)
        }
    }

    override fun watchNewsQuery(query: String) {
        watchId = UUID.randomUUID().toString()
        val request = NewsWatchRequest.create(watchId, query)
        val json = Gson().toJson(request)
        appExecutors.networkIO().execute {
            websocketService.sendMessage(json)
        }
    }

    override fun getNewsArticle(id: String, listener: OnNewsArticleReceivedListener?) {
        newsArticleListener = listener
        snapId = UUID.randomUUID().toString()

        val request = NewsPageSnapRequest.create(snapId, id)
        val json = Gson().toJson(request)
        appExecutors.networkIO().execute {
            websocketService.sendMessage(json)
        }

    }

    override fun handleMessage(message: String): Boolean {
        if (!isWatchedByThisService(message)) {
            return false
        }
        if (!watchId.isEmpty() && message.contains(watchId)) {
            procesWatchResponse(message)
        } else if (!snapId.isEmpty() && message.contains(snapId)) {
            processSnapResponse(message)
        }

        return true
    }

    private fun processSnapResponse(message: String) {
        val response = Gson().fromJson(message, NewsPageSnapResponse::class.java)
        appExecutors.mainThread().execute {
            newsArticleListener?.onNewsArticleReceived(Resource.success(response))
        }
    }

    private fun procesWatchResponse(message: String) {
        val response = Gson().fromJson(message, NewsWatchResponse::class.java)
        val oldResponse = liveData.value?.data ?: NewsWatchResponse()
        val mergedResponse = removeDuplicatesInOldResponse(oldResponse, response)
        if (mergedResponse == null) {
            return
        }
        appExecutors.mainThread().execute {
            liveData.value = Resource.success(mergedResponse)
        }
    }

    private fun isWatchedByThisService(message: String): Boolean {
        return (!watchId.isEmpty() && message.contains(watchId)) || (!snapId.isEmpty() && message.contains(snapId))
    }

    private fun removeDuplicatesInOldResponse(oldResponse: NewsWatchResponse?, newResponse: NewsWatchResponse?): NewsWatchResponse? {
        var oldList = oldResponse?.data ?: emptyList()
        val newList = newResponse?.data ?: emptyList()
        var output: MutableList<CategoryArticle> = mutableListOf()
        output.addAll(newList)
        for (response in newList) {
            oldList = oldList.filter { it.storyId != response.storyId }
        }
        output.addAll(oldList)
        oldResponse?.data = output
        return oldResponse
    }

}