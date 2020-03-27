package com.fondova.finance.api.model.quote

import com.fondova.finance.api.model.base.WebsocketApiRequest

open class QuoteRequest(command: String, expression: String, fields: List<String>): WebsocketApiRequest(command) {

    var data: QuoteRequestData = QuoteRequestData(expression, fields)
}

class QuoteRequestData(expression: String, fields: List<String>) {

    var expression: String = expression
    var priceFormat: String = "text"
    var timeFormat: String = "text"
    var symbolFormat: String = "dict"
    var fields: List<String> = fields

}

class QuoteWatchRequest(expression: String, fields: List<String>): QuoteRequest("QuoteWatch", expression, fields)
class QuoteSnapRequest(expression: String, fields: List<String>): QuoteRequest("QuoteSnap", expression, fields)
