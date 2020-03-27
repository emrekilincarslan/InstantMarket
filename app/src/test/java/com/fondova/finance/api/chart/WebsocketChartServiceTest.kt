package com.fondova.finance.api.chart

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.*
import com.fondova.finance.AppExecutors
import com.fondova.finance.InlineExecutor
import com.fondova.finance.api.Status
import com.fondova.finance.api.model.base.WebsocketApiResponse
import com.fondova.finance.api.model.chart.ChartWatchResponse
import com.fondova.finance.api.model.chart.MetaChartWatch
import com.fondova.finance.api.model.quote.QuoteSnapRequest
import com.fondova.finance.api.model.quote.QuoteWatchRequest
import com.fondova.finance.api.socket.WebsocketService
import com.fondova.finance.persistance.AppStorageInterface
import com.fondova.finance.repo.ChartWatchRepository
import com.fondova.finance.util.TaskRunner
import com.fondova.finance.vo.ChartData
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class WebsocketChartServiceTest {

    @Rule
    fun rule(): TestRule { return InstantTaskExecutorRule() }

    lateinit var testObject: WebsocketChartService

    @Mock
    lateinit var mockWebsocketService: WebsocketService
    @Mock
    lateinit var mockAppExecutors: AppExecutors
    @Mock
    lateinit var mockAppStorage: AppStorageInterface

    val mockNetworkExecutor: InlineExecutor = InlineExecutor()
    val mockMainExecutor: InlineExecutor = InlineExecutor()
    val chartWatRepository = ChartWatchRepository()
    val taskRunner = ManualTaskRunner()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        testObject = WebsocketChartService(mockWebsocketService, mockAppExecutors, chartWatRepository, mockAppStorage, taskRunner)
        whenever(mockAppExecutors.networkIO()).thenReturn(mockNetworkExecutor)
        whenever(mockAppExecutors.mainThread()).thenReturn(mockMainExecutor)
        whenever(mockAppStorage.getRefreshRateAsInt()).thenReturn(5)
    }

    @Test
    fun watchChartSendsMessageForExpression() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.watchChart("AAPL", 60, true)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        assertEquals(1, mockNetworkExecutor.executeCount)
        assertEquals(true, argumentCaptor.firstValue.contains("ChartWatch"))
        assertEquals(true, argumentCaptor.firstValue.contains("AAPL"))
        assertEquals(true, argumentCaptor.firstValue.contains("60"))

    }

    @Test
    fun snapChartSendsMessageForExpression() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.snapChart("AAPL", 60, true)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        assertEquals(1, mockNetworkExecutor.executeCount)
        assertEquals(true, argumentCaptor.firstValue.contains("ChartSnap"))
        assertEquals(true, argumentCaptor.firstValue.contains("AAPL"))
        assertEquals(true, argumentCaptor.firstValue.contains("60"))
    }

    @Test
    fun watchChartSendsMessageForQuoteWithSymbolWrappedInQuotes() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.watchChart("AAPL", 60, false)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        assertEquals(1, mockNetworkExecutor.executeCount)
        assertEquals(true, argumentCaptor.firstValue.contains("ChartWatch"))
        assertEquals(true, argumentCaptor.firstValue.contains("\\u0027AAPL\\u0027"))
        assertEquals(true, argumentCaptor.firstValue.contains("60"))

    }

    @Test
    fun snapChartSendsMessageForQuoteWithSymbolWrappedInQuotes() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.snapChart("AAPL", 60, false)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        assertEquals(1, mockNetworkExecutor.executeCount)
        assertEquals(true, argumentCaptor.firstValue.contains("ChartSnap"))
        assertEquals(true, argumentCaptor.firstValue.contains("\\u0027AAPL\\u0027"))
        assertEquals(true, argumentCaptor.firstValue.contains("60"))
    }

    @Test
    fun sendLoadingMessageToLiveDataOnWatch() {

        testObject.watchChart("AAPL", 60, true)

        assertEquals(1, mockMainExecutor.executeCount)
        assertEquals(Status.LOADING, chartWatRepository.subscribe().value?.status)

    }

    @Test
    fun sendLoadingMeassageToLiveDataOnSnap() {
        testObject.watchChart("AAPL", 60, true)

        assertEquals(1, mockMainExecutor.executeCount)
        assertEquals(Status.LOADING, chartWatRepository.subscribe().value?.status)

    }

    @Test
    fun waitForLatestDataBeforeSendingToLiveData() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.watchChart("AAPL", 60, true)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        val equestId = Gson().fromJson(argumentCaptor.firstValue, QuoteWatchRequest::class.java).meta.requestId

        var response1 = ChartWatchResponse.empty()
        response1.meta = MetaChartWatch()
        response1.meta?.upToDate = false
        response1.meta?.requestId = equestId
        response1.meta?.command = "ChartWatch"
        response1.meta?.status = 200
        val dataPoint1 = createChartData(1.0, "2016-08-31T19:40:00.000")
        response1.data = listOf(dataPoint1)

        var response2 = ChartWatchResponse.empty()
        response2.meta = MetaChartWatch()
        response2.meta?.upToDate = true
        response2.meta?.requestId = equestId
        response2.meta?.command = "ChartWatch"
        response2.meta?.status = 200
        val dataPoint2 = createChartData(2.0, "2016-08-31T19:45:00.000")

        response2.data = listOf(dataPoint2)

        mockMainExecutor.executeCount = 0


        assertTrue(testObject.handleMessage(Gson().toJson(response1)))
        assertTrue(testObject.handleMessage(Gson().toJson(response2)))

        val data = chartWatRepository.subscribe().value?.data

        assertEquals(2, mockMainExecutor.executeCount)
        assertEquals(2, data?.size)

        assertEquals(1.toDouble(), data?.first()?.close?.number )
        assertEquals(2.toDouble(), data?.last()?.close?.number)

    }

    fun createChartData(value: Double, date: String): ChartData {
        val dataPoint2 = ChartData()
        dataPoint2.open = ChartData.DualFormatValue()
        dataPoint2.close = ChartData.DualFormatValue()
        dataPoint2.high = ChartData.DualFormatValue()
        dataPoint2.low = ChartData.DualFormatValue()
        dataPoint2.open.number = value
        dataPoint2.close.number = value
        dataPoint2.high.number = value
        dataPoint2.low.number = value
        dataPoint2.dateTime = date
        return dataPoint2
    }

    @Test
    fun sortResponses() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.watchChart("AAPL", 60, true)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        val equestId = Gson().fromJson(argumentCaptor.firstValue, QuoteWatchRequest::class.java).meta.requestId

        var response1 = ChartWatchResponse.empty()
        response1.meta = MetaChartWatch()
        response1.meta?.upToDate = true
        response1.meta?.requestId = equestId
        response1.meta?.command = "ChartWatch"
        response1.meta?.status = 200
        val dataPoint1 = createChartData(1.0, "2016-08-31T19:40:00.000")
        response1.data = listOf(dataPoint1)

        var response2 = ChartWatchResponse.empty()
        response2.meta = MetaChartWatch()
        response2.meta?.upToDate = true
        response2.meta?.requestId = equestId
        response2.meta?.command = "ChartWatch"
        response2.meta?.status = 200
        val dataPoint2 = createChartData(2.0, "2016-08-31T19:45:00.000")

        response2.data = listOf(dataPoint2)

        mockMainExecutor.executeCount = 0


        assertTrue(testObject.handleMessage(Gson().toJson(response2)))
        assertTrue(testObject.handleMessage(Gson().toJson(response1)))

        val data = chartWatRepository.subscribe().value?.data

        assertEquals(2, mockMainExecutor.executeCount)
        assertEquals(2, data?.size)

        assertEquals(1.toDouble(), data?.first()?.close?.number )
        assertEquals(2.toDouble(), data?.last()?.close?.number)

    }

    @Test
    fun ignoreNonChartWatchOrChartSnapResponses() {
        var response = WebsocketApiResponse()
        response.meta?.command = "Login"

        assertFalse(testObject.handleMessage(Gson().toJson(response)))
    }

    @Test
    fun updateNewDataPointsForExistingDateTime() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.watchChart("AAPL", 60, true)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        val equestId = Gson().fromJson(argumentCaptor.firstValue, QuoteWatchRequest::class.java).meta.requestId

        var response1 = ChartWatchResponse.empty()
        response1.meta = MetaChartWatch()
        response1.meta?.upToDate = true
        response1.meta?.requestId = equestId
        response1.meta?.command = "ChartWatch"
        response1.meta?.status = 200

        val dataPoint1 = createChartData(1.0, "2016-08-31T19:40:00.000")
        dataPoint1.volume = ChartData.DualFormatValue()
        dataPoint1.volume.number = 10.toDouble()
        dataPoint1.volume.text = "10"
        dataPoint1.dateTime = "2016-08-31T19:40:00.000"
        response1.data = listOf(dataPoint1)

        var response2 = ChartWatchResponse.empty()
        response2.meta = MetaChartWatch()
        response2.meta?.upToDate = true
        response2.meta?.requestId = equestId
        response2.meta?.command = "ChartWatch"
        response2.meta?.status = 200
        val dataPoint2 = createChartData(2.0, "2016-08-31T19:40:00.000")

        response2.data = listOf(dataPoint2)

        mockMainExecutor.executeCount = 0


        assertTrue(testObject.handleMessage(Gson().toJson(response1)))
        assertTrue(testObject.handleMessage(Gson().toJson(response2)))

        val data = chartWatRepository.subscribe().value?.data

        assertEquals(2, mockMainExecutor.executeCount)
        assertEquals(1, data?.size)

        assertEquals(2.toDouble(), data?.first()?.close?.number )
        assertEquals(10.toDouble(), data?.first()?.volume?.number )
        assertEquals("10", data?.first()?.volume?.text )
    }

    @Test
    fun clearCacheWhenFiringNewWatch() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.watchChart("AAPL", 60, true)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        val equestId = Gson().fromJson(argumentCaptor.firstValue, QuoteWatchRequest::class.java).meta.requestId

        var response1 = ChartWatchResponse.empty()
        response1.meta = MetaChartWatch()
        response1.meta?.upToDate = true
        response1.meta?.requestId = equestId
        response1.meta?.command = "ChartWatch"
        response1.meta?.status = 200
        val dataPoint1 = createChartData(1.0, "2016-08-31T19:40:00.000")

        val dataPoint2 = createChartData(2.0, "2016-08-31T19:45:00.000")

        response1.data = listOf(dataPoint1)

        mockMainExecutor.executeCount = 0

        assertTrue(testObject.handleMessage(Gson().toJson(response1)))

        val data = chartWatRepository.subscribe().value?.data

        assertEquals(1, mockMainExecutor.executeCount)
        assertEquals(1, data?.size)

        assertEquals(1.toDouble(), data?.first()?.close?.number )

        reset(mockWebsocketService)

        testObject.watchChart("AAPL", 60, true)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        val equestId2 = Gson().fromJson(argumentCaptor.lastValue, QuoteWatchRequest::class.java).meta.requestId
        response1.meta?.requestId = equestId2

        mockMainExecutor.executeCount = 0

        response1.data = listOf(dataPoint2)

        assertTrue(testObject.handleMessage(Gson().toJson(response1)))

        val data2 = chartWatRepository.subscribe().value?.data

        assertEquals(1, mockMainExecutor.executeCount)
        assertEquals(1, data2?.size)

        assertEquals(2.toDouble(), data2?.first()?.close?.number )

    }

    @Test
    fun clearCacheWhenFiringNewSnap() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.snapChart("AAPL", 60, true)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        val requestId = Gson().fromJson(argumentCaptor.firstValue, QuoteSnapRequest::class.java).meta.requestId

        var response1 = ChartWatchResponse.empty()
        response1.meta = MetaChartWatch()
        response1.meta?.upToDate = true
        response1.meta?.requestId = requestId
        response1.meta?.command = "ChartWatch"
        response1.meta?.status = 200
        val dataPoint1 = createChartData(1.0, "2016-08-31T19:40:00.000")

        val dataPoint2 = createChartData(2.0, "2016-08-31T19:45:00.000")

        response1.data = listOf(dataPoint1)

        mockMainExecutor.executeCount = 0

        assertTrue(testObject.handleMessage(Gson().toJson(response1)))

        val data = chartWatRepository.subscribe().value?.data

        assertEquals(1, mockMainExecutor.executeCount)
        assertEquals(1, data?.size)

        assertEquals(1.toDouble(), data?.first()?.close?.number )

        reset(mockWebsocketService)

        testObject.snapChart("AAPL", 60, true)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        val requestId2 = Gson().fromJson(argumentCaptor.lastValue, QuoteSnapRequest::class.java).meta.requestId
        response1.meta?.requestId = requestId2

        mockMainExecutor.executeCount = 0

        response1.data = listOf(dataPoint2)

        assertTrue(testObject.handleMessage(Gson().toJson(response1)))

        val data2 = chartWatRepository.subscribe().value?.data

        assertEquals(1, mockMainExecutor.executeCount)
        assertEquals(1, data2?.size)

        assertEquals(2.toDouble(), data2?.first()?.close?.number )

    }

    @Test
    fun watchTimeoutSendsErrorToLiveData() {
        testObject.watchChart("AAPL", 30, true)

        val task = taskRunner.task ?: {}
        task()

        assertEquals(Status.ERROR, chartWatRepository.subscribe().value?.status)
    }
    @Test
    fun snapTimeoutSendsErrorToLiveData() {
        testObject.snapChart("AAPL", 30, true)

        val task = taskRunner.task ?: {}
        task()

        assertEquals(Status.ERROR, chartWatRepository.subscribe().value?.status)
    }
}

class ManualTaskRunner: TaskRunner {

    var cancelCount = 0
    var task: (() -> Unit)? = null

    override fun run(task: () -> Unit) {
        this.task = task
    }

    override fun cancel() {
        cancelCount += 1
    }

}