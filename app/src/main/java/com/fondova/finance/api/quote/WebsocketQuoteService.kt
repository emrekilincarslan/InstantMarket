package com.fondova.finance.api.quote

import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.fondova.finance.AppExecutors
import com.fondova.finance.api.model.quote.QuoteSnapRequest
import com.fondova.finance.api.model.quote.QuoteWatchRequest
import com.fondova.finance.api.model.quote.QuoteWatchResponse
import com.fondova.finance.api.model.unwatch.UnwatchRequest
import com.fondova.finance.api.socket.WebsocketService
import com.fondova.finance.api.socket.WebsocketServiceListener
import com.fondova.finance.persistance.AppStorageInterface
import com.fondova.finance.repo.QuoteWatchRepository
import java.io.IOException
import java.util.*

class WebsocketQuoteService(val websocketService: WebsocketService,
                            val appExecutors: AppExecutors,
                            val quoteWatchRepository: QuoteWatchRepository,
                            val quoteFieldsFactory: QuoteFieldsFactory,
                            val appStorage: AppStorageInterface): QuoteService, WebsocketServiceListener {

    private var watchMap: MutableMap<String, Pair<String, Int>> = mutableMapOf()
    private var snapMap: MutableMap<String, String> = mutableMapOf()
    private val lastUpdateLiveData = MutableLiveData<Date>()

    init {
        websocketService.addListener(this)
    }

    override fun getLastQuoteUpdateLiveData(): MutableLiveData<Date> {
        return this.lastUpdateLiveData
    }

    override fun watchQuote(symbol: String, isExpression: Boolean) {
        if (appStorage.getRefreshRateAsInt() == 0) {
            snapQuote(symbol, isExpression)
            return
        }
        val entry = watchMap.entries.firstOrNull { it.value.first == symbol }
        if (entry != null) {
            watchMap.set(entry.key, Pair(entry.value.first, entry.value.second + 1))
            return
        }
        var requestSymbol = symbol
        if (!isExpression) {
            requestSymbol = "'$requestSymbol'"
        }
        val request = QuoteWatchRequest(requestSymbol, quoteFieldsFactory.getQuoteRequestFields())
        watchMap.set(request.meta.requestId, Pair(symbol, 1))
        val message = Gson().toJson(request)

        sendMessage(message)
    }

    override fun unwatchQuote(symbol: String) {
        val entry = watchMap.entries.firstOrNull { it.value.first == symbol }
        if (entry == null) {
            return
        }

        var requestId = entry.key

        if (entry.value.second > 1) {
            watchMap.set(requestId, Pair(entry.value.first, entry.value.second - 1))
            return
        }

        var request = UnwatchRequest.create(requestId)
        watchMap.remove(requestId)
        sendMessage(Gson().toJson(request))
    }

    override fun snapQuote(symbol: String, isExpression: Boolean) {

        var requestSymbol = symbol
        if (!isExpression) {
            requestSymbol = "'$requestSymbol'"
        }

        val request = QuoteSnapRequest(requestSymbol, quoteFieldsFactory.getQuoteRequestFields())

        snapMap.set(request.meta.requestId, symbol)
        val message = Gson().toJson(request)

        sendMessage(message)
    }

    override fun unwatchAll() {
        val mapCopy = watchMap.toMap()
        watchMap = mutableMapOf()
        for (entry in mapCopy) {
            var request = UnwatchRequest.create(entry.key)
            sendMessage(Gson().toJson(request))
        }
    }

    override fun handleMessage(message: String): Boolean {

        if (message.contains("QuoteWatch")) {
            return handleQuoteResponse(message, { watchMap.get(it)?.first })
        }

        if (message.contains("QuoteSnap")) {
            return handleQuoteResponse(message, { snapMap.get(it) })
        }

        return false
    }

    private fun handleQuoteResponse(message: String, symbolGen: (String) -> String?): Boolean {
        val response = Gson().fromJson(message, QuoteWatchResponse::class.java)
        var requestId = response.meta?.requestId
        if (requestId == null) {
            return false
        }

        val symbol = symbolGen(requestId)

        if (symbol == null) {
            return false
        }

        appExecutors.mainThread().execute {
            lastUpdateLiveData.value = Date()
            quoteWatchRepository.updateQuote(symbol, response)
        }

        return true
    }

    private fun sendMessage(message: String) {
        appExecutors.networkIO().execute {
            websocketService.sendMessage(message)
        }
    }

    override fun onConnected(websocketService: WebsocketService) {
        // Don't care
    }

    override fun onDisconnected(websocketService: WebsocketService, code: Int, reason: String, closedByServer: Boolean) {
        reset()
    }

    override fun onSocketError(websocketService: WebsocketService, exception: IOException) {
        // Don't care
    }

    override fun onErrorMessage(message: String) {
        // Don't care
    }

    override fun reset() {
        watchMap = mutableMapOf()
        snapMap = mutableMapOf()
        quoteWatchRepository.clearCache()
    }
}