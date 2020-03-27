package com.fondova.finance.api.model.symbol;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.base.MetaRequest;

import java.util.UUID;

public class SymbolSearchRequest {

    public static final String COMMAND_SYMBOL_SEARCH = "SymbolSearch";
    public static String REQUEST_ID_QUOTE_WATCH = UUID.randomUUID().toString();
    private static final String SYMBOL_FORMAT = "dict";

    @SerializedName("meta")
    @Expose
    public MetaRequest meta;

    @SerializedName("data")
    @Expose
    public SymbolData data;


    public static SymbolSearchRequest create(String query) {
        MetaRequest meta = new MetaRequest();
        meta.command = COMMAND_SYMBOL_SEARCH;
        REQUEST_ID_QUOTE_WATCH = UUID.randomUUID().toString();
        meta.requestId = REQUEST_ID_QUOTE_WATCH;

        SymbolData quoteData = new SymbolData(query);
        quoteData.setSymbolFormat(SYMBOL_FORMAT);

        SymbolSearchRequest quoteRequest = new SymbolSearchRequest();
        quoteRequest.meta = meta;
        quoteRequest.data = quoteData;

        return quoteRequest;
    }
}
