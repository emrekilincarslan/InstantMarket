package com.fondova.finance.api.restful

import com.google.gson.Gson
import com.fondova.finance.api.LogHttpClient
import com.fondova.finance.persistance.fromJson
import com.fondova.finance.workspace.financex.StockCloudQuoteList
import com.fondova.finance.workspace.financex.StockCloudQuoteListEnvelope
import okhttp3.*
import java.io.IOException

class StockCloudQuoteListRequest(var client: OkHttpClient = LogHttpClient().withoutGzip()) {

    var username: String = ""
    var password: String = ""
    var quoteList: StockCloudQuoteList? = null
    var successCallback: (response: StockCloudQuoteListEnvelope?) -> Unit = {}
    var errorCallback: (error: NetworkRequestError?) -> Unit = {}


    fun fetch() {
        val builder = Request.Builder()

        builder.url("https://ws1.stock.com/cloud-storage/files/FinanceX/Shared/Favorites.QuoteList.json")
        if (quoteList == null) {
            builder.get()
        } else {
            val envelope = StockCloudQuoteListEnvelope()
            envelope.data = quoteList
            val json = Gson().toJson(envelope)
            val mediaType = MediaType.parse("application/json; charset=utf-8")
            val body = RequestBody.create(mediaType, json)
            builder.put(body)
        }

        builder.addHeader("Accept", "application/vnd.stock.resource.v1+json")
        builder.addHeader("Authorization", createAuthHeader())
        builder.addHeader("User-Agent", createUserAgentHeader())
        val request = builder.build()
        val call = client.newCall(request)

        try {
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    errorCallback(NetworkRequestError(0, e.localizedMessage))
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code() == 404) {
                        successCallback(null)
                        return
                    }

                    if (response.code() < 200 || response.code() >= 300) {
                        errorCallback(NetworkRequestError(response.code(), response.body()?.string()
                                ?: "null"))
                        return
                    }
                    val json = response.body()?.string()
                    if (json == null) {
                        successCallback(null)
                        return
                    }
                    val quoteListEnvelope = Gson().fromJson<StockCloudQuoteListEnvelope>(json)
                    if (quoteListEnvelope == null) {
                        successCallback(null)
                        return
                    }

                    successCallback(quoteListEnvelope)
                }

            })
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun createUserAgentHeader(): String {
        return "FinanceXAndroid:1.2.0"
    }

    private fun createAuthHeader(): String {
        return Credentials.basic(username, password)
    }


}