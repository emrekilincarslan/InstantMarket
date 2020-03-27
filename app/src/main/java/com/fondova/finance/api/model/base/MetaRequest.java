package com.fondova.finance.api.model.base;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MetaRequest {

    @SerializedName("command")
    @Expose
    public String command;
    @SerializedName("requestId")
    @Expose
    public String requestId;


}