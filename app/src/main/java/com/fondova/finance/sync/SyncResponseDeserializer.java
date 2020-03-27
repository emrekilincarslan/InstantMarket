package com.fondova.finance.sync;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SyncResponseDeserializer implements JsonDeserializer<HashMap<String, Object>> {


    @Override
    public HashMap<String, Object> deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        JsonArray newsJson = jsonObject.getAsJsonArray(SyncManager.NEWS);
        JsonArray quotesJson = jsonObject.getAsJsonArray(SyncManager.QUOTES);

        HashMap<String, Object> map = new HashMap<>();
        List<QuoteSyncItem> quotes = new ArrayList<>();
        List<NewsCategorySyncItem> categories = new ArrayList<>();


        for (int i = 0; i < quotesJson.size(); i++) {
            JsonObject row = (JsonObject) quotesJson.get(i);

            String displayName = row.get("display_name").getAsString();
            String requestName = row.get("request_name").getAsString();
            int order = row.get("order").getAsInt();
            int type = row.get("type").getAsInt();

            quotes.add(new QuoteSyncItem(requestName, displayName, order, type));
        }

        for (int i = 0; i < newsJson.size(); i++) {
            JsonObject row = (JsonObject) newsJson.get(i);

            String name = row.get("name").getAsString();
            String query = row.get("query").getAsString();
            String keywords = null;
            int order = row.get("order").getAsInt();
            boolean isQuoteRelated = row.get("is_quote_related").getAsBoolean();

            if (row.has("keywords")) {
                keywords = row.get("keywords").getAsString();
            }

            categories.add(new NewsCategorySyncItem(name, query, keywords, order, isQuoteRelated));
        }

        map.put(SyncManager.QUOTES, quotes);
        map.put(SyncManager.NEWS, categories);

        return map;
    }
}
