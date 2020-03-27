package com.fondova.finance.api.model.quote

import com.fondova.finance.api.model.base.WebsocketApiResponseMeta

class QuoteWatchResponse {
    companion object

    var data: MutableList<MutableMap<String, Any>>? = mutableListOf(mutableMapOf())
    var meta: QuoteWatchResponseMeta? = QuoteWatchResponseMeta()
    var errors: List<QuoteWatchError>? = emptyList()

}

class QuoteWatchResponseMeta: WebsocketApiResponseMeta() {

    var symbols: List<QuoteWatchResponseSymbol>? = emptyList()

}

class QuoteWatchError {

    var code: String? = null
    var detail: String? = null

}

class QuoteWatchResponseSymbol {

    var symbol: String? = null
    var market: String? = null

}
