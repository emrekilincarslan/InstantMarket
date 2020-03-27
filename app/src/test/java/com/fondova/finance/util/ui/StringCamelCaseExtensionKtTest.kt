package com.fondova.finance.util.ui

import org.junit.Assert.*
import org.junit.Test

class StringCamelCaseExtensionKtTest {


    @Test
    fun fromCamelCaseToSpaces() {

        assertEquals("High", "High".fromCamelCaseToSpaces())
        assertEquals("Low", "Low".fromCamelCaseToSpaces())
        assertEquals("Open", "Open".fromCamelCaseToSpaces())
        assertEquals("Bid", "Bid".fromCamelCaseToSpaces())
        assertEquals("Ask", "Ask".fromCamelCaseToSpaces())
        assertEquals("Cum Volume", "CumVolume".fromCamelCaseToSpaces())
        assertEquals("Open Interest", "OpenInterest".fromCamelCaseToSpaces())
        assertEquals("Volitility", "Volitility".fromCamelCaseToSpaces())
        assertEquals("Bid Size", "BidSize".fromCamelCaseToSpaces())
        assertEquals("Ask Size", "AskSize".fromCamelCaseToSpaces())

    }
}