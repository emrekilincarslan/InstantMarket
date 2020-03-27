package com.fondova.finance.workspace.instantmarket

import com.fondova.finance.workspace.WorkspaceQuote

data class IMWorkspaceQuote(var map: MutableMap<String, Any>) : WorkspaceQuote {

    constructor(): this(mutableMapOf())

    constructor(displayName: String, type: String, value: String): this() {
        this.setDisplayName(displayName)
        this.setType(type)
        this.setValue(value)
    }

    override fun getDisplayName(): String? {
        return map.get("DisplayName") as? String
    }

    override fun setDisplayName(name: String) {
        map.set("DisplayName", name)
    }

    override fun getType(): String? {
        return map.get("Type") as? String
    }

    override fun setType(type: String) {
        map.set("Type", type)
    }

    override fun getValue(): String? {
        return map.get("Value") as? String
    }

    override fun setValue(value: String) {
        map.set("Value", value)
    }
}