package com.fondova.finance.api.model.workspace

import com.fondova.finance.api.model.base.WebsocketApiRequest

class SetDefaultWorkspaceRequest(oldWorkspaceId: String, newWorkspaceId: String?): WebsocketApiRequest("WebRequest") {

    val data: SetDefaultWorksapaceRequestData = SetDefaultWorksapaceRequestData(oldWorkspaceId, newWorkspaceId)

}

class SetDefaultWorkspaceRequestParams(old: String?, new: String?) {

    val route: String = "Workspace"
    val method: String = "patch"
    val oldDefaultWorkspaceId: String? = old
    val newDefaultWorkspaceId: String? = new

}

class SetDefaultWorksapaceRequestData(oldWorkspaceId: String?, newWorkspaceId: String?) {

    val name: String = "WorkspaceStorage"
    val parameters: SetDefaultWorkspaceRequestParams = SetDefaultWorkspaceRequestParams(oldWorkspaceId, newWorkspaceId)

}
