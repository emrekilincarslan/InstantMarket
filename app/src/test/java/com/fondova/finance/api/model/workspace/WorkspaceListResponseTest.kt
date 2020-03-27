package com.fondova.finance.api.model.workspace

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class WorkspaceListResponseTest {

    @Test
    fun fromJson() {
        val testJson = "        {\n" +
                "            \"meta\": {\n" +
                "                \"command\": \"WebRequest\",\n" +
                "                \"requestId\": \"4AD50580-62FE-4A19-A8A9-00195F6D0FC2\",\n" +
                "                \"status\": 200\n" +
                "            },\n" +
                "            \"data\": [\n" +
                "            {\n" +
                "            \"data\": [\n" +
                "            {\n" +
                "            \"Name\": \"My Workspace\",\n" +
                "            \"WorkspaceId\": \"e1b2bcb5-fb8f-5f2a-ad65-b4598f2e464f\",\n" +
                "            \"IsDefault\": true\n" +
                "            }\n" +
                "            ]\n" +
                "            }\n" +
                "            ]\n" +
                "        }\n"

        val testObject: WorkspaceListResponse = Gson().fromJson(testJson, WorkspaceListResponse::class.java)

        assertEquals("WebRequest", testObject.meta?.command)
        assertEquals("4AD50580-62FE-4A19-A8A9-00195F6D0FC2", testObject.meta?.requestId)
        assertEquals(200, testObject.meta?.status)
        assertEquals(1, testObject.data?.first()?.data?.size)
        assertEquals("My Workspace", testObject.data?.first()?.getWorkspaces()?.first()?.getName())
        assertEquals("e1b2bcb5-fb8f-5f2a-ad65-b4598f2e464f", testObject.data?.first()?.getWorkspaces()?.first()?.getWorkspaceId())
        assertEquals(true, testObject.data?.first()?.getWorkspaces()?.first()?.isDefault())

    }
}