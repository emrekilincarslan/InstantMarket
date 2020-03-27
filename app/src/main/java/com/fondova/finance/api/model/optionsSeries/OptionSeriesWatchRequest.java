package com.fondova.finance.api.model.optionsSeries;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.base.MetaRequest;
import com.fondova.finance.api.model.optionsSnap.OptionsSnap;

public class OptionSeriesWatchRequest {
    public static final String COMMAND_OPTIONS_PAGE_SERIES = "OptionSeriesWatch";

    @SerializedName("meta")
    @Expose
    public MetaRequest meta;
    @SerializedName("data")
    @Expose
    public OptionsSeriesData data;


    public static OptionSeriesWatchRequest create(String reqId, OptionsSnap optionsSnap) {
        MetaRequest meta = new MetaRequest();
        meta.command = COMMAND_OPTIONS_PAGE_SERIES;
        meta.requestId = reqId;

        OptionsSeriesData optionsSeriesData = new OptionsSeriesData(optionsSnap);

        OptionSeriesWatchRequest optionsSeriesRequest = new OptionSeriesWatchRequest();
        optionsSeriesRequest.meta = meta;
        optionsSeriesRequest.data = optionsSeriesData;

        return optionsSeriesRequest;
    }
}