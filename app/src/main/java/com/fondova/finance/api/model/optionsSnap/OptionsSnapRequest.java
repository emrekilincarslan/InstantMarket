package com.fondova.finance.api.model.optionsSnap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.base.MetaRequest;

public class OptionsSnapRequest {

    public static final String COMMAND_OPTIONS_PAGE_SNAP = "OptionMaturitySnap";

    @SerializedName("meta")
    @Expose
    public MetaRequest meta;
    @SerializedName("data")
    @Expose
    public OptionsSnapData data;


    public static OptionsSnapRequest create(String reqId, String symbol) {
        MetaRequest meta = new MetaRequest();
        meta.command = COMMAND_OPTIONS_PAGE_SNAP;
        meta.requestId = reqId;

        OptionsSnapData newsPageSnapData = new OptionsSnapData(symbol);

        OptionsSnapRequest optionsSnapRequest = new OptionsSnapRequest();
        optionsSnapRequest.meta = meta;
        optionsSnapRequest.data = newsPageSnapData;

        return optionsSnapRequest;
    }
}