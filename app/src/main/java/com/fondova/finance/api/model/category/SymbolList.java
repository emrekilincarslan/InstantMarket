package com.fondova.finance.api.model.category;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SymbolList {

    @SerializedName("symbol")
    @Expose
    public String symbol;

    @SerializedName("description")
    @Expose
    public String description;

    @SerializedName("market")
    @Expose
    public String market;

}