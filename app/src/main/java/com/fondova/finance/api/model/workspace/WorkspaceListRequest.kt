package com.fondova.finance.api.model.workspace

import com.fondova.finance.api.model.base.WebsocketApiRequest

class WorkspaceListRequest: WebsocketApiRequest("WebRequest") {

    var data: WorkspaceListRequestData = WorkspaceListRequestData()

}

class WorkspaceListRequestData {

    val name: String = "WorkspaceStorage"
    val parameters: WorkspaceListRequestParameters = WorkspaceListRequestParameters()

}

class WorkspaceListRequestParameters {

    val route: String = "WorkspaceNames"
    val method: String = "get"

}