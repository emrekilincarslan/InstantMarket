package com.fondova.finance.repo

import com.fondova.finance.vo.QuoteValue
import org.junit.Assert.assertEquals
import org.junit.Test

class QuoteWatchRepositoryTest {

    @Test
    fun mergeData() {

        var testQuoteInitialValue = QuoteValue()
        testQuoteInitialValue.actualSymbol = "AAPL"
        testQuoteInitialValue.change = "1"
        testQuoteInitialValue.currentAsk = "2"
        testQuoteInitialValue.currentBestBid = "3"
        testQuoteInitialValue.highPrice = "4"

        var testQuoteNewValue = QuoteValue()
        testQuoteNewValue.highPrice = "5"
        testQuoteNewValue.lowPrice = "6"
        val testObject = QuoteWatchRepository()
        val mergedValue = testObject.mergeData(testQuoteInitialValue, testQuoteNewValue)

        assertEquals("1", mergedValue.change)
        assertEquals("2", mergedValue.currentAsk)
        assertEquals("3", mergedValue.currentBestBid)
        assertEquals("5", mergedValue.highPrice)
        assertEquals("6", mergedValue.lowPrice)

    }
}