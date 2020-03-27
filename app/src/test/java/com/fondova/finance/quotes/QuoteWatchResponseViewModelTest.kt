package com.fondova.finance.quotes

import com.fondova.finance.R
import com.fondova.finance.api.model.quote.*
import com.fondova.finance.workspace.WorkspaceQuote
import com.fondova.finance.workspace.instantmarket.IMWorkspaceQuote
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class QuoteWatchResponseViewModelTest {

    lateinit var testResponse: QuoteWatchResponse
    lateinit var testQuote: WorkspaceQuote
    lateinit var testObject: QuoteWatchResponseViewModel

    @Before
    fun setup() {
        testResponse = QuoteWatchResponse()
        testResponse.meta = QuoteWatchResponseMeta()
        testResponse.meta?.status = 200

        testQuote = IMWorkspaceQuote()
        testObject = QuoteWatchResponseViewModel(testResponse, testQuote)
    }

    @Test
    fun getLastTextForValidResponse() {
        testResponse.last = "1234"

        assertEquals("1234", testObject.getLastText())

    }

    @Test
    fun getLastTextForValidResponseMissingLastData() {
        testResponse.last = null

        assertEquals("--", testObject.getLastText())

    }

    @Test
    fun getLastTextForNonPermissionedQuote() {
        testQuote.setValue("AAPL")
        val error = QuoteWatchError()
        error.code = "Quote Failed"
        val error2 = QuoteWatchError()
        error2.code = "Not Permissioned"
        testResponse.errors = listOf(error, error2)

        assertEquals("np", testObject.getLastText())

    }

    @Test
    fun getTitleTextForNonPermissionedQuote() {
        testQuote.setValue("AAPL")
        val error = QuoteWatchError()
        error.code = "Quote Failed"
        val error2 = QuoteWatchError()
        error2.code = "Not Permissioned"
        testResponse.errors = listOf(error, error2)

        assertEquals("AAPL - Not Permissioned", testObject.getTitleText())

    }

    @Test
    fun getTitleTextForNotFOundQuote() {
        testQuote.setValue("AAPL")
        val error = QuoteWatchError()
        error.code = "Quote Failed"
        val error2 = QuoteWatchError()
        error2.code = "Symbol Not Found"

        testResponse.errors = listOf(error, error2)

        assertEquals("AAPL - Invalid", testObject.getTitleText())

    }

    @Test
    fun getTitleTextForExpiredOundQuote() {
        testQuote.setValue("AAPL")
        val error = QuoteWatchError()
        error.code = "Quote Failed"
        error.detail = "Unknown error. There are no quote updates available for symbol @CK16.  It may be expired."

        testResponse.errors = listOf(error)

        assertEquals("AAPL - Expired", testObject.getTitleText())

    }

    @Test
    fun getTitleTextForExpressionReturnsQuoteDisplayName() {
        testQuote.setDisplayName("Display Name")
        testQuote.setType("Expression")
        testResponse.symbolDescription = "Symbol Description"
        testObject.showExpressionDescriptions = false

        assertEquals("Display Name", testObject.getTitleText())

    }

    @Test
    fun getTitleTextForExpressionReturnsDescription() {
        testQuote.setDisplayName("Display Name")
        testQuote.setType("Expression")
        testResponse.symbolDescription = "Symbol Description"
        testObject.showExpressionDescriptions = true

        assertEquals("Symbol Description", testObject.getTitleText())

    }

    @Test
    fun getTitleTextForSymbolReturnsResponseDescription() {
        testQuote.setDisplayName("Display Name")
        testQuote.setType("Symbol")
        testResponse.symbolDescription = "Symbol Description"

        assertEquals("Symbol Description", testObject.getTitleText())

    }

    @Test
    fun getTitleTextForSymbolReturnsResponseDescriptionWithoutDelayedText() {
        testQuote.setDisplayName("Display Name")
        testQuote.setType("Symbol")
        testResponse.symbolDescription = "Symbol Description [Delayed 15 Minutes]"

        assertEquals("Symbol Description", testObject.getTitleText())

    }

    @Test
    fun getTitleTextForEmptyResponseReturnsQuoteValue() {
        testQuote.setValue("AAPL")
        assertEquals("AAPL", testObject.getTitleText())

    }

    @Test
    fun getTitleTextForEmptyResponseAndQuoteReturnsDefaultString() {
        testObject = QuoteWatchResponseViewModel(null, null)

        assertEquals("--", testObject.getTitleText())

    }

    @Test
    fun getTitleTextForSymbolWithQuoteDelayReturnsResponseDescriptionWithDelay() {
        testQuote.setDisplayName("Display Name")
        testQuote.setType("Symbol")
        testResponse.symbolDescription = "Symbol Descriptioni"
        testResponse.quoteDelay = 12

        assertEquals("Symbol Descriptioni [12]", testObject.getTitleText())

    }

    @Test
    fun getChangeBackgroundResourceForZeroReturnsBlack() {
        testResponse.changePercentage = "0"

        assertEquals(R.drawable.current_price_black_background, testObject.getChangeBackgroundResource())
    }

    @Test
    fun getChangeBackgroundResourceForNullReturnsBlack() {
        testResponse.changePercentage = null

        assertEquals(R.drawable.current_price_black_background, testObject.getChangeBackgroundResource())
    }

    @Test
    fun getChangeBackgroundResourceForPositiveReturnsGreen() {
        testResponse.changePercentage = "0.1"

        assertEquals(R.drawable.current_price_green_background, testObject.getChangeBackgroundResource())
    }

    @Test
    fun getChangeBackgroundResourceForNegativeReturnsRed() {
        testResponse.changePercentage = "-0.1"

        assertEquals(R.drawable.current_price_red_background, testObject.getChangeBackgroundResource())
    }

    @Test
    fun valueForFloatField() {
        testResponse.data = mutableListOf(mutableMapOf<String, Any>(Pair("thing", 1.1.toFloat())))

        assertEquals("1.1", testObject.valueForField("thing"))
    }

    @Test
    fun valueForNonPermissionedField() {
        testQuote.setValue("AAPL")
        val error = QuoteWatchError()
        error.code = "Quote Failed"
        val error2 = QuoteWatchError()
        error2.code = "Not Permissioned"
        testResponse.errors = listOf(error, error2)

        assertEquals("np", testObject.valueForField("thing"))

    }

    @Test
    fun valueForFloatFieldWithEmptyDecimal() {
        testResponse.data = mutableListOf(mutableMapOf<String, Any>(Pair("thing", 1.0.toFloat())))

        assertEquals("1", testObject.valueForField("thing"))
    }

    @Test
    fun valueForCropDataField() {
        testResponse.data = mutableListOf(mutableMapOf<String, Any>(Pair("thing", "111'1")))

        assertEquals("111'1", testObject.valueForField("thing"))
    }

    @Test
    fun valueForUnknownFieldReturnsDefaultValue() {
        testResponse.data = mutableListOf(mutableMapOf())

        assertEquals("--", testObject.valueForField("thing"))
    }

    @Test
    fun landscapeSubheaderText() {
        testResponse.open = "1111.1111"
        testResponse.last = "2222.2222"
        testResponse.high = "3333.3333"
        testResponse.low = "4444.4444"
        testResponse.volume = 1234
        testResponse.actualSymbol = "AAPL"

        val expectedString = "AAPL  La:2222.2222  O:1111.1111  H:3333.3333  L:4444.4444  V:1234"

        assertEquals(expectedString, testObject.getLandscapeSubtitle())


    }

    @Test
    fun getActualSymbolShowsActualSymbolFromResponse() {
        testResponse.actualSymbol = "Actual Symbol"
        testQuote.setValue("Request Symbol")

        assertEquals("Actual Symbol", testObject.getActualSymbol())
    }

    @Test
    fun getActualSymbolShowsRequestSymbolWhenActualSymbolIsMissing() {
        testResponse.actualSymbol = null
        testQuote.setValue("Request Symbol")

        assertEquals("Request Symbol", testObject.getActualSymbol())
    }

}