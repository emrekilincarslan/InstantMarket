package com.fondova.finance.workspace.service

import android.content.Context
import com.google.gson.Gson
import com.fondova.finance.AppExecutors
import com.fondova.finance.db.QuoteDao
import com.fondova.finance.enums.QuoteType
import com.fondova.finance.repo.DefaultSymbolsRepository
import com.fondova.finance.sync.QuoteSyncItem
import com.fondova.finance.vo.Quote
import com.fondova.finance.workspace.Workspace
import com.fondova.finance.workspace.WorkspaceQuoteType
import com.fondova.finance.workspace.financex.StockCloudQuoteList

class GoogleWorkspaceService(val context: Context, val appExecutors: AppExecutors = AppExecutors()): WorkspaceService {

    private var current: Workspace? = null
    private var googleService: GoogleDriveService = GoogleDriveService.shared

    override fun fetchWorkspaceList(callbackListener: OnWorkspaceListReceivedListener) {
        fetchWorkspaceDetails("", object : OnWorkspaceDetailsReceivedListener {
            override fun onWorkspaceDataReceived(workspace: Workspace?, error: String?) {
                var list: MutableList<Workspace> = mutableListOf()
                if (workspace != null) {
                    list.add(workspace)
                }
                callbackListener.onWorkspaceListReceived(list, error)
            }
        })
    }

    override fun fetchDefaultWorkspace(callbackListener: OnDefaultWorkspaceReceivedListener) {
        fetchWorkspaceDetails("", object : OnWorkspaceDetailsReceivedListener {
            override fun onWorkspaceDataReceived(workspace: Workspace?, error: String?) {
                var list: MutableList<Workspace> = mutableListOf()
                if (workspace != null) {
                    list.add(workspace)
                }
                callbackListener.onWorkspaceDataReceived(list, workspace, error)
            }
        })
    }

    override fun fetchWorkspaceDetails(workspaceId: String?, callbackListener: OnWorkspaceDetailsReceivedListener) {
        googleService.connectToGoogleClient(context) { apiClient, connected ->
            appExecutors.syncThread().execute {
                if (!connected) {
                    appExecutors.mainThread().execute {
                        callbackListener.onWorkspaceDataReceived(null, "Not connected to network")
                    }
                    return@execute
                }
                if (apiClient == null) {
                    appExecutors.mainThread().execute {
                        callbackListener.onWorkspaceDataReceived(null, "Unable to connecto to Google")
                    }
                    return@execute
                }
                var dataString = googleService.readFromDrive(apiClient)
                apiClient.disconnect()
                if (dataString == null) {
                    appExecutors.mainThread().execute {
                        callbackListener.onWorkspaceDataReceived(null, null)
                    }
                }
                val data: GoogleDriveData = Gson().fromJson(dataString, GoogleDriveData::class.java)
                val quoteList = data.quotes
                if (quoteList == null || quoteList.isEmpty()) {
                    appExecutors.mainThread().execute {
                        callbackListener.onWorkspaceDataReceived(null, null)
                    }
                    return@execute
                }
                val workspace = StockCloudQuoteList()
                if (quoteList.count() == 0 || quoteList[0].type  != QuoteType.LABEL) {
                    workspace.appendGroup("UNASSIGNED")
                }
                for (item in quoteList) {
                    if (item.type == QuoteType.LABEL) {
                        workspace.appendGroup(item.displayName ?: "")
                    } else {
                        workspace.appendQuote(stripSingleQuotes(item.requestName), item.displayName ?: "", item.getType()?.toLowerCase() ?: WorkspaceQuoteType.EXPRESSION, workspace.getGroups().size - 1)
                    }
                }
                current = workspace
                appExecutors.mainThread().execute {
                    callbackListener.onWorkspaceDataReceived(workspace, null)
                }
            }

        }
    }

    private fun stripSingleQuotes(string: String?): String {
        if (string == null) {
            return ""
        }

        return string.removeSurrounding("'")
    }

    override fun saveWorkspace(workspace: Workspace, callbackListener: EmptyResponseListener) {
        // We no longer save to Google
    }

    override fun setDefaultWorkspace(oldWorkspaceId: String?, newWorkspaceId: String?, callbackListener: EmptyResponseListener) {
        // We no longer save to Google
    }

