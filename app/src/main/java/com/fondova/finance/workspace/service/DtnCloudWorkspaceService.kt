package com.fondova.finance.workspace.service

import com.fondova.finance.AppExecutors
import com.fondova.finance.api.restful.StockCloudQuoteListRequest
import com.fondova.finance.workspace.Workspace
import com.fondova.finance.workspace.WorkspaceQuoteType
import com.fondova.finance.workspace.financex.StockCloudQuoteList


class StockCloudWorkspaceService(val appExecutors: AppExecutors): WorkspaceService {

    var username: String = ""
    var password: String = ""

    private var current: Workspace? = null

    override fun fetchWorkspaceList(callbackListener: OnWorkspaceListReceivedListener) {
        fetchWorkspaceDetails(null, object : OnWorkspaceDetailsReceivedListener {
            override fun onWorkspaceDataReceived(workspace: Workspace?, error: String?) {
                var list: MutableList<Workspace> = mutableListOf()
                if (workspace != null) {
                    list.add(workspace)
                }
                appExecutors.mainThread().execute {
                    callbackListener.onWorkspaceListReceived(list, error)
                }
            }

        })
    }

    override fun fetchDefaultWorkspace(callbackListener: OnDefaultWorkspaceReceivedListener) {
        fetchWorkspaceDetails(null, object : OnWorkspaceDetailsReceivedListener {
            override fun onWorkspaceDataReceived(workspace: Workspace?, error: String?) {
                var list: MutableList<Workspace> = mutableListOf()
                if (workspace != null) {
                    list.add(workspace)
                }
                appExecutors.mainThread().execute {
                    callbackListener.onWorkspaceDataReceived(list, workspace, error)
                }
            }

        })
    }

    override fun fetchWorkspaceDetails(workspaceId: String?, callbackListener: OnWorkspaceDetailsReceivedListener) {

        val request = StockCloudQuoteListRequest()
        request.username = username
        request.password = password
        request.successCallback = { response ->
            appExecutors.mainThread().execute {
                callbackListener.onWorkspaceDataReceived(response?.data, null)
            }
        }
        request.errorCallback = { error ->
            appExecutors.mainThread().execute {
                callbackListener.onWorkspaceDataReceived(null, "Fetching Workspace Details Failed - code: ${error?.code}")
            }
        }
        request.fetch()

    }

    override fun saveWorkspace(workspace: Workspace, callbackListener: EmptyResponseListener) {
        val quoteList = convertWorkspaceToStockCloudQuoteList(workspace)
        current = quoteList
        if (quoteList == null) {
            appExecutors.mainThread().execute {
                callbackListener.onResponse()
            }
            return
        }

        val request = StockCloudQuoteListRequest()
        request.username = username
        request.password = password
        request.quoteList = quoteList
        request.successCallback = { response ->
            appExecutors.mainThread().execute {
                callbackListener.onResponse()
            }
        }
        request.errorCallback = { error ->
            appExecutors.mainThread().execute {
                callbackListener.onResponse()
            }
        }
        request.fetch()

    }

    private fun convertWorkspaceToStockCloudQuoteList(workspace: Workspace): StockCloudQuoteList? {
        if (workspace is StockCloudQuoteList) {
            return workspace
        }
        var output = StockCloudQuoteList()

        for (group in workspace.getGroups()) {
            output.appendGroup(group.getDisplayName() ?: "Group")
            val groupIndex = output.getGroups().size - 1
            for (item in group.getListOfQuotes()) {
                output.appendQuote(item.getValue() ?: "", item.getDisplayName() ?: "", item.getType() ?: WorkspaceQuoteType.SYMBOL, groupIndex)
            }
        }

        return output
    }

    override fun setDefaultWorkspace(oldWorkspaceId: String?, newWorkspaceId: String?, callbackListener: EmptyResponseListener) {
        // Don't care
    }

    override fun currentWorkspace(): Workspace? {
        return current
    }

    override fun handleMessage(message: String): Boolean {
        return true
    }

    override fun clearCache() {
        current = null
    }

}

