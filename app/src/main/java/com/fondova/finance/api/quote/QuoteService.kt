package com.fondova.finance.api.quote

import android.arch.lifecycle.MutableLiveData
import com.fondova.finance.api.socket.WebsocketResponseHandler
import java.util.*


interface QuoteService: WebsocketResponseHandler {

    fun watchQuote(symbol: String, isExpression: Boolean)
    fun unwatchQuote(symbol: String)
    fun snapQuote(symbol: String, isExpression: Boolean)
    fun unwatchAll()
    fun getLastQuoteUpdateLiveData(): MutableLiveData<Date>
    fun reset()

}