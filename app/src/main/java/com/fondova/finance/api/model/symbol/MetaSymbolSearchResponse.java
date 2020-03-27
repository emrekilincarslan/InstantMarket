package com.fondova.finance.api.model.symbol;

import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.base.MetaResponse;

public class MetaSymbolSearchResponse extends MetaResponse {

    @SerializedName("moreSymbols")
    public boolean moreSymbols;

}
