package com.fondova.finance.workspace.instantmarket

import com.fondova.finance.workspace.WorkspaceGroup
import com.fondova.finance.workspace.WorkspaceQuote

data class IMWorkspaceGroup(var map: MutableMap<String, Any> = mutableMapOf()) : WorkspaceGroup {

    override fun getDisplayName(): String? {
        return map.get("DisplayName") as? String
    }

    override fun setDisplayName(name: String) {
        map.set("DisplayName", name)
    }

    override fun getListOfQuotes(): MutableList<WorkspaceQuote> {
        @Suppress("UNCHECKED_CAST")
        if (map.get("ListOfQuotes") as? MutableList<MutableMap<String, Any>> == null) {
            map.set("ListOfQuotes", mutableListOf<MutableMap<String, Any>>())
        }
        @Suppress("UNCHECKED_CAST")
        val list = map.get("ListOfQuotes") as? MutableList<MutableMap<String, Any>>
        return list?.map { IMWorkspaceQuote(it) as WorkspaceQuote }?.toMutableList() ?: mutableListOf()
    }

    override fun setListOfQuotes(quotes: MutableList<WorkspaceQuote>) {
        val dictionaryList: MutableList<MutableMap<String, Any>> = mutableListOf()
        for (quote in quotes) {
            if (quote as? IMWorkspaceQuote != null) {
                dictionaryList.add(quote.map)
            }
        }
        map.set("ListOfQuotes", dictionaryList)
    }
}