package com.fondova.finance.api.model.unwatch;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.base.MetaRequest;

public class UnwatchRequest {

    public static final String COMMAND_UNWATCH = "Unwatch";

    @SerializedName("meta")
    @Expose
    public MetaRequest meta;


    public static UnwatchRequest create(String reqId) {
        MetaRequest meta = new MetaRequest();
        meta.command = COMMAND_UNWATCH;
        meta.requestId = reqId;

        UnwatchRequest quoteRequest = new UnwatchRequest();
        quoteRequest.meta = meta;

        return quoteRequest;
    }
}
