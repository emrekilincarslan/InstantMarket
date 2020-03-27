package com.fondova.finance.api.chart

import com.fondova.finance.api.socket.WebsocketResponseHandler


interface ChartService: WebsocketResponseHandler {
    fun watchChart(symbol: String, interval: Int, isExpression: Boolean)
    fun snapChart(symbol: String, interval: Int, isExpression: Boolean)
    fun unwatchChart()
}