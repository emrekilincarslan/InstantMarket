package com.fondova.finance.workspace.financex

import com.google.gson.annotations.SerializedName
import com.fondova.finance.workspace.Workspace
import com.fondova.finance.workspace.WorkspaceGroup
import com.fondova.finance.workspace.WorkspaceQuote

class StockCloudQuoteListEnvelope {
    @SerializedName("data")
    var data: StockCloudQuoteList? = StockCloudQuoteList()
}

class StockCloudQuoteList: Workspace {
    override fun getWorkspaceId(): String? {
        return "DTN CLOUD QUOTE LIST"
    }

    override fun setWorkspaceId(workspaceId: String) {
        // Hard coded value in DTN cloud quote list
    }

    override fun getName(): String? {
        return "Quote List"
    }

    override fun setName(name: String) {
        // Hard coded value in DTN cloud quote list
    }

    override fun isDefault(): Boolean? {
        return true
    }

    override fun setDefault(default: Boolean) {
        // Hard coded value in DTN cloud quote list
    }

    override fun getGroups(): MutableList<WorkspaceGroup> {
        return itemList?.map { it as WorkspaceGroup }?.toMutableList() ?: mutableListOf()
    }

    override fun setGroups(groups: MutableList<WorkspaceGroup>) {
        var groupList: MutableList<StockCloudQuoteListGroup> = mutableListOf()
        for (group in groups) {
            var item = StockCloudQuoteListGroup()
            item.setDisplayName(group.getDisplayName() ?: "")
            var newItems: MutableList<StockCloudQuoteListQuote> = mutableListOf()
            for (quote in group.getListOfQuotes()) {
                val newItem = StockCloudQuoteListQuote()
                newItem.originalDisplayName = quote.getDisplayName()
                newItem.originalValue = quote.getValue()
                newItem.typeString = quote.getType()?.toLowerCase()
                newItems.add(newItem)
            }
            item.items = newItems
            groupList.add(item)
        }
        itemList = groupList
    }

    override fun getExpandedFields(): List<String> {
        return listOf(
                "UserDescription",
                "IssueDescription",
                "QuoteDelay",
                "PctChange",
                "ActualSymbol",
                "Last",
                "LastTicknum",
                "Change",
                "UserDescription",
                "IssueDescription",
                "PctChange",
                "High",
                "Low",
                "Open",
                "Bid",
                "Ask",
                "CumVolume",
                "TradeDateTime",
                "SettlementPrice",
                "Settledate",
                "QuoteDelay",
                "ActualSymbol"
        )

    }

    override fun insertGroup(title: String, index: Int) {
        val newGroup = StockCloudQuoteListGroup()
        newGroup.originalDisplayName = title
        newGroup.items = mutableListOf()
        itemList?.add(index, newGroup)
    }

    override fun appendGroup(title: String) {
        val newGroup = StockCloudQuoteListGroup()
        newGroup.originalDisplayName = title
        newGroup.items = mutableListOf()
        itemList?.add(newGroup)
    }

    override fun insertQuote(value: String, displayName: String, type: String, group: Int, index: Int) {
        if (getGroups().isEmpty()) {
            insertGroup("UNASSIGNED", 0)
        }

        val newQuote = StockCloudQuoteListQuote()
        newQuote.originalValue = value
        newQuote.originalDisplayName = displayName
        newQuote.typeString = type
        itemList?.get(group)?.items?.add(index, newQuote)
    }

    override fun appendQuote(value: String, displayName: String, type: String, group: Int) {
        if (getGroups().isEmpty()) {
            insertGroup("UNASSIGNED", 0)
        }
        val newQuote = StockCloudQuoteListQuote()
        newQuote.originalValue = value
        newQuote.originalDisplayName = displayName
        newQuote.typeString = type
        itemList?.get(group)?.items?.add(newQuote)

    }

    override fun deleteSymbol(groupIndex: Int, index: Int) {
        itemList?.get(groupIndex)?.items?.removeAt(index)
    }

    override fun deleteGroup(index: Int) {
        itemList?.removeAt(index)
    }

    @SerializedName("groups")
    var itemList: MutableList<StockCloudQuoteListGroup>? = mutableListOf()
    @SerializedName("view")
    var view: String? = null

}

class StockCloudQuoteListGroup: WorkspaceGroup {

    @SerializedName("displayName")
    var originalDisplayName: String? = null
    @SerializedName("expanded")
    var expanded: Boolean? = null
    @SerializedName("show")
    var show: Boolean? = null
    @SerializedName("items")
    var items: MutableList<StockCloudQuoteListQuote>? = mutableListOf()

    override fun getDisplayName(): String? {
        return originalDisplayName
    }

    override fun setDisplayName(name: String) {
        originalDisplayName = name
    }

    override fun getListOfQuotes(): MutableList<WorkspaceQuote> {
        return items?.map { it as WorkspaceQuote }?.toMutableList() ?: mutableListOf()
    }

    override fun setListOfQuotes(quotes: MutableList<WorkspaceQuote>) {
        var quoteList: MutableList<StockCloudQuoteListQuote> = mutableListOf()
        for (quote in quotes) {
            val newItem = StockCloudQuoteListQuote()
            newItem.typeString = quote.getType()?.toLowerCase()
            newItem.originalValue = quote.getValue()
            newItem.originalDisplayName = quote.getDisplayName()
            quoteList.add(newItem)
        }
        items = quoteList
    }

}

class StockCloudQuoteListQuote: WorkspaceQuote {
    @SerializedName("displayName")
    var originalDisplayName: String? = null
    @SerializedName("expanded")
    var expanded: Boolean? = null
    @SerializedName("market")
    var market: Int? = null
    @SerializedName("type")
    var typeString: String? = null
    @SerializedName("value")
    var originalValue: String? = null
    @SerializedName("vendor")
    var vendor: String? = null

    override fun getDisplayName(): String? {
        return originalDisplayName
    }

    override fun setDisplayName(name: String) {
        originalDisplayName = name
    }

    override fun getType(): String? {
        return typeString
    }

    override fun setType(type: String) {
        typeString = type
    }

    override fun getValue(): String? {
        return originalValue
    }

    override fun setValue(value: String) {
        originalValue = value
    }

}
