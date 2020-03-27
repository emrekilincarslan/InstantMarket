package com.fondova.finance.workspace

import com.fondova.finance.workspace.instantmarket.IMWorkspace
import com.fondova.finance.workspace.instantmarket.IMWorkspaceGroup
import com.fondova.finance.workspace.instantmarket.IMWorkspaceQuote
import com.fondova.finance.workspace.instantmarket.WorkspaceSettings

interface Workspace {
    fun getWorkspaceId(): String?
    fun setWorkspaceId(workspaceId: String)
    fun getName(): String?
    fun setName(name: String)
    fun isDefault(): Boolean?
    fun setDefault(default: Boolean)
    fun getGroups(): MutableList<WorkspaceGroup>
    fun setGroups(groups: MutableList<WorkspaceGroup>)
    fun getExpandedFields(): List<String>
    fun insertGroup(title: String, index: Int)
    fun appendGroup(title: String)
    fun insertQuote(value: String, displayName: String, type: String, group: Int, index: Int)
    fun appendQuote(value: String, displayName: String, type: String, group: Int)
    fun deleteSymbol(groupIndex: Int, index: Int)
    fun deleteGroup(index: Int)

}

class WorkspaceFactory {

    fun newWorkspaceGroup(name: String, quotes: MutableList<WorkspaceQuote>): WorkspaceGroup {
        val newGroup = IMWorkspaceGroup()
        newGroup.setDisplayName(name)
        newGroup.setListOfQuotes(quotes)
        return newGroup
    }

    fun newWorkspaceQuote(displayName: String, type: String, value: String): WorkspaceQuote {
        val newQuote = IMWorkspaceQuote()
        newQuote.setDisplayName(displayName)
        newQuote.setType(type)
        newQuote.setValue(value)
        return newQuote
    }

    fun emptyQuote(): WorkspaceQuote {
        val newQuote = IMWorkspaceQuote()
        newQuote.setDisplayName("--")
        newQuote.setType(WorkspaceQuoteType.SYMBOL)
        newQuote.setValue("")
        return newQuote
    }

    fun emptyWorkspace(): Workspace {
        val workspace = IMWorkspace()
        workspace._setSettings(WorkspaceSettings(mutableMapOf()))
        return  workspace
    }

    fun copyQuote(quote: WorkspaceQuote): WorkspaceQuote {
        return newWorkspaceQuote(quote.getDisplayName() ?: "",
                quote.getType()?.toLowerCase() ?: WorkspaceQuoteType.SYMBOL,
                quote.getValue() ?: "")
    }
}

