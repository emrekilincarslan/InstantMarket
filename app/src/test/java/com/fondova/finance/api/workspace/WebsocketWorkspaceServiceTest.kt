package com.fondova.finance.api.workspace

import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.*
import com.fondova.finance.AppExecutors
import com.fondova.finance.InlineExecutor
import com.fondova.finance.api.model.base.WebsocketApiRequest
import com.fondova.finance.api.model.base.WebsocketApiResponse
import com.fondova.finance.api.model.workspace.*
import com.fondova.finance.api.socket.WebsocketService
import com.fondova.finance.workspace.Workspace
import com.fondova.finance.workspace.instantmarket.IMWorkspace
import com.fondova.finance.persistance.fromJson
import com.fondova.finance.workspace.service.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class WebsocketWorkspaceServiceTest {

    @Mock lateinit var mockAppExecutors: AppExecutors
    @Mock lateinit var mockWebsocketService: WebsocketService
    val mockNetworkExecutor: InlineExecutor = InlineExecutor()
    val mockMainExecutor: InlineExecutor = InlineExecutor()

    lateinit var testObject: WebsocketWorkspaceService

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        testObject = WebsocketWorkspaceService(mockWebsocketService, mockAppExecutors)
        whenever(mockAppExecutors.networkIO()).thenReturn(mockNetworkExecutor)
        whenever(mockAppExecutors.mainThread()).thenReturn(mockMainExecutor)
    }

    @Test
    fun fetchWorkspaceListSendsMessageToWebSocket() {

        val argumentCaptor = argumentCaptor<String>()

        testObject.fetchWorkspaceList(object : OnWorkspaceListReceivedListener {
            override fun onWorkspaceListReceived(workspaces: List<Workspace>, error: String?) {
                // Don't care in this test
            }
        })

        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        assertEquals(1, mockNetworkExecutor.executeCount)
        assertEquals(true, argumentCaptor.firstValue.contains("WorkspaceNames"))
        assertEquals(true, argumentCaptor.firstValue.contains("WebRequest"))
    }

    @Test
    fun fetchWorkspaceListResponsesMapToCorrectCall() {
        val argumentCaptor = argumentCaptor<String>()
        var firstResponseCount: Int = 0
        var secondResponseCount: Int = 0

        testObject.fetchWorkspaceList(object : OnWorkspaceListReceivedListener {
            override fun onWorkspaceListReceived(workspaces: List<Workspace>, error: String?) {
                firstResponseCount = workspaces.count()
            }
        })

        testObject.fetchWorkspaceList(object : OnWorkspaceListReceivedListener {
            override fun onWorkspaceListReceived(workspaces: List<Workspace>, error: String?) {
                secondResponseCount = workspaces.count()
            }
        })



        verify(mockWebsocketService, times(2)).sendMessage(argumentCaptor.capture())

        val request1: WebsocketApiRequest = Gson().fromJson(argumentCaptor.allValues.first(), WorkspaceListRequest::class.java)
        val request2: WebsocketApiRequest = Gson().fromJson(argumentCaptor.allValues.last(), WorkspaceListRequest::class.java)

        val response = WorkspaceListResponse()
        response.meta?.command = "WebRequest"
        val workspace1 = IMWorkspace(mutableMapOf())
        workspace1.setDefault(true)

        val workspace2 = IMWorkspace(mutableMapOf())
        workspace2.setDefault(true)

        val firstResponseWorkspaces = listOf(workspace1, workspace2)
        val firstResponseData = WorkspaceListResponseData()
        firstResponseData.setWorkspaces(firstResponseWorkspaces)
        val secondResponseWorkspaces = listOf(workspace1)
        val secondResponseData = WorkspaceListResponseData()
        secondResponseData.setWorkspaces(secondResponseWorkspaces)

        response.meta?.requestId = request2.meta.requestId
        response.data = listOf(secondResponseData)
        testObject.handleMessage(Gson().toJson(response))

        response.meta?.requestId = request1.meta.requestId
        response.data = listOf(firstResponseData)
        testObject.handleMessage(Gson().toJson(response))

        assertEquals(2, firstResponseCount)
        assertEquals(1, secondResponseCount)
        assertEquals(2, mockMainExecutor.executeCount)
    }

    @Test
    fun updateDefaultWorkspaceSendsMessageToWebSocket() {
        val argumentCaptor = argumentCaptor<String>()
        var callbackInvoked = false
        whenever(mockWebsocketService.sendMessage(any())).thenAnswer {
            var incoingMessage = it.arguments[0] as String
            var requestId = Gson().fromJson<WebsocketApiRequest>(incoingMessage).meta.requestId
            var response = WebsocketApiResponse()
            response.meta?.requestId = requestId
            testObject.handleMessage(Gson().toJson(response))
        }

        testObject.setDefaultWorkspace("old-workspace-id", "new-workspace-id", object : EmptyResponseListener {
            override fun onResponse() {
                callbackInvoked = true
            }

        })
        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        assertEquals(1, mockNetworkExecutor.executeCount)
        assertEquals(true, argumentCaptor.firstValue.contains("Workspace"))
        assertEquals(true, argumentCaptor.firstValue.contains("WebRequest"))
        assertEquals(true, argumentCaptor.firstValue.contains("old-workspace-id"))
        assertEquals(true, argumentCaptor.firstValue.contains("new-workspace-id"))
        assertEquals(true, callbackInvoked)

    }

    @Test
    fun fetchWorkspaceDetailsSendsMessageToWebSocket() {

        val argumentCaptor = argumentCaptor<String>()

        testObject.fetchWorkspaceDetails("workspace-id", object : OnWorkspaceDetailsReceivedListener {
            override fun onWorkspaceDataReceived(workspace: Workspace?, error: String?) {
                // Don't care in this test
            }
        })
        verify(mockWebsocketService, times(1)).sendMessage(argumentCaptor.capture())

        assertEquals(1, mockNetworkExecutor.executeCount)
        assertEquals(true, argumentCaptor.firstValue.contains("Workspace"))
        assertEquals(true, argumentCaptor.firstValue.contains("WebRequest"))
    }

    @Test
    fun fetchWorkspaceDetailsResponsesMapToCorrectCall() {
        val argumentCaptor = argumentCaptor<String>()
        var firstWorkspaceId: String? = null
        var secondWorkspaceId: String? = null

        testObject.fetchWorkspaceDetails("workspace-id-1", object : OnWorkspaceDetailsReceivedListener {
            override fun onWorkspaceDataReceived(workspace: Workspace?, error: String?) {
                firstWorkspaceId = workspace?.getWorkspaceId()
            }
        })

        testObject.fetchWorkspaceDetails("workspace-id-2", object : OnWorkspaceDetailsReceivedListener {
            override fun onWorkspaceDataReceived(workspace: Workspace?, error: String?) {
                secondWorkspaceId = workspace?.getWorkspaceId()
            }
        })



        verify(mockWebsocketService, times(2)).sendMessage(argumentCaptor.capture())

        val request1: WebsocketApiRequest = Gson().fromJson(argumentCaptor.allValues.first(), WorkspaceListRequest::class.java)
        val request2: WebsocketApiRequest = Gson().fromJson(argumentCaptor.allValues.last(), WorkspaceListRequest::class.java)

        val response = WorkspaceDetailsResponse()
        response.meta?.command = "WebRequest"
        val workspace1 = IMWorkspace()//"workspace-id-1", "", true, null)
        workspace1.setWorkspaceId("workspace-id-1")
        workspace1.setDefault(true)

        val workspace2 = IMWorkspace()//"workspace-id-2", "", true, null)
        workspace2.setWorkspaceId("workspace-id-2")
        workspace2.setDefault(true)

        response.meta?.requestId = request2.meta.requestId

        val workspaceDetailsResponseData = WorkspaceDetailsResponseData()
        workspaceDetailsResponseData.setWorksapce(workspace2)

        response.data = mutableListOf(workspaceDetailsResponseData)
        testObject.handleMessage(Gson().toJson(response))

        response.meta?.requestId = request1.meta.requestId
        workspaceDetailsResponseData.setWorksapce(workspace1)
        response.data = mutableListOf(workspaceDetailsResponseData)
        testObject.handleMessage(Gson().toJson(response))

        assertEquals("workspace-id-1", firstWorkspaceId)
        assertEquals("workspace-id-2", secondWorkspaceId)
        assertEquals(2, mockMainExecutor.executeCount)
    }

    @Test
    fun fetchDefaultWorkspaceFetchesDetailsOfWorkspaceMarkedAsDefault() {

        var fetchedWorkspace: Workspace? = null
        var fetchedWorkspaceList: List<Workspace>? = null
        val defaultWorkspace = IMWorkspace()//"default workspace", "", true, null)
        defaultWorkspace.setWorkspaceId("default workspace")
        defaultWorkspace.setDefault(true)

        val nonDefaultWorkspace = IMWorkspace()//"non-default workspace", "", false, null)
        nonDefaultWorkspace.setWorkspaceId("non-default workspace")
        nonDefaultWorkspace.setDefault(false)

        whenever(mockWebsocketService.sendMessage(argThat<String> { this.contains("WorkspaceNames")})).thenAnswer {
            val requestId = Gson().fromJson(it.arguments[0] as String, WorkspaceListRequest::class.java)?.meta?.requestId
            val response = WorkspaceListResponse()
            response.meta?.command = "WebRequest"
            response.meta?.requestId = requestId
            val firstResponseWorkspaces = listOf(nonDefaultWorkspace, defaultWorkspace)
            val firstResponseData = WorkspaceListResponseData()
            firstResponseData.setWorkspaces(firstResponseWorkspaces)

            response.data = listOf(firstResponseData)
            testObject.handleMessage(Gson().toJson(response))
        }

        whenever(mockWebsocketService.sendMessage(argThat<String> { this.contains("default workspace")})).thenAnswer {
            val requestId = Gson().fromJson(it.arguments[0] as String, WorkspaceListRequest::class.java)?.meta?.requestId
            val response = WorkspaceDetailsResponse()
            response.meta?.requestId = requestId
            response.meta?.command = "WebRequest"

            val workspaceDetailsResponseData = WorkspaceDetailsResponseData()

            workspaceDetailsResponseData.data = defaultWorkspace.map
            response.data = mutableListOf(workspaceDetailsResponseData)
            testObject.handleMessage(Gson().toJson(response))
        }

        whenever(mockWebsocketService.sendMessage(argThat<String> { this.contains("non-default workspace")})).thenAnswer {
            val requestId = Gson().fromJson(it.arguments[0] as String, WorkspaceListRequest::class.java)?.meta?.requestId
            val response = WorkspaceDetailsResponse()
            response.meta?.requestId = requestId
            response.meta?.command = "WebRequest"

            val workspaceDetailsResponseData = WorkspaceDetailsResponseData()
            workspaceDetailsResponseData.data = nonDefaultWorkspace.map

            response.data = mutableListOf(workspaceDetailsResponseData)

            testObject.handleMessage(Gson().toJson(response))
        }

        testObject.fetchDefaultWorkspace(object : OnDefaultWorkspaceReceivedListener {
            override fun onWorkspaceDataReceived(workspaces: List<Workspace>, default: Workspace?, error: String?) {
                fetchedWorkspace = default
                fetchedWorkspaceList = workspaces
            }
        })

        assertEquals("default workspace", fetchedWorkspace?.getWorkspaceId())
        assertEquals(2, fetchedWorkspaceList?.size)
        assertNotNull(fetchedWorkspaceList?.firstOrNull { it.getWorkspaceId() == "default workspace" })
        assertNotNull(fetchedWorkspaceList?.firstOrNull { it.getWorkspaceId() == "non-default workspace" })
    }

    @Test
    fun fetchDefaultWorkspaceFetchesDetailsOfFirstWorkspaceIfNoneMarkedAsDefault() {

        var fetchedWorkspace: Workspace? = null
        var fetchedWorkspaceList: List<Workspace>? = null
        val workspace1 = IMWorkspace()//"default workspace", "", true, null)
        workspace1.setWorkspaceId("non-default workspace 1")
        workspace1.setDefault(false)

        val workspace2 = IMWorkspace()//"non-default workspace", "", false, null)
        workspace2.setWorkspaceId("non-default workspace 2")
        workspace2.setDefault(false)

        whenever(mockWebsocketService.sendMessage(argThat<String> { this.contains("WorkspaceNames")})).thenAnswer {
            val requestId = Gson().fromJson(it.arguments[0] as String, WorkspaceListRequest::class.java)?.meta?.requestId
            val response = WorkspaceListResponse()
            response.meta?.command = "WebRequest"
            response.meta?.requestId = requestId
            val firstResponseWorkspaces = listOf(workspace1, workspace2)
            val firstResponseData = WorkspaceListResponseData()
            firstResponseData.setWorkspaces(firstResponseWorkspaces)

            response.data = listOf(firstResponseData)
            testObject.handleMessage(Gson().toJson(response))
        }

        whenever(mockWebsocketService.sendMessage(argThat<String> { this.contains("non-default workspace 1")})).thenAnswer {
            val requestId = Gson().fromJson(it.arguments[0] as String, WorkspaceListRequest::class.java)?.meta?.requestId
            val response = WorkspaceDetailsResponse()
            response.meta?.requestId = requestId
            response.meta?.command = "WebRequest"

            val workspaceDetailsResponseData = WorkspaceDetailsResponseData()

            workspaceDetailsResponseData.data = workspace1.map
            response.data = mutableListOf(workspaceDetailsResponseData)
            testObject.handleMessage(Gson().toJson(response))
        }

        whenever(mockWebsocketService.sendMessage(argThat<String> { this.contains("non-default workspace 2")})).thenAnswer {
            val requestId = Gson().fromJson(it.arguments[0] as String, WorkspaceListRequest::class.java)?.meta?.requestId
            val response = WorkspaceDetailsResponse()
            response.meta?.requestId = requestId
            response.meta?.command = "WebRequest"

            val workspaceDetailsResponseData = WorkspaceDetailsResponseData()
            workspaceDetailsResponseData.data = workspace2.map

            response.data = mutableListOf(workspaceDetailsResponseData)

            testObject.handleMessage(Gson().toJson(response))
        }

        testObject.fetchDefaultWorkspace(object : OnDefaultWorkspaceReceivedListener {
            override fun onWorkspaceDataReceived(workspaces: List<Workspace>, default: Workspace?, error: String?) {
                fetchedWorkspace = default
                fetchedWorkspaceList = workspaces
            }
        })

        assertEquals("non-default workspace 1", fetchedWorkspace?.getWorkspaceId())
        assertEquals(2, fetchedWorkspaceList?.size)
    }

    @Test
    fun fetchDefaultWorkspaceFetchesBlankWorkspaceIfNoWorkspacesArePresent() {

        var fetchedWorkspace: Workspace? = null
        var fetchedWorkspaceList: List<Workspace>? = emptyList()

        whenever(mockWebsocketService.sendMessage(argThat<String> { this.contains("WorkspaceNames")})).thenAnswer {
            val requestId = Gson().fromJson(it.arguments[0] as String, WorkspaceListRequest::class.java)?.meta?.requestId
            val response = WorkspaceListResponse()
            response.meta?.command = "WebRequest"
            response.meta?.requestId = requestId
            val firstResponseWorkspaces = emptyList<IMWorkspace>()
            val firstResponseData = WorkspaceListResponseData()
            firstResponseData.setWorkspaces(firstResponseWorkspaces)

            response.data = listOf(firstResponseData)
            testObject.handleMessage(Gson().toJson(response))
        }

        testObject.fetchDefaultWorkspace(object : OnDefaultWorkspaceReceivedListener {
            override fun onWorkspaceDataReceived(workspaces: List<Workspace>, default: Workspace?, error: String?) {
                fetchedWorkspace = default
                fetchedWorkspaceList = workspaces
            }
        })

        assertNotNull(fetchedWorkspace?.getWorkspaceId())
        assertEquals("Workspace", fetchedWorkspace?.getName())
        assertEquals(0, fetchedWorkspace?.getGroups()?.size)
        assertEquals(1, fetchedWorkspaceList?.size)
    }

}