package com.fondova.finance.api.model.workspace

import com.fondova.finance.api.model.base.WebsocketApiResponse
import com.fondova.finance.workspace.instantmarket.IMWorkspace

class WorkspaceListResponse: WebsocketApiResponse() {

    var data: List<WorkspaceListResponseData>? = null

}

class WorkspaceListResponseData {

    var data: List<MutableMap<String, Any>>? = null

    fun getWorkspaces(): List<IMWorkspace>? {
        return data?.map { IMWorkspace(it) }
    }

    fun setWorkspaces(workspaces: List<IMWorkspace>) {
        data = workspaces.map { it.map }
    }

}

