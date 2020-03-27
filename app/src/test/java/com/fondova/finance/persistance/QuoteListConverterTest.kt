package com.fondova.finance.persistance

import com.fondova.finance.enums.QuoteType
import com.fondova.finance.workspace.instantmarket.IMWorkspace
import com.fondova.finance.workspace.instantmarket.IMWorkspaceGroup
import com.fondova.finance.workspace.instantmarket.IMWorkspaceQuote
import com.fondova.finance.sync.QuoteSyncItem
import org.junit.Assert.assertEquals
import org.junit.Test

class QuoteListConverterTest {

    @Test
    fun convertFlatQuoteListIntoWorkspace() {
        val label1 = QuoteSyncItem()
        label1.displayName = "Label 1"
        label1.type = QuoteType.LABEL
        label1.requestName = "Label 1 value"

        val quote1 = QuoteSyncItem()
        quote1.displayName = "Symbol 1"
        quote1.type = QuoteType.SYMBOL
        quote1.requestName = "Symbol 1 value"

        val quote2 = QuoteSyncItem()
        quote2.displayName = "Symbol 2"
        quote2.type = QuoteType.SYMBOL
        quote2.requestName = "Symbol 2 value"

        val label2 = QuoteSyncItem()
        label2.displayName = "Label 2"
        label2.type = QuoteType.LABEL
        label2.requestName = "Label 2 value"

        val expression1 = QuoteSyncItem()
        expression1.displayName = "Expression 1"
        expression1.type = QuoteType.EXPRESSION
        expression1.requestName = "Expression 1 value"

        val expression2 = QuoteSyncItem()
        expression2.displayName = "Expression 2"
        expression2.type = QuoteType.EXPRESSION
        expression2.requestName = "Expression 2 value"

        val testQuoteList = listOf(label1, quote1, quote2, label2, expression1, expression2)

        val workspace = QuoteListConverter.convertQuoteSyncItemListToWorkspace(testQuoteList)

        assertEquals(2, workspace.getGroups().size)
        assertEquals("Label 1", workspace.getGroups().first().getDisplayName())
        assertEquals(2, workspace.getGroups().first().getListOfQuotes().size)
        assertEquals("Symbol 1", workspace.getGroups().first().getListOfQuotes().first().getDisplayName())
        assertEquals("Symbol 1 value", workspace.getGroups().first().getListOfQuotes().first().getValue())
        assertEquals("Symbol 2", workspace.getGroups().first().getListOfQuotes().last().getDisplayName())
        assertEquals("Symbol 2 value", workspace.getGroups().first().getListOfQuotes().last().getValue())
        assertEquals("Label 2", workspace.getGroups().last().getDisplayName())
        assertEquals(2, workspace.getGroups().last().getListOfQuotes().size)
        assertEquals("Expression 1", workspace.getGroups().last().getListOfQuotes().first().getDisplayName())
        assertEquals("Expression 1 value", workspace.getGroups().last().getListOfQuotes().first().getValue())
        assertEquals("Expression 2", workspace.getGroups().last().getListOfQuotes().last().getDisplayName())
        assertEquals("Expression 2 value", workspace.getGroups().last().getListOfQuotes().last().getValue())
        assertEquals("1", workspace.getWorkspaceId())

    }

