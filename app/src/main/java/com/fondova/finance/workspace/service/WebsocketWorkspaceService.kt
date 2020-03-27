package com.fondova.finance.workspace.service

import android.util.Log
import com.google.gson.Gson
import com.fondova.finance.AppExecutors
import com.fondova.finance.api.model.base.WebsocketApiRequest
import com.fondova.finance.api.model.base.WebsocketApiResponse
import com.fondova.finance.api.model.workspace.*
import com.fondova.finance.api.socket.WebsocketResponseHandler
import com.fondova.finance.api.socket.WebsocketService
import com.fondova.finance.api.socket.WebsocketServiceListener
import com.fondova.finance.workspace.instantmarket.IMWorkspace
import com.fondova.finance.workspace.Workspace
import com.fondova.finance.workspace.WorkspaceFactory
import java.io.IOException
import java.util.*

class WebsocketWorkspaceService(val websocketService: WebsocketService, val appExecutors: AppExecutors): WorkspaceService, WebsocketResponseHandler, WebsocketServiceListener {

    val callbackMap: MutableMap<String, Any> = mutableMapOf()
    private var lastWorkspaceFetched: Workspace? = null

    init {
        websocketService.addListener(this)
    }

    override fun currentWorkspace(): Workspace? {
        return  lastWorkspaceFetched
    }

    override fun fetchWorkspaceList(callbackListener: OnWorkspaceListReceivedListener) {
        val request = WorkspaceListRequest()
        val requestId = request.meta.requestId
        callbackMap.set(requestId, callbackListener)
        sendRequest(request)
    }

    override fun fetchDefaultWorkspace(callbackListener: OnDefaultWorkspaceReceivedListener) {
        fetchWorkspaceList(object : OnWorkspaceListReceivedListener {
            override fun onWorkspaceListReceived(workspaces: List<Workspace>, error: String?) {
                var defaultWorkspace = workspaces.firstOrNull { it.isDefault() == true }
                if (defaultWorkspace == null) {
                    Log.e("WorkspaceService", "No default workspace found, defaulting to first workspace")
                    defaultWorkspace = workspaces.firstOrNull()
                }
                if (defaultWorkspace == null) {
                    Log.e("WorkspaceService", "No workspaces found, creating blank workspace")
                    defaultWorkspace = WorkspaceFactory().emptyWorkspace()
                    defaultWorkspace.setName("Workspace")
                    defaultWorkspace.setWorkspaceId(UUID.randomUUID().toString())
                    callbackListener.onWorkspaceDataReceived(listOf(defaultWorkspace), defaultWorkspace, error)
                    return
                }
                fetchWorkspaceDetails(defaultWorkspace.getWorkspaceId(), object : OnWorkspaceDetailsReceivedListener {
                    override fun onWorkspaceDataReceived(workspace: Workspace?, error: String?) {
                        callbackListener.onWorkspaceDataReceived(workspaces, workspace, error)
                    }
                })
            }
        })
    }

    override fun fetchWorkspaceDetails(workspaceId: String?, callbackListener: OnWorkspaceDetailsReceivedListener) {
        val request = WorkspaceDetailsRequest(workspaceId)
        val requestId = request.meta.requestId
        callbackMap.set(requestId, callbackListener)
        sendRequest(request)
    }

    override fun saveWorkspace(workspace: Workspace, callbackListener: EmptyResponseListener) {
        lastWorkspaceFetched = workspace
        if (workspace as? IMWorkspace == null) {
            Log.e("wsworkspaceservice", "Invalid Argument sent to saveWorkspace()")
            return
        }
        val request = PutWorkspaceRequest(workspace)
        val requestId = request.meta.requestId
        callbackMap.set(requestId, callbackListener)
        sendRequest(request)
    }

    override fun setDefaultWorkspace(oldWorkspaceId: String?, newWorkspaceId: String?, callbackListener: EmptyResponseListener) {
        if (oldWorkspaceId == null) {
            Log.e("${this.javaClass}", "oldWorkspaceId is null")
            return
        }
        val request = SetDefaultWorkspaceRequest(oldWorkspaceId, newWorkspaceId)
        val requestId = request.meta.requestId
        callbackMap.set(requestId, callbackListener)
        sendRequest(request)
    }

    override fun handleMessage(message: String): Boolean {
        val response: WebsocketApiResponse = Gson().fromJson(message, WebsocketApiResponse::class.java)
        val requestId = response.meta?.requestId
        if (requestId.isNullOrEmpty()) {
            return false
        }

        val callback = callbackMap.get(requestId)
        if (callback == null) {
            return false
        }

        callbackMap.remove(requestId)

        if (callback is OnWorkspaceListReceivedListener) {
            processWorkspaceListResponse(message, callback)
            return true
        }

        if (callback is OnWorkspaceDetailsReceivedListener) {
            processWorkspaceDetailsResponse(message, callback)
            return true
        }

        if (callback is EmptyResponseListener) {
            processEmptyResponse(callback)
            return true
        }

        return false
    }

    private fun processWorkspaceListResponse(message: String, listener: OnWorkspaceListReceivedListener) {
        val workspaceListResponse: WorkspaceListResponse = Gson().fromJson(message, WorkspaceListResponse::class.java)
        val workspaces = workspaceListResponse.data?.first()?.getWorkspaces() ?: emptyList()

        appExecutors.mainThread().execute {
            listener.onWorkspaceListReceived(workspaces, null)
        }
    }

    private fun processWorkspaceDetailsResponse(message: String, listener: OnWorkspaceDetailsReceivedListener) {
        val workspaceDetailsResponse: WorkspaceDetailsResponse = Gson().fromJson(message, WorkspaceDetailsResponse::class.java)
        val workspace = workspaceDetailsResponse.data?.first()?.getWorkspace()
        lastWorkspaceFetched = workspace

        appExecutors.mainThread().execute {
            listener.onWorkspaceDataReceived(workspace, null)
        }

    }

    private fun processEmptyResponse(listener: EmptyResponseListener) {
        appExecutors.mainThread().execute {
            listener.onResponse()
        }
    }

    private fun sendRequest(request: WebsocketApiRequest) {
        appExecutors.networkIO().execute {
            val json = Gson().toJson(request)
            Log.i("" + this.javaClass, String.format("Sending Request: %s", request.meta.command))
            Log.d("" + this.javaClass, json)
            websocketService.sendMessage(json)
        }
    }

    override fun onConnected(websocketService: WebsocketService) {
        // Don't care
    }

    override fun onDisconnected(websocketService: WebsocketService, code: Int, reason: String, closedByServer: Boolean) {
        // Don't care
    }

    override fun onSocketError(websocketService: WebsocketService, exception: IOException) {
        // Don't care
    }

    override fun onErrorMessage(message: String) {
        // Don't care
    }

    override fun clearCache() {
        lastWorkspaceFetched = null
    }

}