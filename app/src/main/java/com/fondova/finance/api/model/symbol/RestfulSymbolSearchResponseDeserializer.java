package com.fondova.finance.api.model.symbol;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.fondova.finance.api.model.category.CategoriesData;
import com.fondova.finance.api.model.category.CategoriesResponse;
import com.fondova.finance.api.model.category.Category;
import com.fondova.finance.vo.Quote;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class RestfulSymbolSearchResponseDeserializer implements JsonDeserializer<CategoriesResponse> {


    @Override
    public CategoriesResponse deserialize(JsonElement json, Type typeOfT,
                                          JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        JsonObject data = jsonObject.getAsJsonObject("data");
        JsonArray categoryListJson = data.getAsJsonArray("categoryList");
        JsonArray symbolListJson = data.getAsJsonArray("symbolList");

        CategoriesResponse categoriesResponse = new CategoriesResponse();
        categoriesResponse.categoriesData = new CategoriesData();
        categoriesResponse.categoriesData.categoryList = new ArrayList<>();
        categoriesResponse.categoriesData.symbolList = new ArrayList<>();

        for (JsonElement category : categoryListJson) {
            JsonObject categoryAsJsonObject = category.getAsJsonObject();
            Category c = new Category();
            c.name = categoryAsJsonObject.get("name").getAsString();
            c.more = categoryAsJsonObject.get("more").getAsBoolean();
            categoriesResponse.categoriesData.categoryList.add(c);
        }

        for (int i = 0; i < symbolListJson.size(); i++) {
            JsonObject row = (JsonObject) symbolListJson.get(i);

            Quote quote = new Quote();
            quote.displayName = row.get("symbol").getAsString();
            quote.requestName = quote.displayName;

            JsonElement description = row.get("description");
            if (description != null) {
                quote.description = description.getAsString();
            }

            JsonElement expirationDate = row.get("expirationDate");
            if (expirationDate != null) {
                quote.expirationDate = expirationDate.getAsString();
            }

            categoriesResponse.categoriesData.symbolList.add(quote);
        }

        return categoriesResponse;
    }
}
