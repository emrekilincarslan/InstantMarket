package com.fondova.finance.api.model.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.base.MetaRequest;

public class LoginRequest {

    public static final String COMMAND_LOGIN = "Login";
    private static final String REQUEST_ID_LOGIN = "1";


    @SerializedName("meta")
    @Expose public MetaRequest meta;
    @SerializedName("data")
    @Expose public LoginData data;

    public static LoginRequest create(String username, String password, String appName,
                                      String appVersion, Double defaultUpdateInterval) {
        MetaRequest meta = new MetaRequest();
        meta.command = COMMAND_LOGIN;
        meta.requestId = REQUEST_ID_LOGIN;

        LoginData loginData = new LoginData();
        loginData.appname = appName;
        loginData.version = appVersion;
        loginData.username = username;
        loginData.password = password;

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.meta = meta;
        loginRequest.data = loginData;
        loginRequest.data.defaultUpdateInterval = defaultUpdateInterval;

        return loginRequest;
    }
}