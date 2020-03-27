package com.fondova.finance.api.model.workspace

import com.google.gson.Gson
import com.fondova.finance.persistance.fromJson
import com.fondova.finance.util.JsonFileReader
import org.junit.Assert.*
import org.junit.Test

class WorkspaceDetailsResponseTest {

    @Test
    fun fromJson() {
        val json = JsonFileReader().getJsonFromFile("WorkspaceDetailsResponseSample.json") ?: "{}"

        val testObject = Gson().fromJson<WorkspaceDetailsResponse>(json)
        assertEquals("WebRequest", testObject.meta?.command)
        assertEquals("D3BF3E6C-9C04-4FAF-99C1-B250881EA1BA", testObject.meta?.requestId)
        assertEquals(200, testObject.meta?.status)
        assertEquals("e1b2bcb5-fb8f-5f2a-ad65-b4598f2e464f", testObject.data?.first()?.getWorkspace()?.getWorkspaceId())
    }
}