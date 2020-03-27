package com.fondova.finance.api.quote

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.*
import com.fondova.finance.AppExecutors
import com.fondova.finance.InlineExecutor
import com.fondova.finance.api.model.quote.QuoteWatchRequest
import com.fondova.finance.api.model.quote.QuoteWatchResponse
import com.fondova.finance.api.socket.WebsocketService
import com.fondova.finance.persistance.AppStorageInterface
import com.fondova.finance.repo.QuoteWatchRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

class WebsocketQuoteServiceTest {

    @Rule
    fun rule(): TestRule { return InstantTaskExecutorRule() }

    @Mock
    lateinit var mockAppExecutors: AppExecutors
    @Mock
    lateinit var mockWebsocketService: WebsocketService
    @Mock
    lateinit var mockQuoteFieldsFactory: QuoteFieldsFactory

    lateinit var quoteWatchRepository: QuoteWatchRepository

    @Mock
    lateinit var mockAppStorage: AppStorageInterface

    val mockNetworkExecutor: InlineExecutor = InlineExecutor()
    val mockMainExecutor: InlineExecutor = InlineExecutor()

    lateinit var testObject: WebsocketQuoteService


    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        quoteWatchRepository = QuoteWatchRepository()
        testObject = WebsocketQuoteService(mockWebsocketService, mockAppExecutors, quoteWatchRepository, mockQuoteFieldsFactory, mockAppStorage)
        whenever(mockAppExecutors.networkIO()).thenReturn(mockNetworkExecutor)
        whenever(mockAppExecutors.mainThread()).thenReturn(mockMainExecutor)
        whenever(mockAppStorage.getRefreshRateAsInt()).thenReturn(5)
    }

    @Test
    fun quoteResponseUpdatesLastUptateTimeLiveData() {
        val argumentCaptor = argumentCaptor<String>()

        val startDate = Date()
        testObject.getLastQuoteUpdateLiveData().value = startDate
        testObject.watchQuote("AAPL", true)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        val aaplRequestId = Gson().fromJson(argumentCaptor.firstValue, QuoteWatchRequest::class.java).meta.requestId

        var aaplResponse = QuoteWatchResponse()
        aaplResponse.meta?.requestId = aaplRequestId
        aaplResponse.meta?.command = "QuoteWatch"
        aaplResponse.data = mutableListOf(mutableMapOf(Pair("Bomb", "Bay" as Any)))

        assertTrue(testObject.handleMessage(Gson().toJson(aaplResponse)))

        assertNotNull(testObject.getLastQuoteUpdateLiveData().value)
        assertNotEquals(startDate, testObject.getLastQuoteUpdateLiveData().value)
        assertEquals(true, testObject.getLastQuoteUpdateLiveData().value?.after(startDate))

    }
    @Test
    fun watchExpressionSendsWatchMessageToWebsocket() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.watchQuote("AAPL", true)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        assertEquals(1, mockNetworkExecutor.executeCount)
        assertEquals(true, argumentCaptor.firstValue.contains("QuoteWatch"))
        assertEquals(true, argumentCaptor.firstValue.contains("AAPL"))


    }

    @Test
    fun watchQuoteSendsWatchMessageToWebsocketWrappedInSingleQuotes() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.watchQuote("AAPL", false)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        assertEquals(1, mockNetworkExecutor.executeCount)
        assertEquals(true, argumentCaptor.firstValue.contains("QuoteWatch"))
        assertEquals(true, argumentCaptor.firstValue.contains("\\u0027AAPL\\u0027"))


    }

    @Test
    fun quoteWatchResponsesAreSentToRepo() {

        val argumentCaptor = argumentCaptor<String>()

        testObject.watchQuote("AAPL", true)
        testObject.watchQuote("GOOG", true)

        verify(mockWebsocketService, times(2)).sendMessage(argumentCaptor.capture())

        val aaplRequestId = Gson().fromJson(argumentCaptor.firstValue, QuoteWatchRequest::class.java).meta.requestId
        val googRequestId = Gson().fromJson(argumentCaptor.lastValue, QuoteWatchRequest::class.java).meta.requestId

        var aaplResponse = QuoteWatchResponse()
        aaplResponse.meta?.requestId = aaplRequestId
        aaplResponse.meta?.command = "QuoteWatch"
        aaplResponse.data = mutableListOf(mutableMapOf(Pair("Bomb", "Bay" as Any)))

        var googResponse = QuoteWatchResponse()
        googResponse.meta?.requestId = googRequestId
        googResponse.meta?.command = "QuoteWatch"
        googResponse.data = mutableListOf(mutableMapOf(Pair("Bomb", "Diggidy" as Any)))

        assertTrue(testObject.handleMessage(Gson().toJson(googResponse)))
        assertTrue(testObject.handleMessage(Gson().toJson(aaplResponse)))

        assertEquals("Bay", quoteWatchRepository.subscribe("AAPL").value?.data?.first()?.get("Bomb") as? String)
        assertEquals("Diggidy", quoteWatchRepository.subscribe("GOOG").value?.data?.first()?.get("Bomb") as? String)
    }

    @Test
    fun snapExpressionSendsWatchMessageToWebsocket() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.snapQuote("AAPL", true)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        assertEquals(1, mockNetworkExecutor.executeCount)
        assertEquals(true, argumentCaptor.firstValue.contains("QuoteSnap"))
        assertEquals(true, argumentCaptor.firstValue.contains("AAPL"))


    }

    @Test
    fun snapQuoteSendsWatchMessageToWebsocketWrappedInSingleQuotes() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.snapQuote("AAPL", false)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        assertEquals(1, mockNetworkExecutor.executeCount)
        assertEquals(true, argumentCaptor.firstValue.contains("QuoteSnap"))
        assertEquals(true, argumentCaptor.firstValue.contains("\\u0027AAPL\\u0027"))


    }

    @Test
    fun snapWatchResponsesAreSentToRepo() {

        val argumentCaptor = argumentCaptor<String>()

        testObject.snapQuote("AAPL", true)
        testObject.snapQuote("GOOG", true)

        verify(mockWebsocketService, times(2)).sendMessage(argumentCaptor.capture())

        val aaplRequestId = Gson().fromJson(argumentCaptor.firstValue, QuoteWatchRequest::class.java).meta.requestId
        val googRequestId = Gson().fromJson(argumentCaptor.lastValue, QuoteWatchRequest::class.java).meta.requestId

        var aaplResponse = QuoteWatchResponse()
        aaplResponse.meta?.requestId = aaplRequestId
        aaplResponse.meta?.command = "QuoteSnap"
        aaplResponse.data = mutableListOf(mutableMapOf(Pair("Bomb", "Bay" as Any)))

        var googResponse = QuoteWatchResponse()
        googResponse.meta?.requestId = googRequestId
        googResponse.meta?.command = "QuoteSnap"
        googResponse.data = mutableListOf(mutableMapOf(Pair("Bomb", "Diggidy" as Any)))

        assertTrue(testObject.handleMessage(Gson().toJson(googResponse)))
        assertTrue(testObject.handleMessage(Gson().toJson(aaplResponse)))

        assertEquals("Bay", quoteWatchRepository.subscribe("AAPL").value?.data?.first()?.get("Bomb") as? String)
        assertEquals("Diggidy", quoteWatchRepository.subscribe("GOOG").value?.data?.first()?.get("Bomb") as? String)
    }

    @Test
    fun unwatchQuoteSendsMessageToWebsocket() {

        val argumentCaptor = argumentCaptor<String>()

        testObject.watchQuote("AAPL", true)
        testObject.unwatchQuote("AAPL")

        verify(mockWebsocketService, times(2)).sendMessage(argumentCaptor.capture())

        assertEquals(true, argumentCaptor.lastValue.contains("Unwatch"))
    }

    @Test
    fun unwatchDoesNotSendMessageIfAlreadyUnwatched() {

        testObject.watchQuote("AAPL", true)
        testObject.unwatchQuote("AAPL")

        reset(mockWebsocketService)

        testObject.unwatchQuote("AAPL")

        verifyNoMoreInteractions(mockWebsocketService)

    }

    @Test
    fun unwatchAllUnwatchesAllSymbols() {
        val watchCaptor = argumentCaptor<String>()

        testObject.watchQuote("AAPL", true)
        testObject.watchQuote("GOOG", true)

        verify(mockWebsocketService, times(2)).sendMessage(watchCaptor.capture())

        val aaplRequestId = Gson().fromJson(watchCaptor.firstValue, QuoteWatchRequest::class.java).meta.requestId
        val googRequestId = Gson().fromJson(watchCaptor.lastValue, QuoteWatchRequest::class.java).meta.requestId

        reset(mockWebsocketService)
        val unwatchCaptor = argumentCaptor<String>()

        testObject.unwatchAll()

        verify(mockWebsocketService, times(2)).sendMessage(unwatchCaptor.capture())

        assertNotNull(unwatchCaptor.allValues.firstOrNull { it.contains(aaplRequestId) })
        assertNotNull(unwatchCaptor.allValues.firstOrNull { it.contains(googRequestId) })
    }

    @Test
    fun watchDuplicateQuoteDoesNotSendMessageToWebsocket() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.watchQuote("AAPL", true)
        testObject.watchQuote("AAPL", true)

        reset(mockWebsocketService)

        testObject.unwatchQuote("AAPL")
        verifyNoMoreInteractions(mockWebsocketService)

        testObject.unwatchQuote("AAPL")

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

    }

    @Test
    fun unwatchDuplicateSymbolDoesNotSendMessageToWebsocketUntilLastDuplicateIsUnwatched() {

    }

}