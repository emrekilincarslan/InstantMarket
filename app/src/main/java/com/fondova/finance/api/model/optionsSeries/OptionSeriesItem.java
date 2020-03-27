package com.fondova.finance.api.model.optionsSeries;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OptionSeriesItem {
    @SerializedName("Symbol")
    @Expose
    public String symbol;

    @SerializedName("Last")
    @Expose
    public String last;

    @SerializedName("Bid")
    @Expose
    public String bid;

    @SerializedName("Ask")
    @Expose
    public String ask;

    @SerializedName("CumVolume")
    @Expose
    public Double cumVolume;

    @SerializedName("Change")
    @Expose
    public String change;

    @SerializedName("ExercisePrice")
    @Expose
    public String exercisePrice;

}