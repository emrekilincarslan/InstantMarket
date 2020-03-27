package com.fondova.finance.persistance

import com.fondova.finance.enums.QuoteType
import com.fondova.finance.sync.QuoteSyncItem
import com.fondova.finance.vo.Quote
import com.fondova.finance.workspace.*

class QuoteListConverter {

    companion object {

        fun convertQuoteSyncItemListToWorkspace(quoteList: List<QuoteSyncItem>): Workspace {
            val quoteListWithEmptyGroup = insertEmptyGroupIfNeeded(quoteList)

            val workspaceId = "1"
            val workspaceName = "Workspace"

            val groups: MutableList<WorkspaceGroup> = mutableListOf()
            var workingGroupIndex = -1

            for (item in quoteListWithEmptyGroup) {
                if (item.type == QuoteType.LABEL) {
                    val newGroup = convertQuoteToWorkspaceGroup(item)
                    groups.add(newGroup)
                    workingGroupIndex += 1
                }
                if (item.type == QuoteType.SYMBOL) {
                    val quote = convertQuoteToWorkspaceQuote(item)
                    val listOfQuotes = groups[workingGroupIndex].getListOfQuotes()
                    listOfQuotes.add(quote)
                    groups[workingGroupIndex].setListOfQuotes(listOfQuotes)
                }
                if (item.type == QuoteType.EXPRESSION) {
                    val quote = convertQuoteToWorkspaceQuote(item)
                    val listOfQuotes = groups[workingGroupIndex].getListOfQuotes()
                    listOfQuotes.add(quote)
                    groups[workingGroupIndex].setListOfQuotes(listOfQuotes)
                }
            }
            val workspace = WorkspaceFactory().emptyWorkspace()
            workspace.setGroups(groups)

            workspace.setWorkspaceId(workspaceId)
            workspace.setName(workspaceName)
            workspace.setDefault(true)


            return workspace
        }

        fun quoteSyncItemFromQuote(quote: Quote): QuoteSyncItem {
            val item = QuoteSyncItem()
            item.displayName = quote.displayName
            item.type = quote.type
            item.requestName = quote.requestName
            return item
        }

        @Deprecated("use convertQuoteSyncItemListToWorkspace instead", ReplaceWith("convertQuoteSyncItemListToWorkspace", "List<QuoteSyncItem>"))
        fun convertQuoteListToWorkspace(quoteList: List<Quote>): Workspace {

            val legacyQuoteList: List<QuoteSyncItem> = quoteList.map {
                quoteSyncItemFromQuote(it)
            }
            return convertQuoteSyncItemListToWorkspace(legacyQuoteList)

        }

        fun convertQuoteToWorkspaceGroup(item: QuoteSyncItem): WorkspaceGroup {
            val newGroup = WorkspaceFactory().newWorkspaceGroup(item.displayName ?: item.requestName, mutableListOf())
            return newGroup
        }

        fun convertWorkspaceQuoteToQuote(workspaceQuote: WorkspaceQuote): QuoteSyncItem {
            val quote = QuoteSyncItem()
            if (workspaceQuote.getType()?.toLowerCase() == WorkspaceQuoteType.SYMBOL) {
                quote.type = QuoteType.SYMBOL
            } else {
                quote.type = QuoteType.EXPRESSION
            }
            quote.displayName = workspaceQuote.getDisplayName()
            quote.requestName = workspaceQuote.getValue()
            return quote
        }

        fun convertQuoteToWorkspaceQuote(item: QuoteSyncItem): WorkspaceQuote {
            val quote = WorkspaceFactory().newWorkspaceQuote(item.displayName ?: item.requestName,
                    WorkspaceQuoteType.SYMBOL,
                    item.requestName)

            if (item.type == QuoteType.EXPRESSION) {
                quote.setType(WorkspaceQuoteType.EXPRESSION)
            }
            return quote
        }

        fun convertWorkspaceIntoQuoteList(workspace: Workspace): List<QuoteSyncItem> {
            var list: MutableList<QuoteSyncItem> = mutableListOf()
            for (group in workspace.getGroups()) {
                val label = QuoteSyncItem()
                label.type = QuoteType.LABEL
                label.displayName = group.getDisplayName()
                label.requestName = group.getDisplayName()
                list.add(label)
                for (workspaceQuote in group.getListOfQuotes() ) {
                    val quote = convertWorkspaceQuoteToQuote(workspaceQuote)
                    list.add(quote)
                }
            }
            return list
        }

        fun convertWorkspaceIndexIntoQuote(groupIndex: Int, quoteIndex: Int?, quoteList: List<QuoteSyncItem>): QuoteSyncItem {
            val flatIndex = getFlatIndex(groupIndex, quoteIndex, quoteList)
            if (flatIndex == null) {
                return QuoteSyncItem()
            }

            return quoteList[flatIndex]
        }

        fun getFlatIndex(groupIndex: Int, quoteIndex: Int?, quoteList: List<QuoteSyncItem>): Int? {
            var currentGroupIndex = -1
            var currentFlatIndex = -1
            for (quote in quoteList) {
                currentFlatIndex += 1
                if (quote.type == QuoteType.LABEL) {
                    currentGroupIndex += 1
                }
                if (currentGroupIndex == groupIndex) {
                    if (quoteIndex == null) {
                        return currentFlatIndex
                    } else {
                        return currentFlatIndex + quoteIndex + 1
                    }

                }
            }

            return null
        }

        fun removeItemFromList(groupIndex: Int, quoteIndex: Int?, quoteList: List<QuoteSyncItem>): List<QuoteSyncItem> {
            val flatIndex = getFlatIndex(groupIndex, quoteIndex, quoteList)
            if (flatIndex == null) {
                return quoteList
            }
            val quoteListCopy = quoteList.toMutableList()
            quoteListCopy.removeAt(flatIndex)
            return quoteListCopy
        }

        fun insertEmptyGroupIfNeeded(list: List<QuoteSyncItem>): List<QuoteSyncItem> {
            if (!list.isEmpty() && list.first().type == QuoteType.LABEL) {
                return list
            }
            val mutableList = list.toMutableList()
            val label = createEmptyGroup()
            mutableList.add(0, label)
            return mutableList
        }

        private fun createEmptyGroup(): QuoteSyncItem {
            val label = QuoteSyncItem()
            label.type = QuoteType.LABEL
            label.displayName = "Unassigned"
            label.requestName = "Unassigned"
            return label
        }

    }
}