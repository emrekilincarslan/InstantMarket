package com.fondova.finance.api.model.base

import java.util.*


open class WebsocketApiRequest(command: String) {

    val meta: WebsocketApiRequestMeta = WebsocketApiRequestMeta(command)

}

class WebsocketApiRequestMeta(command: String) {

    val requestId: String = UUID.randomUUID().toString()
    val command: String = command

}

