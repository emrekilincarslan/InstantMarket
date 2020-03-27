package com.fondova.finance.api.news

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.fondova.finance.AppExecutors
import com.fondova.finance.InlineExecutor
import com.fondova.finance.api.Resource
import com.fondova.finance.api.Status
import com.fondova.finance.api.model.base.MetaResponse
import com.fondova.finance.api.model.news.*
import com.fondova.finance.api.socket.WebsocketService
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

class WebsocketNewsServiceTest {

    @Rule
    fun rule(): TestRule { return InstantTaskExecutorRule() }

    lateinit var testObject: WebsocketNewsService

    @Mock
    lateinit var mockWebsocketService: WebsocketService
    @Mock
    lateinit var mockAppExecutors: AppExecutors
    val mockNetworkExecutor: InlineExecutor = InlineExecutor()
    val mockMainExecutor: InlineExecutor = InlineExecutor()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        testObject = WebsocketNewsService(mockWebsocketService, mockAppExecutors)
        whenever(mockAppExecutors.networkIO()).thenReturn(mockNetworkExecutor)
        whenever(mockAppExecutors.mainThread()).thenReturn(mockMainExecutor)
    }

    @Test
    fun watchNewsSendsMessage() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.watchSymbolNews("AAPL", 3)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        assertEquals(1, mockNetworkExecutor.executeCount)
        assertEquals(true, argumentCaptor.firstValue.contains("NewsWatch"))
        assertEquals(true, argumentCaptor.firstValue.contains("AAPL"))
        assertEquals(true, argumentCaptor.firstValue.contains("3"))

    }

    @Test
    fun getNewsArticleSendsMessage() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.getNewsArticle("XXX-XXX", null)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        assertEquals(1, mockNetworkExecutor.executeCount)
        assertEquals(true, argumentCaptor.firstValue.contains("NewsPageSnap"))
        assertEquals(true, argumentCaptor.firstValue.contains("XXX-XXX"))

    }

    @Test
    fun sendLoadingMessageToLiveDataOnWatch() {

        testObject.watchSymbolNews("AAPL", 3)

        assertEquals(1, mockMainExecutor.executeCount)

        assertEquals(Status.LOADING, testObject.getNewsLiveData().value?.status)

    }

    @Test
    fun sendWatchResponseToLiveData() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.watchSymbolNews("AAPL", 3)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        val requestId = Gson().fromJson(argumentCaptor.firstValue, NewsWatchRequest::class.java).meta.requestId

        val testResponse = NewsWatchResponse()
        testResponse.meta = NewsWatchResponse.NewsWatchResponseMeta()
        testResponse.meta.requestId = requestId
        val article = CategoryArticle()
        article.title = "Boom!"
        testResponse.data = listOf(article)

        assertTrue(testObject.handleMessage(Gson().toJson(testResponse)))

        assertEquals("Boom!", testObject.getNewsLiveData().value?.data?.data?.firstOrNull()?.title)

    }

    @Test
    fun ignoreNonNewsWatchMessages() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.watchSymbolNews("AAPL", 3)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())


        val testResponse = NewsWatchResponse()
        testResponse.meta = NewsWatchResponse.NewsWatchResponseMeta()
        testResponse.meta.requestId = UUID.randomUUID().toString()

        assertFalse(testObject.handleMessage(Gson().toJson(testResponse)))
    }

    @Test
    fun ignoreNonNewsSnaphMessages() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.getNewsArticle("AAPL", null)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())


        val testResponse = NewsWatchResponse()
        testResponse.meta = NewsWatchResponse.NewsWatchResponseMeta()
        testResponse.meta.requestId = UUID.randomUUID().toString()

        assertFalse(testObject.handleMessage(Gson().toJson(testResponse)))
    }

    @Test
    fun sendPageSnapResponseToCallback() {
        val argumentCaptor = argumentCaptor<String>()
        var actualResponse: Resource<NewsPageSnapResponse>? = null

        testObject.getNewsArticle("AAPL", object : OnNewsArticleReceivedListener {
            override fun onNewsArticleReceived(response: Resource<NewsPageSnapResponse>) {
                actualResponse = response
            }

        })

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        val requestId = Gson().fromJson(argumentCaptor.firstValue, NewsPageSnapRequest::class.java).meta.requestId

        val testResponse = NewsPageSnapResponse()
        testResponse.meta = MetaResponse()
        testResponse.meta.requestId = requestId
        val newsSnap = NewsSnap()
        newsSnap.body = "Booyah!"
        testResponse.data = listOf(newsSnap)

        assertTrue(testObject.handleMessage(Gson().toJson(testResponse)))

        assertEquals("Booyah!", actualResponse?.data?.data?.firstOrNull()?.body)
    }

    @Test
    fun replaceDuplicateArticles() {
        val argumentCaptor = argumentCaptor<String>()

        testObject.watchSymbolNews("AAPL", 3)

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        val requestId = Gson().fromJson(argumentCaptor.firstValue, NewsWatchRequest::class.java).meta.requestId

        val testResponse = NewsWatchResponse()
        testResponse.meta = NewsWatchResponse.NewsWatchResponseMeta()
        testResponse.meta.requestId = requestId
        val article1 = CategoryArticle()
        article1.title = "Article 1"
        article1.storyId = "1"

        val article2 = CategoryArticle()
        article2.title = "Article 2"
        article2.storyId = "2"

        val article3 = CategoryArticle()
        article3.title = "Article 3"
        article3.storyId = "3"

        testResponse.data = listOf(article1, article2)

        assertTrue(testObject.handleMessage(Gson().toJson(testResponse)))

        article2.title = "Updated Article 2"

        testResponse.data = listOf(article2, article3)

        assertTrue(testObject.handleMessage(Gson().toJson(testResponse)))

        assertEquals(3, testObject.getNewsLiveData().value?.data?.data?.size)
        assertEquals("Updated Article 2", testObject.getNewsLiveData().value?.data?.data?.get(0)?.title)
        assertEquals("Article 3", testObject.getNewsLiveData().value?.data?.data?.get(1)?.title)
        assertEquals("Article 1", testObject.getNewsLiveData().value?.data?.data?.get(2)?.title)
    }

}