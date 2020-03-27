package com.fondova.finance.api.model.quote;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.base.MetaRequest;

public class OldQuoteSnapRequest {

    public static final String COMMAND_QUOTE_SNAP = "QuoteSnap";

    @SerializedName("meta")
    @Expose
    public MetaRequest meta;
    @SerializedName("data")
    @Expose
    public QuoteData data;


    public static OldQuoteSnapRequest create(String reqId, String[] fields, String... expression) {
        MetaRequest meta = new MetaRequest();
        meta.command = COMMAND_QUOTE_SNAP;
        meta.requestId = reqId;

        QuoteData quoteData = new QuoteData(expression);

        OldQuoteSnapRequest quoteRequest = new OldQuoteSnapRequest();
        quoteRequest.meta = meta;
        quoteRequest.data = quoteData;
        quoteRequest.data.fields = fields;

        return quoteRequest;
    }
}
