package com.fondova.finance.api.model.optionsSeries;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OptionSeriesMeta {
    @SerializedName("command")
    @Expose
    public String command;

    @SerializedName("requestId")
    @Expose
    public String requestId;

    @SerializedName("status")
    @Expose
    public String status;
}