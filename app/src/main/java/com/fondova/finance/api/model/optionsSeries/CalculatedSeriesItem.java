package com.fondova.finance.api.model.optionsSeries;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CalculatedSeriesItem {
    @SerializedName("OptionDaysToExpiration")
    @Expose
    public Double optionDaysToExpiration;

    @SerializedName("OptionExpirationDate")
    @Expose
    public String optionExpirationDate;

    @SerializedName("OpenInterestCall")
    @Expose
    public Double openInterestCall;

    @SerializedName("OpenInterestPut")
    @Expose
    public Double openInterestPut;
}