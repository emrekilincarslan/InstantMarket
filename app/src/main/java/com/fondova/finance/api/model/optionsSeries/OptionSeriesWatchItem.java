package com.fondova.finance.api.model.optionsSeries;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class OptionSeriesWatchItem {

    @SerializedName("underlying")
    @Expose
    public OptionsUnderlying underlying;

    @SerializedName("options")
    @Expose
    public ArrayList<OptionSeriesWatchHeadline> options;

    @SerializedName("calculated")
    @Expose
    public CalculatedSeriesItem calculated;
}