package com.fondova.finance.api.model.quote

fun QuoteWatchResponse.update(newData: QuoteWatchResponse): QuoteWatchResponse {
    val newResponse = QuoteWatchResponse()
    newResponse.meta = this.meta
    newResponse.errors = this.errors
    newResponse.data = mutableListOf(this.data?.first()?.toMutableMap() ?: mutableMapOf())
    for (key in newData.data?.first()?.keys ?: emptySet<String>()) {
        val value = newData.data?.first()?.get(key)
        if (value != null) {
            newResponse.data?.first()?.set(key, value)
        }
    }
    return newResponse
}

fun QuoteWatchResponse.Companion.notPermissioned(): QuoteWatchResponse {
    return QuoteWatchResponse()
}

fun QuoteWatchResponse.Companion.notFound(): QuoteWatchResponse {
    return QuoteWatchResponse()
}