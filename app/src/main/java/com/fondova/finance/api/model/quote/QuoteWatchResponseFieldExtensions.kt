package com.fondova.finance.api.model.quote

import kotlin.math.roundToInt

fun QuoteWatchResponse.stringForKey(key: String, checkErrors: Boolean = true): String? {
    if (errorValue() != null && checkErrors) {
        return errorValue()
    }
    val value = data?.first()?.get(key)
    return value as? String
}

fun QuoteWatchResponse.setValueForKey(key: String, value: Any?) {
    if (value == null) {
        data?.first()?.remove(key)
        return
    }
    data?.first()?.set(key, value)
}

var QuoteWatchResponse.actualSymbol: String?
    get() = stringForKey("ActualSymbol")
    set(value) { setValueForKey("ActualSymbol", value) }

var QuoteWatchResponse.quoteDelay: Int?
    get() = (data?.first()?.get("QuoteDelay") as? Double)?.roundToInt()
    set(value) { setValueForKey("QuoteDelay", value?.toDouble()) }

var QuoteWatchResponse.last: String?
    get() = stringForKey("Last")
    set(value) { setValueForKey("Last", value) }

var QuoteWatchResponse.change: String?
    get() = stringForKey("Change")
    set(value) { setValueForKey("Change", value) }

var QuoteWatchResponse.changePercentage: String?
    get() = stringForKey("PctChange")
    set(value) { setValueForKey("PctChange", value) }

var QuoteWatchResponse.high: String?
    get() = stringForKey("High")
    set(value) { setValueForKey("High", value) }

var QuoteWatchResponse.low: String?
    get() = stringForKey("Low")
    set(value) { setValueForKey("Low", value) }

var QuoteWatchResponse.symbolDescription: String?
    get() {
        if (errorTitle() != null) {
            return "${stringForKey("Expression", false) ?: "null"} - ${errorTitle()}"
        }
        return stringForKey("IssueDescription")
    }
    set(value) { setValueForKey("IssueDescription", value) }

var QuoteWatchResponse.open: String?
    get() = stringForKey("Open")
    set(value) { setValueForKey("Open", value) }

var QuoteWatchResponse.bid: String?
    get() = stringForKey("Bid")
    set(value) { setValueForKey("Bid", value) }

var QuoteWatchResponse.ask: String?
    get() = stringForKey("Ask")
    set(value) { setValueForKey("Ask", value) }

var QuoteWatchResponse.volume: Int?
    get() =  (data?.first()?.get("CumVolume") as? Double)?.roundToInt()
    set(value) { setValueForKey("CumVolume", value?.toDouble()) }

var QuoteWatchResponse.settlePrice: String?
    get() = stringForKey("SettlementPrice")
    set(value) { setValueForKey("SettlementPrice", value) }

var QuoteWatchResponse.settleDate: String?
    get() = stringForKey("Settledate")
    set(value) { setValueForKey("Settledate", value) }

fun QuoteWatchResponse.errorOrEmpty(): String {
    return errorValue() ?: "--"
}

fun QuoteWatchResponse.errorValue(): String? {
    if (errors?.firstOrNull { it.code == "Not Permissioned" } != null) {
        return "np"
    }
    val status = meta?.status ?: 500
    if (status >= 300 || status < 200) {
        return "invalid"
    }
    return null
}

fun QuoteWatchResponse.errorTitle(): String? {
    if (errors?.firstOrNull { it.code == "Not Permissioned" } != null) {
        return "Not Permissioned"
    }
    val status = meta?.status ?: 500
    if (status >= 300 || status < 200) {
        return "invalid"
    }
    return null
}
