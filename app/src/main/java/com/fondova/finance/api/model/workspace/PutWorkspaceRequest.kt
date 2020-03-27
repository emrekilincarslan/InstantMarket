package com.fondova.finance.api.model.workspace

import com.fondova.finance.api.model.base.WebsocketApiRequest
import com.fondova.finance.workspace.instantmarket.IMWorkspace

class PutWorkspaceRequest(workspace: IMWorkspace): WebsocketApiRequest("WebRequest") {
    val data: PutWorkspaceDetailsRequestData = PutWorkspaceDetailsRequestData(workspace)
}

class PutWorkspaceDetailsRequestData(workspace: IMWorkspace) {
    val name: String = "WorkspaceStorage"
    val parameters: PutWorkspaceDetailsRequestParams = PutWorkspaceDetailsRequestParams(workspace)
}

class PutWorkspaceDetailsRequestParams(workspace: IMWorkspace) {
    val route: String = "Workspace"
    val method: String = "put"
    val workspaceId: String? = workspace.getWorkspaceId()
    val workspace = workspace.map
}