    @Test
    fun convertWorkspaceIntoFlatQuote() {
        val symbol1 = IMWorkspaceQuote("Symbol 1", "Symbol", "Symbol 1 Value")
        val symbol2 = IMWorkspaceQuote("Symbol 2", "Symbol", "Symbol 2 Value")
        val group1 = IMWorkspaceGroup()//"Group 1", mutableListOf(symbol1, symbol2))
        group1.setDisplayName("Group 1")
        group1.setListOfQuotes(mutableListOf(symbol1, symbol2))

        val expression1 = IMWorkspaceQuote("Expression 1", "Expression", "Expression 1 Value")
        val expression2 = IMWorkspaceQuote("Expression 2", "Expression", "Expression 2 Value")
        val group2 = IMWorkspaceGroup()//"Group 2", mutableListOf(expression1, expression2))
        group2.setDisplayName("Group 2")
        group2.setListOfQuotes(mutableListOf(expression1, expression2))

        val workspace = IMWorkspace()//"Workspace", "Workspace", true, WorkspaceSettings(mutableListOf(group1, group2), mutableListOf()))
        workspace.setWorkspaceId("Workspace")
        workspace.setName("Workspace Name")
        workspace.setDefault(true)
        workspace.setGroups(mutableListOf(group1, group2))


        val quoteList = QuoteListConverter.convertWorkspaceIntoQuoteList(workspace)

        assertEquals(6, quoteList.size)
        assertEquals("Group 1", quoteList[0].displayName)
        assertEquals("Group 1", quoteList[0].requestName)
        assertEquals(QuoteType.LABEL, quoteList[0].type)

        assertEquals("Symbol 1", quoteList[1].displayName)
        assertEquals("Symbol 1 Value", quoteList[1].requestName)
        assertEquals(QuoteType.SYMBOL, quoteList[1].type)

        assertEquals("Symbol 2", quoteList[2].displayName)
        assertEquals("Symbol 2 Value", quoteList[2].requestName)
        assertEquals(QuoteType.SYMBOL, quoteList[2].type)

        assertEquals("Group 2", quoteList[3].displayName)
        assertEquals("Group 2", quoteList[3].requestName)
        assertEquals(QuoteType.LABEL, quoteList[3].type)

        assertEquals("Expression 1", quoteList[4].displayName)
        assertEquals("Expression 1 Value", quoteList[4].requestName)
        assertEquals(QuoteType.EXPRESSION, quoteList[4].type)

        assertEquals("Expression 2", quoteList[5].displayName)
        assertEquals("Expression 2 Value", quoteList[5].requestName)
        assertEquals(QuoteType.EXPRESSION, quoteList[5].type)
    }

    @Test
    fun convertWorkspaceIndexIntoQuote() {
        val label1 = QuoteSyncItem()
        label1.displayName = "Label 1"
        label1.type = QuoteType.LABEL

        val quote1 = QuoteSyncItem()
        quote1.displayName = "Symbol 1"
        quote1.type = QuoteType.SYMBOL

        val quote2 = QuoteSyncItem()
        quote2.displayName = "Symbol 2"
        quote2.type = QuoteType.SYMBOL

        val label2 = QuoteSyncItem()
        label2.displayName = "Label 2"
        label2.type = QuoteType.LABEL

        val expression1 = QuoteSyncItem()
        expression1.displayName = "Expression 1"
        expression1.type = QuoteType.EXPRESSION

        val expression2 = QuoteSyncItem()
        expression2.displayName = "Expression 2"
        expression2.type = QuoteType.EXPRESSION

        val testQuoteList = listOf(label1, quote1, quote2, label2, expression1, expression2)

        assertEquals("Label 1", QuoteListConverter.convertWorkspaceIndexIntoQuote(0, null, testQuoteList).displayName)
        assertEquals("Symbol 1", QuoteListConverter.convertWorkspaceIndexIntoQuote(0, 0, testQuoteList).displayName)
        assertEquals("Symbol 2", QuoteListConverter.convertWorkspaceIndexIntoQuote(0, 1, testQuoteList).displayName)
        assertEquals("Label 2", QuoteListConverter.convertWorkspaceIndexIntoQuote(1, null, testQuoteList).displayName)
        assertEquals("Expression 1", QuoteListConverter.convertWorkspaceIndexIntoQuote(1, 0, testQuoteList).displayName)
        assertEquals("Expression 2", QuoteListConverter.convertWorkspaceIndexIntoQuote(1, 1, testQuoteList).displayName)
    }

    @Test
    fun removeItemAtIndex() {
        val label1 = QuoteSyncItem()
        label1.displayName = "Label 1"
        label1.type = QuoteType.LABEL

        val quote1 = QuoteSyncItem()
        quote1.displayName = "Symbol 1"
        quote1.type = QuoteType.SYMBOL

        val quote2 = QuoteSyncItem()
        quote2.displayName = "Symbol 2"
        quote2.type = QuoteType.SYMBOL

        val label2 = QuoteSyncItem()
        label2.displayName = "Label 2"
        label2.type = QuoteType.LABEL

        val expression1 = QuoteSyncItem()
        expression1.displayName = "Expression 1"
        expression1.type = QuoteType.EXPRESSION

        val expression2 = QuoteSyncItem()
        expression2.displayName = "Expression 2"
        expression2.type = QuoteType.EXPRESSION

        val testQuoteList = listOf(label1, quote1, quote2, label2, expression1, expression2)

        val testResult = QuoteListConverter.removeItemFromList(0, 0, testQuoteList)

        assertEquals(5, testResult.size)
        assertEquals("Label 1",      testResult[0].displayName)
        assertEquals("Symbol 2",     testResult[1].displayName)
        assertEquals("Label 2",      testResult[2].displayName)
        assertEquals("Expression 1", testResult[3].displayName)
        assertEquals("Expression 2", testResult[4].displayName)
    }

}