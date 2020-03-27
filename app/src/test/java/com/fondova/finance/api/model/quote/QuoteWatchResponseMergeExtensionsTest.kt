package com.fondova.finance.api.model.quote

import org.junit.Assert.*
import org.junit.Test

class QuoteWatchResponseMergeExtensionsTest {

    @Test
    fun mergeUpdatesData() {
        val oldResponse = QuoteWatchResponse()
        oldResponse.data?.first()?.set("Monkey", "See")

        val newResponse = QuoteWatchResponse()
        newResponse.data?.first()?.set("Monkey", "Do")

        val updatedResponse = oldResponse.update(newResponse)

        assertEquals("Do", updatedResponse.data?.first()?.get("Monkey"))
    }

    @Test
    fun addMissingFields() {
        val oldResponse = QuoteWatchResponse()
        oldResponse.data?.first()?.set("Monkey", "See")

        val newResponse = QuoteWatchResponse()
        newResponse.data?.first()?.set("Banana", "Whoops")

        val updatedResponse = oldResponse.update(newResponse)

        assertEquals("See", updatedResponse.data?.first()?.get("Monkey"))
        assertEquals("Whoops", updatedResponse.data?.first()?.get("Banana"))
    }

    @Test
    fun doNotOverwriteExistingDataWithNull() {
        val oldResponse = QuoteWatchResponse()
        oldResponse.data?.first()?.set("Monkey", "See")

        val newResponse = QuoteWatchResponse()

        val updatedResponse = oldResponse.update(newResponse)

        assertEquals("See", updatedResponse.data?.first()?.get("Monkey"))
    }

}