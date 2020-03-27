package com.fondova.finance.workspace.instantmarket

import com.fondova.finance.workspace.Workspace
import com.fondova.finance.workspace.WorkspaceGroup

data class IMWorkspace(var map: MutableMap<String, Any> = mutableMapOf()): Workspace {
    override fun insertGroup(title: String, index: Int) {
        val groups = getGroups()
        val newGroup = IMWorkspaceGroup()
        newGroup.setDisplayName(title)
        groups.add(index, newGroup)
        setGroups(groups)
    }

    override fun appendGroup(title: String) {
        val groups = getGroups()
        val newGroup = IMWorkspaceGroup()
        newGroup.setDisplayName(title)
        groups.add(newGroup)
        setGroups(groups)
    }

    override fun insertQuote(value: String, displayName: String, type: String, group: Int, index: Int) {
        if (getGroups().isEmpty()) {
            insertGroup("UNASSIGNED", 0)
        }
        val groups = getGroups()
        val quotes = groups[group].getListOfQuotes()
        val newQuote = IMWorkspaceQuote()
        newQuote.setValue(value)
        newQuote.setDisplayName(displayName)
        newQuote.setType(type)
        quotes.add(index, newQuote)
        groups[group].setListOfQuotes(quotes)
        setGroups(groups)
    }

    override fun appendQuote(value: String, displayName: String, type: String, group: Int) {
        if (getGroups().isEmpty()) {
            insertGroup("UNASSIGNED", 0)
        }
        val groups = getGroups()
        val quotes = groups[group].getListOfQuotes()
        val newQuote = IMWorkspaceQuote()
        newQuote.setValue(value)
        newQuote.setDisplayName(displayName)
        newQuote.setType(type)
        quotes.add(newQuote)
        groups[group].setListOfQuotes(quotes)
        setGroups(groups)
    }

    override fun deleteSymbol(groupIndex: Int, index: Int) {
        val groups = getGroups()
        val quotes = groups[groupIndex].getListOfQuotes()
        quotes.removeAt(index)
        groups[groupIndex].setListOfQuotes(quotes)
        setGroups(groups)
    }

    override fun deleteGroup(index: Int) {
        val groups = getGroups()
        groups.removeAt(index)
        setGroups(groups)
    }

    override fun getWorkspaceId(): String? {
        return map.get("WorkspaceId") as? String
    }

    override fun setWorkspaceId(workspaceId: String) {
        map.set("WorkspaceId",  workspaceId)
    }

    override fun getName(): String? {
        return map.get("Name") as? String
    }

    override fun setName(name: String) {
        map.set("Name", name)
    }

    override fun isDefault(): Boolean? {
        return map.get("IsDefault") as? Boolean
    }

    override fun setDefault(default: Boolean) {
        map.set("IsDefault", default)
    }

    override fun getGroups(): MutableList<WorkspaceGroup> {
        return getSettings()?.getGroups() ?: mutableListOf()
    }

    override fun setGroups(groups: MutableList<WorkspaceGroup>) {
        getSettings()?.setGroups(groups)
    }

    override fun getExpandedFields(): List<String> {
        return getSettings()?.getExpandedFields() ?: emptyList()
    }

    private fun getSettings(): WorkspaceSettings? {
        @Suppress("UNCHECKED_CAST")
        if (map.get("Settings") as? MutableMap<String, Any> == null) {
            map.set("Settings", mutableMapOf<String, Any>())
        }
        @Suppress("UNCHECKED_CAST")
        val settingsMap = map.get("Settings") as? MutableMap<String, Any> ?: mutableMapOf()
        return WorkspaceSettings(settingsMap)
    }

    fun _setSettings(settings: WorkspaceSettings) {
        map.set("Settings", settings.map)
    }

}