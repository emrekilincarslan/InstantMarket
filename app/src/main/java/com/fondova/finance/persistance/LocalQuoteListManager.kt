package com.fondova.finance.persistance

import com.fondova.finance.workspace.Workspace
import com.fondova.finance.workspace.WorkspaceFactory
import com.fondova.finance.workspace.WorkspaceQuote
import javax.inject.Inject

class LocalQuoteListManager @Inject constructor(val appStorage: AppStorage) {

    fun insertGroup(title: String, index: Int) {
        var workspace = appStorage.getWorkspace()
        workspace.insertGroup(title, index)
        appStorage.updateAndSaveWorkspace(workspace)
    }

    fun insertQuote(value: String, displayName: String, type: String, group: Int, index: Int) {
        var workspace = appStorage.getWorkspace()
        workspace.insertQuote(value, displayName, type, group, index)
        appStorage.updateAndSaveWorkspace(workspace)
    }

    fun setWorkspace(workspace: Workspace) {
        appStorage.updateAndSaveWorkspace(workspace)
    }

    fun getAllSymbols(): List<WorkspaceQuote> {
        val workspaceQuoteList: MutableList<WorkspaceQuote> = mutableListOf()
        for (group in getWorkspace().getGroups()) {
            workspaceQuoteList.addAll(group.getListOfQuotes())
        }
        return workspaceQuoteList
    }

    fun getQuoteCount(): Int {
        return QuoteListConverter.convertWorkspaceIntoQuoteList(getWorkspace()).size
    }

    fun deleteSymbol(groupIndex: Int, quoteIndex: Int) {

        val workspace = appStorage.getWorkspace()
        workspace.deleteSymbol(groupIndex, quoteIndex)
        appStorage.updateAndSaveWorkspace(workspace)

    }

    fun deleteGroup(index: Int) {
        val workspace = appStorage.getWorkspace()
        workspace.deleteGroup(index)

        appStorage.updateAndSaveWorkspace(workspace)
    }

    fun getWorkspace(): Workspace {
        return appStorage.getWorkspace()
    }

    fun deleteAllQuotes() {
        appStorage.setWorkspace(WorkspaceFactory().emptyWorkspace())
    }


}