    override fun currentWorkspace(): Workspace? {
        return current
    }

    override fun handleMessage(message: String): Boolean {
        // Don't care
        return true
    }

    override fun clearCache() {
        current = null
    }

}

class LegacyWorkspaceService(val legacyQuoteDao: QuoteDao, val defaultSymbolsRepository: DefaultSymbolsRepository, val appExecutors: AppExecutors = AppExecutors()): WorkspaceService {

    private var current: Workspace? = null

    override fun fetchWorkspaceList(callbackListener: OnWorkspaceListReceivedListener) {
        fetchWorkspaceDetails("", object : OnWorkspaceDetailsReceivedListener {
            override fun onWorkspaceDataReceived(workspace: Workspace?, error: String?) {
                var list: MutableList<Workspace> = mutableListOf()
                if (workspace != null) {
                    list.add(workspace)
                }
                callbackListener.onWorkspaceListReceived(list, error)
            }
        })
    }

    override fun fetchDefaultWorkspace(callbackListener: OnDefaultWorkspaceReceivedListener) {
        fetchWorkspaceDetails("", object : OnWorkspaceDetailsReceivedListener {
            override fun onWorkspaceDataReceived(workspace: Workspace?, error: String?) {
                var list: MutableList<Workspace> = mutableListOf()
                if (workspace != null) {
                    list.add(workspace)
                }
                callbackListener.onWorkspaceDataReceived(list, workspace, error)
            }
        })
    }

    override fun fetchWorkspaceDetails(workspaceId: String?, callbackListener: OnWorkspaceDetailsReceivedListener) {
            appExecutors.syncThread().execute {
                val dataString = getLegacyDataString()

                if (dataString == null) {
                    appExecutors.mainThread().execute {
                        callbackListener.onWorkspaceDataReceived(null, null)
                    }
                }
                val data: GoogleDriveData = Gson().fromJson(dataString, GoogleDriveData::class.java)
                val quoteList = data.quotes
                if (quoteList == null || quoteList.isEmpty()) {
                    appExecutors.mainThread().execute {
                        callbackListener.onWorkspaceDataReceived(defaultSymbolsRepository.createDefaultWorkspace(), null)
                    }
                    return@execute
                }
                val workspace = StockCloudQuoteList()
                if (quoteList.count() == 0 || quoteList[0].type  != QuoteType.LABEL) {
                    workspace.appendGroup("UNASSIGNED")
                }
                for (item in quoteList) {
                    if (item.type == QuoteType.LABEL) {
                        workspace.appendGroup(item.displayName ?: "")
                    } else {
                        workspace.appendQuote(stripSingleQuotes(item.requestName), item.displayName ?: "", item.getType()?.toLowerCase() ?: WorkspaceQuoteType.EXPRESSION, workspace.getGroups().size - 1)
                    }
                }
                current = workspace
                appExecutors.mainThread().execute {
                    callbackListener.onWorkspaceDataReceived(workspace, null)
                }
            }

    }

    private fun stripSingleQuotes(string: String?): String {
        if (string == null) {
            return ""
        }

        return string.removeSurrounding("'")
    }

    private fun getLegacyDataString(): String? {
        val legacyList = legacyQuoteDao.quotes.map { convertToQuoteSyncItem(it) }
        val data = GoogleDriveData()
        data.quotes = legacyList
        return Gson().toJson(data)

    }

    private fun convertToQuoteSyncItem(quote: Quote): QuoteSyncItem {
        var item = QuoteSyncItem()
        item.displayName = quote.displayName
        item.requestName = quote.requestName
        item.type = quote.type
        return item
    }

    override fun saveWorkspace(workspace: Workspace, callbackListener: EmptyResponseListener) {
        // We no longer save to Google
    }

    override fun setDefaultWorkspace(oldWorkspaceId: String?, newWorkspaceId: String?, callbackListener: EmptyResponseListener) {
        // We no longer save to Google
    }

    override fun currentWorkspace(): Workspace? {
        return current
    }

    override fun handleMessage(message: String): Boolean {
        // Don't care
        return true
    }

    override fun clearCache() {
        current = null
    }
}

