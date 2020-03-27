package com.fondova.finance.api.options

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.fondova.finance.AppExecutors
import com.fondova.finance.api.Resource
import com.fondova.finance.api.model.news.*
import com.fondova.finance.api.model.optionsSeries.OptionSeriesWatchItem
import com.fondova.finance.api.model.optionsSeries.OptionSeriesWatchRequest
import com.fondova.finance.api.model.optionsSeries.OptionsSeriesWatchResponse
import com.fondova.finance.api.model.optionsSnap.OptionsSnap
import com.fondova.finance.api.model.optionsSnap.OptionsSnapResponse
import com.fondova.finance.api.socket.WebsocketService
import java.util.*
//service
class WebsocketOptionsService(val websocketService: WebsocketService,
                              val appExecutors: AppExecutors): OptionsService {

    private val liveData = MutableLiveData<Resource<OptionsSeriesWatchResponse>>()
    private var watchId = ""
    private var snapId = ""
    private var limit: Int = 1000
    private var optionsSnapListener: OnOptionsSnapReceivedListener? = null

    override fun getOptionsLiveData(): LiveData<Resource<OptionsSeriesWatchResponse>> {
        return liveData
    }

    override fun watchSymbolOptions(snap: OptionsSnap, limit: Int) {
        this.limit = limit
        appExecutors.mainThread().execute {
            liveData.value = Resource.loading()
        }
        watchId = UUID.randomUUID().toString()
        val request = OptionSeriesWatchRequest.create(watchId, snap);
        val json = Gson().toJson(request)
        appExecutors.networkIO().execute {
            websocketService.sendMessage(json)
        }
    }

    override fun watchOptionsQuery(query: String) {
        watchId = UUID.randomUUID().toString()
        val request = NewsWatchRequest.create(watchId, query)
        val json = Gson().toJson(request)
        appExecutors.networkIO().execute {
            websocketService.sendMessage(json)
        }
    }

    override fun getOptionsSnaps(id: String, listener: OnOptionsSnapReceivedListener?) {
        optionsSnapListener = listener
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
        val response = Gson().fromJson(message, OptionsSnapResponse::class.java)
        appExecutors.mainThread().execute {
            optionsSnapListener?.onOptionsSnapReceived(Resource.success(response))
        }
    }

    private fun procesWatchResponse(message: String) {
        val response = Gson().fromJson(message, OptionsSeriesWatchResponse::class.java)
        val oldResponse = liveData.value?.data ?: OptionsSeriesWatchResponse()
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

    private fun removeDuplicatesInOldResponse(oldResponse: OptionsSeriesWatchResponse?, newResponse: OptionsSeriesWatchResponse?): OptionsSeriesWatchResponse? {
        var oldList = oldResponse?.data ?: emptyList()
        val newList = newResponse?.data ?: emptyList()
        val output: MutableList<OptionSeriesWatchItem> = mutableListOf()
        output.addAll(newList)
        for (response in newList) {
            oldList = oldList.filter { it.underlying.symbol != response.underlying.symbol }
        }
        output.addAll(oldList)
        oldResponse?.data = output
        return oldResponse
    }

}