package com.fondova.finance.api.model.refresh_rate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.base.MetaRequest;

public class RefreshRateRequest {

    public static final String COMMAND_REFRESH_RATE = "ThrottleChange";

    @SerializedName("meta")
    @Expose
    public MetaRequest meta;
    @SerializedName("data")
    @Expose
    public RefreshRateData data;

    public static RefreshRateRequest create(int refreshRate) {
        MetaRequest meta = new MetaRequest();
        meta.command = COMMAND_REFRESH_RATE;
        meta.requestId = ""; // empty so that it updates everything

        RefreshRateData data = new RefreshRateData();
        data.updateInterval = refreshRate;

        RefreshRateRequest loginRequest = new RefreshRateRequest();
        loginRequest.meta = meta;
        loginRequest.data = data;

        return loginRequest;
    }
}