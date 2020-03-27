package com.fondova.finance.api.model.quote;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.vo.QuoteValue;

import java.util.List;

public class OldQuoteWatchResponse {

    @SerializedName("meta")
    public MetaResponse meta;

    @SerializedName("data")
    @Expose
    public List<QuoteValue> data;

    public class MetaResponse {

        @SerializedName("command")
        @Expose
        public String command;
        @SerializedName("status")
        @Expose
        public int status;

        @SerializedName("requestId")
        @Expose
        public String requestId;

        @SerializedName("symbols")
        public List<SymbolItem> symbols;
    }

    public class SymbolItem {

        @SerializedName("symbol")
        public String symbol;

        @SerializedName("market")
        public String market;
    }
}
