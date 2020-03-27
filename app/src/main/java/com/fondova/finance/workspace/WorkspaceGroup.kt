package com.fondova.finance.workspace

interface WorkspaceGroup {
    fun getDisplayName(): String?
    fun setDisplayName(name: String)
    fun getListOfQuotes(): MutableList<WorkspaceQuote>
    fun setListOfQuotes(quotes: MutableList<WorkspaceQuote>)
}

