package com.fondova.finance.api.model.base;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MetaResponse {

    @SerializedName("command")
    @Expose
    public String command;
    @SerializedName("status")
    @Expose
    public int status;

    @SerializedName("requestId")
    @Expose
    public String requestId;

    @Override
    public String toString() {
        return "MetaResponse{" +
                "command='" + command + '\'' +
                ", status=" + status +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}