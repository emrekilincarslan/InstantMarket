package com.fondova.finance.api.model.symbol;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.fondova.finance.vo.Quote;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SymbolSearchResponseDeserializer implements JsonDeserializer<SymbolSearchResponse> {


    @Override
    public SymbolSearchResponse deserialize(JsonElement json, Type typeOfT,
                                            JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        JsonObject metaJson = jsonObject.getAsJsonObject("meta");
        JsonArray dataJson = jsonObject.getAsJsonArray("data");

        SymbolSearchResponse symbolSearchResponse = new SymbolSearchResponse();
        MetaSymbolSearchResponse meta = new MetaSymbolSearchResponse();

        meta.command = metaJson.get("command").getAsString();
        meta.status = metaJson.get("status").getAsInt();
        meta.requestId = metaJson.get("requestId").getAsString();
        meta.moreSymbols = metaJson.get("moreSymbols").getAsBoolean();


        List<Quote> quotes = new ArrayList<>();
        for (int i = 0; i < dataJson.size(); i++) {
            JsonObject row = (JsonObject) dataJson.get(i);

            Quote quote = new Quote();
            quote.displayName = row.getAsJsonObject("symbol").get("symbol").getAsString();
            quote.requestName = quote.displayName;

            JsonElement description = row.get("description");
            if (description != null) {
                quote.description = description.getAsString();
            }

            JsonElement expirationDate = row.get("expirationDate");
            if (expirationDate != null) {
                quote.expirationDate = expirationDate.getAsString();
            }

            quotes.add(quote);
        }

        symbolSearchResponse.meta = meta;
        symbolSearchResponse.quotes = quotes;

        return symbolSearchResponse;
    }
}
