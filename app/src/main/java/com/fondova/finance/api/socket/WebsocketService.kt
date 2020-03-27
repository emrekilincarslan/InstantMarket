package com.fondova.finance.api.socket

import java.io.IOException

interface WebsocketResponseHandler {
    fun handleMessage(message: String): Boolean
}

interface WebsocketServiceListener: WebsocketResponseHandler {
    fun onConnected(websocketService: WebsocketService)
    fun onDisconnected(websocketService: WebsocketService, code: Int, reason: String, closedByServer: Boolean)
    fun onSocketError(websocketService: WebsocketService, exception: IOException)
    fun onErrorMessage(message: String)
}

interface WebsocketService {
    fun sendMessage(message: String)
    fun connect()
    fun disconnect()
    fun isConnected(): Boolean
    fun hasWebsocket(): Boolean
    fun addListener(listener: WebsocketServiceListener)
    fun removeListener(listener: WebsocketServiceListener)
}

