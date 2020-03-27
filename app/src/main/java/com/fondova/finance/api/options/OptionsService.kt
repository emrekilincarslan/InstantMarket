package com.fondova.finance.api.options

import android.arch.lifecycle.LiveData
import com.fondova.finance.api.Resource
import com.fondova.finance.api.model.optionsSeries.OptionsSeriesWatchResponse
import com.fondova.finance.api.model.optionsSnap.OptionsSnap
import com.fondova.finance.api.model.optionsSnap.OptionsSnapResponse
import com.fondova.finance.api.socket.WebsocketResponseHandler


//snap
interface OnOptionsSnapReceivedListener {
    fun onOptionsSnapReceived(response: Resource<OptionsSnapResponse>)
}

interface OptionsService: WebsocketResponseHandler {

    fun getOptionsLiveData(): LiveData<Resource<OptionsSeriesWatchResponse>>

    fun watchOptionsQuery(query: String)

    fun watchSymbolOptions(snap: OptionsSnap, limit: Int)

    fun getOptionsSnaps(id: String, listener: OnOptionsSnapReceivedListener?)
}