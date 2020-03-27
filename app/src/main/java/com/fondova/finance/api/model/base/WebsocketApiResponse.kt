package com.fondova.finance.api.model.base

open class WebsocketApiResponse {
    var meta: WebsocketApiResponseMeta? = WebsocketApiResponseMeta()
}

open class WebsocketApiResponseMeta {

    var command: String? = null
    var requestId: String? = null
    var status: Int? = null

}
