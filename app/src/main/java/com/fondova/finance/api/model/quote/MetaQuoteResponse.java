package com.fondova.finance.api.model.quote;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.base.MetaResponse;

import java.util.List;

public class MetaQuoteResponse extends MetaResponse {

    @SerializedName("symbols")
    @Expose
    public List<String> symbols = null;
    @SerializedName("expression")
    @Expose
    public String expression;
}
