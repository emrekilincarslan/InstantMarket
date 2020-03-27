package com.fondova.finance.workspace

interface WorkspaceQuote {
    fun getDisplayName(): String?
    fun setDisplayName(name: String)
    fun getType(): String?
    fun setType(type: String)
    fun getValue(): String?
    fun setValue(value: String)
}
