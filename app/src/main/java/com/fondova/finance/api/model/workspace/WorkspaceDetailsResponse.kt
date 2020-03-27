package com.fondova.finance.api.model.workspace

import com.fondova.finance.api.model.base.WebsocketApiResponse
import com.fondova.finance.workspace.instantmarket.IMWorkspace

class WorkspaceDetailsResponse: WebsocketApiResponse() {

    var data: MutableList<WorkspaceDetailsResponseData>? = null

}

class WorkspaceDetailsResponseData {


    var data: MutableMap<String, Any>? = null

    fun getWorkspace(): IMWorkspace {
        return IMWorkspace(data ?: mutableMapOf())
    }

    fun setWorksapce(workspace: IMWorkspace) {
        data = workspace.map
    }

}
