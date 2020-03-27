package com.fondova.finance.api.socket

import android.util.Log
import com.neovisionaries.ws.client.*
import com.fondova.finance.FlavorConstants
import java.io.IOException
import javax.inject.Inject

class NeoWebSocketService @Inject constructor(): WebsocketService, WebSocketListener {

    private var websocket: WebSocket? = null
    private val TAG = "NeoWebSocketService"
    private var listeners: MutableList<WebsocketServiceListener> = mutableListOf()

    fun getWebsocket(): WebSocket? {
        if (websocket == null) {
            log("Creating new websocket")
            val factory = WebSocketFactory()

            try {
                websocket = factory.createSocket(FlavorConstants.DTN_BASE_API)
            } catch (exception: IOException) {
                exception.printStackTrace()
                return null
            }
            websocket?.addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)

            websocket?.addListener(this)
        }
        return websocket

    }

    override fun connect() {
        websocket = null
        log("Connecting...")
        getWebsocket()?.connectAsynchronously()
    }

    override fun disconnect() {
        if (websocket?.isOpen == true) {
            log("Disconnecting websocket...")
            websocket?.disconnect()
        }
        log("Destroying old websocket...")
        websocket = null
    }

    override fun isConnected(): Boolean {
        return websocket != null && websocket?.isOpen == true
    }

    override fun hasWebsocket(): Boolean {
        return websocket != null
    }
    override fun sendMessage(message: String) {
        log("sending message: $message")
        getWebsocket()?.sendText(message)
    }

    override fun addListener(listener: WebsocketServiceListener) {
        if (listeners.contains(listener)) {
            return
        }
        listeners.add(listener)
    }

    override fun removeListener(listener: WebsocketServiceListener) {
        if (!listeners.contains(listener)) {
            return
        }
        listeners.remove(listener)
    }

    @Throws(Exception::class)
    override fun onStateChanged(websocket: WebSocket?, newState: WebSocketState?) {

        log("onStateChanged: " + newState.toString())
    }

    @Throws(Exception::class)
    override fun onConnected(websocket: WebSocket?, headers: Map<String, List<String>>?) {

        log("connected")
        for (listener in listeners) {
            listener.onConnected(this)
        }
    }

    @Throws(Exception::class)
    override fun onConnectError(websocket: WebSocket?, cause: WebSocketException?) {

        log("onConnectError $cause")
        sendError(IOException("onConnectError", cause))
    }

    @Throws(Exception::class)
    override fun onDisconnected(websocket: WebSocket?,
                                serverCloseFrame: WebSocketFrame?, clientCloseFrame: WebSocketFrame?,
                                closedByServer: Boolean) {

        log("Server reason: " + serverCloseFrame?.closeReason)

        log("client reason: " + clientCloseFrame?.closeReason)
        this.websocket = null

        for (listener in listeners) {
            listener.onDisconnected(this, serverCloseFrame?.closeCode ?: 0, clientCloseFrame?.closeReason ?: "Unknown", closedByServer)
        }


    }

    @Throws(Exception::class)
    override fun onFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
        log("onFrame")
    }

    @Throws(Exception::class)
    override fun onContinuationFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
        log("onContinuationFrame")
    }

    @Throws(Exception::class)
    override fun onTextFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
        log("onTextFrame")
    }

    @Throws(Exception::class)
    override fun onBinaryFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
        log("onBinaryFrame")
    }

    @Throws(Exception::class)
    override fun onCloseFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
        log("onCloseFrame")
    }

    @Throws(Exception::class)
    override fun onPingFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
        log("onPingFrame")
    }

    @Throws(Exception::class)
    override fun onPongFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
        log("onPongFrame")
    }

    @Throws(Exception::class)
    override fun onTextMessage(websocket: WebSocket?, message: String?) {
        log("response: $message")
        for (listener in listeners) {
            listener.handleMessage(message ?: "")
        }
    }

    @Throws(Exception::class)
    override fun onBinaryMessage(websocket: WebSocket?, binary: ByteArray?) {
        log("onBinaryMessage")
    }

    @Throws(Exception::class)
    override fun onSendingFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
        log("onSendingFrame:")
    }

    @Throws(Exception::class)
    override fun onFrameSent(websocket: WebSocket?, frame: WebSocketFrame?) {
        log("onFrameSent:")
    }

    @Throws(Exception::class)
    override fun onFrameUnsent(websocket: WebSocket?, frame: WebSocketFrame?) {
        log("onFrameUnsent")
    }

    @Throws(Exception::class)
    override fun onThreadCreated(websocket: WebSocket?, threadType: ThreadType?,
                                 thread: Thread?) {
        log("onThreadCreated")
    }

    @Throws(Exception::class)
    override fun onThreadStarted(websocket: WebSocket?, threadType: ThreadType?,
                                 thread: Thread?) {
        log("onThreadStarted")
    }

    @Throws(Exception::class)
    override fun onThreadStopping(websocket: WebSocket?, threadType: ThreadType?,
                                  thread: Thread?) {
        log("onThreadStopping")
    }

    @Throws(Exception::class)
    override fun onError(websocket: WebSocket?, cause: WebSocketException?) {
        log("onError $cause")
        for (listener in listeners) {
            listener.onSocketError(this, IOException(cause?.message ?: "Unknown", cause))
        }
    }

    @Throws(Exception::class)
    override fun onFrameError(websocket: WebSocket?, cause: WebSocketException?,
                              frame: WebSocketFrame?) {
        log("onFrameError $cause")
    }

    @Throws(Exception::class)
    override fun onMessageError(websocket: WebSocket?, cause: WebSocketException?,
                                frames: List<WebSocketFrame>) {
        log("onMessageError $cause")
    }

    @Throws(Exception::class)
    override fun onMessageDecompressionError(websocket: WebSocket?,
                                             cause: WebSocketException?, compressed: ByteArray?) {
        log("onMessageDecompressionError $cause")
    }

    @Throws(Exception::class)
    override fun onTextMessageError(websocket: WebSocket?, cause: WebSocketException?,
                                    data: ByteArray?) {
        log("onTextMessageError $cause")
    }

    @Throws(Exception::class)
    override fun onSendError(websocket: WebSocket?, cause: WebSocketException?,
                             frame: WebSocketFrame?) {
        log("onSendError: $cause")
    }

    @Throws(Exception::class)
    override fun onUnexpectedError(websocket: WebSocket?, cause: WebSocketException?) {
        sendError(IOException("onUnexpectedError", cause))
        log("onUnexpectedError: $cause")

    }

    @Throws(Exception::class)
    override fun handleCallbackError(websocket: WebSocket?, cause: Throwable?) {
        log("handleCallbackError() throwable: " + cause.toString())
        cause?.printStackTrace()
        for (listener in listeners) {
            listener.onErrorMessage(cause?.message ?: "Unknown")
        }
    }

    @Throws(Exception::class)
    override fun onSendingHandshake(websocket: WebSocket?, requestLine: String?,
                                    headers: List<Array<String>>?) {
        log("onSendingHandshake")
    }

    private fun sendError(exception: IOException) {
        for (listener in listeners) {
            listener.onSocketError(this, exception)
        }
    }

    private fun log(message: String) {
        if(FlavorConstants.SHOULD_LOG_WEBSOCKET_COMMUNICATION) {
            Log.d(TAG, message)
        }

    }

}
