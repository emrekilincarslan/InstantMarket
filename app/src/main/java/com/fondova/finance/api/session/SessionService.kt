package com.fondova.finance.api.session

import android.arch.lifecycle.LiveData
import android.content.Context
import com.fondova.finance.api.auth.AuthService
import com.fondova.finance.workspace.Workspace

interface DidSelectWorkspaceListener {
    fun didSelectWorkspace(workspace: Workspace)
}

interface WorkspaceSelector {
    fun selectWorkspace(legacy: Workspace?, current: Workspace?, listener: DidSelectWorkspaceListener)
}

interface SessionService: AuthService {

    fun getSessionStatusLiveData(): LiveData<SessionStatus>
    fun logout(userInitiated: Boolean)
    fun appWillEnterForeground()
    fun appWillEnterBackground()
    fun incrementActivityCount()
    fun decrementActivityCount()
    fun isAppVisible(): Boolean
    fun setWorkspaceSelector(context: Context, workspaceSelector: WorkspaceSelector)
    fun clearCache()
}