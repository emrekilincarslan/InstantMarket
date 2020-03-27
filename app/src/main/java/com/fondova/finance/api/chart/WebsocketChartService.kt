package com.fondova.finance.api.chart

import com.google.gson.Gson
import com.fondova.finance.AppExecutors
import com.fondova.finance.R
import com.fondova.finance.api.model.chart.ChartSnapRequest
import com.fondova.finance.api.model.chart.ChartWatchRequest
import com.fondova.finance.api.model.chart.ChartWatchResponse
import com.fondova.finance.api.model.unwatch.UnwatchRequest
import com.fondova.finance.api.socket.WebsocketService
import com.fondova.finance.persistance.AppStorageInterface
import com.fondova.finance.repo.ChartWatchRepository
import com.fondova.finance.util.TaskRunner
import com.fondova.finance.vo.ChartData
import java.util.*

class WebsocketChartService(val websocketService: WebsocketService,
                            val appExecutors: AppExecutors,
                            val chartWatchRepository: ChartWatchRepository,
                            val appStorage: AppStorageInterface,
                            val timeoutTaskRunner: TaskRunner): ChartService {

    var watchId: String = ""
    var snapId: String = ""

    override fun watchChart(symbol: String, interval: Int, isExpression: Boolean) {
        if (appStorage.getRefreshRateAsInt() == 0) {
            snapChart(symbol, interval, isExpression)
            return
        }
        timeoutTaskRunner.run { throwErrorMessage(R.string.network_timeout_error) }
        appExecutors.mainThread().execute { chartWatchRepository.clearData() }
        watchId = UUID.randomUUID().toString()
        val serverSymbol = if (isExpression) symbol else "'$symbol'"
        val request = ChartWatchRequest.create(watchId, serverSymbol, interval)
        val json = Gson().toJson(request)
        appExecutors.networkIO().execute { websocketService.sendMessage(json) }
    }

    override fun snapChart(symbol: String, interval: Int, isExpression: Boolean) {
        timeoutTaskRunner.run { throwErrorMessage(R.string.network_timeout_error) }
        appExecutors.mainThread().execute { chartWatchRepository.clearData() }
        snapId = UUID.randomUUID().toString()
        val serverSymbol = if (isExpression) symbol else "'$symbol'"
        val request = ChartSnapRequest.create(snapId, serverSymbol, interval)
        val json = Gson().toJson(request)
        appExecutors.networkIO().execute { websocketService.sendMessage(json) }
    }

    override fun unwatchChart() {
        appExecutors.mainThread().execute { chartWatchRepository.clearData() }
        val request = UnwatchRequest.create(watchId)
        watchId = ""
        val json = Gson().toJson(request)
        appExecutors.networkIO().execute { websocketService.sendMessage(json) }
    }

    override fun handleMessage(message: String): Boolean {
        if (!isWatchedByThisService(message)) {
            return false
        }

        timeoutTaskRunner.cancel()
        val response = Gson().fromJson(message, ChartWatchResponse::class.java)
        if (!isSuccess(response)) {
            throwErrorMessage(R.string.invalid_chart_request)
            return true
        }
        val isLatest = response.meta?.upToDate ?: false
        val data = response?.data?.filter { !it.isNullDataPoint() } ?: listOf()
        appExecutors.mainThread().execute {
            chartWatchRepository.addData(data, !isLatest)
        }

        return true
    }

    private fun isSuccess(response: ChartWatchResponse): Boolean {
        val code = response.meta?.status ?: return false
        return  code >= 200 && code < 300

    }

    private fun isWatchedByThisService(message: String): Boolean {
        return (!watchId.isEmpty() && message.contains(watchId)) || (!snapId.isEmpty() && message.contains(snapId))
    }

    private fun throwErrorMessage(stringId: Int) {
        appExecutors.mainThread().execute {
            chartWatchRepository.setError("Network Error", "$stringId")
        }
    }

}

fun ChartData.isNullDataPoint() :Boolean {
    return isNull || open?.number == null || close?.number == null || high?.number == null || low?.number == null
}