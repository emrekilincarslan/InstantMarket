package com.fondova.finance.api.model.optionsSeries;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OptionsUnderlying {
    @SerializedName("Symbol")
    @Expose
    public String symbol;

    @SerializedName("Last")
    @Expose
    public String last;

    @SerializedName("DaysToExpiration")
    @Expose
    public Double daysToExpiration;

    @SerializedName("Volatility")
    @Expose
    public String volatility;

    @SerializedName("ExpirationDate")
    @Expose
    public String expirationDate;
}