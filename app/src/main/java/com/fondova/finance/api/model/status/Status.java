package com.fondova.finance.api.model.status;

import com.google.gson.annotations.SerializedName;

public class Status {

    @SerializedName("type")
    public String type;

    @SerializedName("name")
    public String name;

    @SerializedName("message")
    public String message;

    @SerializedName("status")
    public int status;

}
