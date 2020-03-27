package com.fondova.finance.workspace.service

import com.fondova.finance.api.socket.WebsocketResponseHandler
import com.fondova.finance.workspace.Workspace

interface OnWorkspaceDetailsReceivedListener {
    fun onWorkspaceDataReceived(workspace: Workspace?, error: String?)
}

interface OnDefaultWorkspaceReceivedListener {
    fun onWorkspaceDataReceived(workspaces: List<Workspace>, default: Workspace?, error: String?)
}

interface OnWorkspaceListReceivedListener {
    fun onWorkspaceListReceived(workspaces: List<Workspace>, error: String?)
}

interface EmptyResponseListener {
    fun onResponse()
}

interface WorkspaceService: WebsocketResponseHandler {

    fun fetchWorkspaceList(callbackListener: OnWorkspaceListReceivedListener)
    fun fetchDefaultWorkspace(callbackListener: OnDefaultWorkspaceReceivedListener)
    fun fetchWorkspaceDetails(workspaceId: String?, callbackListener: OnWorkspaceDetailsReceivedListener)
    fun saveWorkspace(workspace: Workspace, callbackListener: EmptyResponseListener)
    fun setDefaultWorkspace(oldWorkspaceId: String?, newWorkspaceId: String?, callbackListener: EmptyResponseListener)
    fun currentWorkspace(): Workspace?
    fun clearCache()
}