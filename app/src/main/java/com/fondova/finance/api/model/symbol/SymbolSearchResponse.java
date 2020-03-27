package com.fondova.finance.api.model.symbol;

import com.google.gson.annotations.SerializedName;
import com.fondova.finance.vo.Quote;

import java.util.List;

public class SymbolSearchResponse {

    @SerializedName("meta")
    public MetaSymbolSearchResponse meta;

    @SerializedName("data")
    public List<Quote> quotes;

}
