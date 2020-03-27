package com.fondova.finance.api.model.optionsSnap;

import com.google.gson.annotations.SerializedName;

public class OptionsSnapData {

    @SerializedName("symbol")
    public String symbol;

    public OptionsSnapData(String symbol) {
        this.symbol = symbol;
    }
}
