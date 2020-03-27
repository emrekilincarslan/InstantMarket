package com.fondova.finance.api.model.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

class LoginData {

    @SerializedName("username")
    @Expose
    public String username;
    @SerializedName("password")
    @Expose
    public String password;
    @SerializedName("appname")
    @Expose
    public String appname;
    @SerializedName("version")
    @Expose
    public String version;
    @SerializedName("defaultUpdateInterval")
    @Expose
    public Double defaultUpdateInterval = 5.0;

}