package com.fondova.finance.api.model.workspace

import com.fondova.finance.api.model.base.WebsocketApiRequest

class WorkspaceDetailsRequest(workspaceId: String?): WebsocketApiRequest("WebRequest") {
    val data: WorkspaceDetailsRequestData = WorkspaceDetailsRequestData(workspaceId)
}

class WorkspaceDetailsRequestParams(workspaceId: String?) {
    val route: String = "Workspace"
    val method: String = "get"
    val workspaceId: String? = workspaceId
}

class WorkspaceDetailsRequestData(workspaceId: String?) {
    val name: String = "WorkspaceStorage"
    val parameters: WorkspaceDetailsRequestParams = WorkspaceDetailsRequestParams(workspaceId)
}

