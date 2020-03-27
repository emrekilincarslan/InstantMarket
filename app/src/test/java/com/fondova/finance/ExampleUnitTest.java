package com.fondova.finance;

import com.google.gson.Gson;
import com.fondova.finance.api.model.base.WebsocketApiRequest;
import com.fondova.finance.api.model.workspace.SetDefaultWorkspaceRequest;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * LoginRequest local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        WebsocketApiRequest testRequest = new SetDefaultWorkspaceRequest("1", "2");
        String json = new Gson().toJson(testRequest);
        assertEquals(4, 2 + 2);
    }
}