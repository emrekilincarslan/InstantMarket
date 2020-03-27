package com.fondova.finance.api.model.optionsSeries;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OptionsSeriesWatchResponse {
    @SerializedName("data")
    @Expose
    public List<OptionSeriesWatchItem> data;

    @SerializedName("meta")
    @Expose
    public OptionSeriesMeta meta;

    @SerializedName("errors")
    @Expose
    public List<OptionSeriesError> errors;
}