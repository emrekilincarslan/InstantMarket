package com.fondova.finance.workspace.instantmarket

import com.fondova.finance.workspace.WorkspaceGroup

data class WorkspaceSettings(val map: MutableMap<String, Any>) {

    constructor(): this(mutableMapOf())

    fun getGroups(): MutableList<WorkspaceGroup> {
        @Suppress("UNCHECKED_CAST")
        val groupsMap = map.get("Groups") as? MutableList<MutableMap<String, Any>>
        val groups = groupsMap?.map { IMWorkspaceGroup(it) as WorkspaceGroup }?.toMutableList() ?: mutableListOf()
        return groups
    }

    fun setGroups(groups: MutableList<WorkspaceGroup>) {
        val dictionaryList: MutableList<MutableMap<String, Any>> = mutableListOf()
        for (group in groups) {
            if (group as? IMWorkspaceGroup != null) {
                dictionaryList.add(group.map)
            }
        }
        map.set("Groups", dictionaryList)
    }


    fun getExpandedFields(): MutableList<String> {
        @Suppress("UNCHECKED_CAST")
        return map.get("ExpandedFields") as? MutableList<String> ?: mutableListOf()
    }

    fun setExpandedFields(fields: MutableList<String>) {
        map.set("ExpandedFields", fields)
    }

}
