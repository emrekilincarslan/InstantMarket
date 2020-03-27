package com.fondova.finance.repo;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.fondova.finance.vo.NewsCategory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultNewsRepository {

    private final Gson gson = new Gson();

    @Inject
    public DefaultNewsRepository() {
    }


    @NonNull
    public List<NewsCategory> createDefaultNewsCategories() {
        Type listType = new TypeToken<List<NewsCategoryMap>>() {
        }.getType();
        final List<NewsCategoryMap> newsCategoryList =
                gson.fromJson(DEFAULT_NEWS_CATEGORIES_JSON, listType);
        final int size = newsCategoryList.size();

        List<NewsCategory> categories = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            NewsCategoryMap map = newsCategoryList.get(i);
            NewsCategory category = new NewsCategory();

            if (map.keywords != null && map.keywords.size() > 0) {
                category.keywords = TextUtils.join(" ", map.keywords);
            }
            category.name = map.name;
            category.query = map.query;
            category.order = i;

            categories.add(category);
        }
        return categories;
    }

    private static final String DEFAULT_NEWS_CATEGORIES_JSON = "[\n" +
            "{ \"name\":\"All News\", \"query\": \"\" }\n" +
            ",\n" +
            "{ \"name\": \"Economy\", \"query\": \"ECONOMY OR \\\"FEDERAL RESERVE\\\" OR "
            + "\\\"FACTORY ORDERS\\\" OR CPI OR JOBLESS OR UNEMPLOYMENT OR EMPLOYMENT OR BERNANKE"
            + " OR \\\"DEBT CEILING\\\" OR \\\"CONSUMER SPENDING\\\"\", \"keywords\": "
            + "[\"Economy\", \"FEDERAL RESERVE\", \"FACTORY ORDERS\", \"CPI\", \"JOBLESS\","
            + "\"UNEMPLOYMENT\", \"EMPLOYMENT\", \"BERNANKE\",\"DEBT CEILING\", \"CONSUMER "
            + "SPENDING\"] }\n"
            +
            ",\n" +
            "{ \"name\": \"Energy Products\", \"query\": \"\\\"crude oil\\\" OR \\\"natural "
            + "gas\\\" OR \\\"rbob gasoline\\\" OR \\\"heating oil\\\"\", \"keywords\": [\"crude "
            + "oil\", \"natural gas\",\"rbob gasoline\", \"heating oil\"] }\n"
            +
            ",\n" +
            "{ \"name\": \"Grains\", \"query\": \"CORN WHEAT OR \\\"soybean oil\\\" OR soybeans "
            + "OR GRAIN\", \"keywords\": [\"CORN\", \"WHEAT\", \"soybean oil\", \"soybeans\", "
            + "\"GRAIN\"] }\n"
            +
            ",\n" +
            "{ \"name\": \"Precious Metals\", \"query\": \"gold OR silver OR platinum OR "
            + "palladium\", \"keywords\": [\"gold\", \"silver\", \"platinum\", \"palladium\"] }\n"
            +
            ",\n" +
            "{ \"name\": \"Refinery Outages\", \"query\": \"\\\"REFINERY OUTAGE\\\" OR "
            + "\\\"REFINERY OUTAGES\\\"\", \"keywords\": [\"REFINERY OUTAGE\", \"REFINERY "
            + "OUTAGES\"] }\n"
            +
            ",\n" +
            "{ \"name\": \"USDA\", \"query\": \"USDA\", \"keywords\": [\"USDA\"] }\n" +
            "]";

    private class NewsCategoryMap {
        @SerializedName("name")
        public String name;

        @SerializedName("query")
        public String query;

        @SerializedName("keywords")
        public List<String> keywords;
    }
}
