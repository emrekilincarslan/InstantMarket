package com.fondova.finance.api.auth

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.*
import com.fondova.finance.AppExecutors
import com.fondova.finance.InlineExecutor
import com.fondova.finance.api.Resource
import com.fondova.finance.api.Status
import com.fondova.finance.api.model.Credentials
import com.fondova.finance.api.model.base.ApiError
import com.fondova.finance.api.model.base.MetaResponse
import com.fondova.finance.api.model.base.WebsocketApiResponse
import com.fondova.finance.api.model.login.LoginResponse
import com.fondova.finance.api.socket.WebsocketService
import com.fondova.finance.persistance.AppStorageInterface
import com.fondova.finance.repo.TextsRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class WebsocketAuthServiceTest {

    @Rule
    fun rule(): TestRule { return InstantTaskExecutorRule() }

    lateinit var testObject: WebsocketAuthService

    @Mock
    lateinit var mockWebsocketService: WebsocketService
    @Mock
    lateinit var mockAppExecutors: AppExecutors
    @Mock
    lateinit var mockAppStorage: AppStorageInterface
    @Mock
    lateinit var mockTextsRepository: TextsRepository

    val mockNetworkExecutor: InlineExecutor = InlineExecutor()
    val mockMainExecutor: InlineExecutor = InlineExecutor()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        testObject = WebsocketAuthService(mockWebsocketService, mockAppExecutors, mockAppStorage, mockTextsRepository)
        whenever(mockAppExecutors.networkIO()).thenReturn(mockNetworkExecutor)
        whenever(mockAppExecutors.mainThread()).thenReturn(mockMainExecutor)
        whenever(mockTextsRepository.appNameForApiCalls()).thenReturn("TestApp")
        whenever(mockTextsRepository.appVersion()).thenReturn("1.0.0")
    }

    @Test
    fun connectWebsocketOnLogin() {
        val testCredentials = Credentials()
        testCredentials.username = "bob"
        testCredentials.password = "ross"
        testObject.authenticate(testCredentials, object : AuthenticationResponseListener {
            override fun onAuthenticationResponse(response: Resource<LoginResponse>) {
                // Don't care in this test
            }
        })

        verify(mockWebsocketService).connect()
    }

    @Test
    fun loginRequestSendsMessageOnConnect() {

        whenever(mockAppStorage.getRefreshRateAsInt()).thenReturn(10)
        val argumentCaptor = argumentCaptor<String>()
        val testCredentials = Credentials()
        testCredentials.username = "bob"
        testCredentials.password = "ross"
        testObject.authenticate(testCredentials, object : AuthenticationResponseListener {
            override fun onAuthenticationResponse(response: Resource<LoginResponse>) {
                // Don't care in this test
            }
        })

        testObject.onConnected(mockWebsocketService)

        verify(mockWebsocketService).sendMessage(argumentCaptor.capture())

        assertEquals(1, mockNetworkExecutor.executeCount)
        assertEquals(true, argumentCaptor.firstValue.contains("\"command\":\"Login\""))
        assertEquals(true, argumentCaptor.firstValue.contains("\"defaultUpdateInterval\":10.0"))
        assertEquals(true, argumentCaptor.firstValue.contains("\"username\":\"bob\""))
        assertEquals(true, argumentCaptor.firstValue.contains("\"password\":\"ross\""))
        assertEquals(true, argumentCaptor.firstValue.contains("\"appname\":\"TestApp\""))
        assertEquals(true, argumentCaptor.firstValue.contains("\"version\":\"1.0.0\""))
    }

    @Test
    fun loginRequestSendsUsernameInLowerCase() {

        whenever(mockAppStorage.getRefreshRateAsInt()).thenReturn(10)
        val argumentCaptor = argumentCaptor<String>()
        val testCredentials = Credentials()
        testCredentials.username = "Bob"
        testCredentials.password = "ross"
        testObject.authenticate(testCredentials, object : AuthenticationResponseListener {
            override fun onAuthenticationResponse(response: Resource<LoginResponse>) {
                // Don't care in this test
            }
        })

        testObject.onConnected(mockWebsocketService)

        verify(mockWebsocketService).sendMessage(argumentCaptor.capture())

        assertEquals(1, mockNetworkExecutor.executeCount)
        assertEquals(true, argumentCaptor.firstValue.contains("\"username\":\"bob\""))
    }

    @Test
    fun connectWebsocketOnReconnect() {
        val testCredentials = Credentials()
        testCredentials.username = "bob"
        testCredentials.password = "ross"
        testObject.authenticate(testCredentials, object : AuthenticationResponseListener {
            override fun onAuthenticationResponse(response: Resource<LoginResponse>) {
                // Don't care in this test
            }
        })
        reset(mockWebsocketService)

        testObject.reconnect(object : AuthenticationResponseListener {
            override fun onAuthenticationResponse(response: Resource<LoginResponse>) {
                // Don't care in this test
            }
        })

        verify(mockWebsocketService).connect()
    }

    @Test
    fun reconnectSendsLoginRequestWithStoredCredentialsOnConnect() {
        whenever(mockAppStorage.getRefreshRateAsInt()).thenReturn(10)
        val argumentCaptor = argumentCaptor<String>()
        val testCredentials = Credentials()
        testCredentials.username = "bob"
        testCredentials.password = "ross"
        testObject.authenticate(testCredentials, object : AuthenticationResponseListener {
            override fun onAuthenticationResponse(response: Resource<LoginResponse>) {
                // Don't care in this test
            }
        })
        reset(mockWebsocketService)

        testObject.reconnect(object : AuthenticationResponseListener {
            override fun onAuthenticationResponse(response: Resource<LoginResponse>) {
                // Don't care in this test
            }
        })

        testObject.onConnected(mockWebsocketService)

        verify(mockWebsocketService).sendMessage(argumentCaptor.capture())

        assertEquals(true, argumentCaptor.firstValue.contains("\"command\":\"Login\""))
        assertEquals(true, argumentCaptor.firstValue.contains("\"defaultUpdateInterval\":10.0"))
        assertEquals(true, argumentCaptor.firstValue.contains("\"username\":\"bob\""))
        assertEquals(true, argumentCaptor.firstValue.contains("\"password\":\"ross\""))
        assertEquals(true, argumentCaptor.firstValue.contains("\"appname\":\"TestApp\""))
        assertEquals(true, argumentCaptor.firstValue.contains("\"version\":\"1.0.0\""))
    }

    @Test
    fun reconnectRespondsWithAnErrorWhenStoredCredentialsAreMissing() {
        whenever(mockAppStorage.getRefreshRateAsInt()).thenReturn(10)
        var errorResponse: Resource<LoginResponse>? = null
        val argumentCaptor = argumentCaptor<String>()
        val testCredentials = Credentials()
        testCredentials.username = "bob"
        testCredentials.password = "ross"
        testObject.reconnect(object : AuthenticationResponseListener {
            override fun onAuthenticationResponse(response: Resource<LoginResponse>) {
                errorResponse = response
            }
        })

        testObject.onConnected(mockWebsocketService)

        verify(mockWebsocketService).sendMessage(argumentCaptor.capture())

        assertEquals(Status.ERROR, errorResponse?.status)
        assertEquals("Login Failed", errorResponse?.title)
        assertEquals("Missing Credentials", errorResponse?.message)
    }

    @Test
    fun loginRequestRespondsWithErrorForEmptyUsername() {
        val testCredentials = Credentials()
        testCredentials.username = ""
        testCredentials.password = "test"
        var errorResponse: Resource<LoginResponse>? = null
        testObject.authenticate(testCredentials, object : AuthenticationResponseListener {
            override fun onAuthenticationResponse(response: Resource<LoginResponse>) {
                errorResponse = response
            }
        })

        assertEquals(Status.ERROR, errorResponse?.status)
        assertEquals("Login Failed", errorResponse?.title)
        assertEquals("Missing Credentials", errorResponse?.message)

    }

    @Test
    fun loginRequestRespondsWithErrorForEmptyPassword() {
        val testCredentials = Credentials()
        testCredentials.username = "test"
        testCredentials.password = ""
        var errorResponse: Resource<LoginResponse>? = null
        testObject.authenticate(testCredentials, object : AuthenticationResponseListener {
            override fun onAuthenticationResponse(response: Resource<LoginResponse>) {
                errorResponse = response
            }
        })

        assertEquals(Status.ERROR, errorResponse?.status)
        assertEquals("Login Failed", errorResponse?.title)
        assertEquals("Missing Credentials", errorResponse?.message)

    }

    @Test
    fun loginSuccessResponseMessageIsSentToCallback() {
        val testCredentials = Credentials()
        testCredentials.username = "bob"
        testCredentials.password = "ross"
        var receivedResponse: LoginResponse? = null
        testObject.authenticate(testCredentials, object : AuthenticationResponseListener {
            override fun onAuthenticationResponse(response: Resource<LoginResponse>) {
                receivedResponse = response.data
            }
        })

        val testResponse = LoginResponse()
        testResponse.meta = MetaResponse()
        testResponse.meta.status = 200
        testResponse.meta.command = "Login"
        testResponse.meta.requestId = testObject.loginRequestId

        assertTrue(testObject.handleMessage(Gson().toJson(testResponse)))

        assertEquals(1, mockMainExecutor.executeCount)
        assertEquals(200, receivedResponse?.meta?.status)
        assertEquals("Login", receivedResponse?.meta?.command)

    }

    @Test
    fun loginErrorResponseMessageIsSentToCallback() {
        val testCredentials = Credentials()
        testCredentials.username = "bob"
        testCredentials.password = "ross"
        var receivedResponse: Resource<LoginResponse>? = null
        testObject.authenticate(testCredentials, object : AuthenticationResponseListener {
            override fun onAuthenticationResponse(response: Resource<LoginResponse>) {
                receivedResponse = response
            }
        })

        val testResponse = LoginResponse()
        testResponse.meta = MetaResponse()
        testResponse.meta.status = 401
        testResponse.meta.command = "Login"
        testResponse.meta.requestId = testObject.loginRequestId
        val error = ApiError()
        error.code = "Login Failed"
        error.detail = "Bad Credentials"
        testResponse.errors = listOf(error)

        assertTrue(testObject.handleMessage(Gson().toJson(testResponse)))

        assertEquals(1, mockMainExecutor.executeCount)
        assertEquals(Status.ERROR, receivedResponse?.status)
        assertEquals("Login Failed", receivedResponse?.title)
        assertEquals("Bad Credentials", receivedResponse?.message)

    }

    @Test
    fun ignoreNonLoginResponses() {
        val testResponse = WebsocketApiResponse()
        testResponse.meta?.command = "QuoteWatch"
        testResponse.meta?.requestId = "2"

        assertFalse(testObject.handleMessage(Gson().toJson(testResponse)))

    }

}