package com.fondova.finance.api.model.news;

import com.google.gson.annotations.SerializedName;
import com.fondova.finance.enums.LocationType;

public class NewsData {

    public static final int DEFAULT_LIMIT = 120;
    private static final String DEFAULT_LOCATION = LocationType.ALL;
    private static final String DEFAULT_DATE_FORMAT = "text";

    @SerializedName("symbol")
    public String symbol;

    @SerializedName("query")
    public String query;

    @SerializedName("limit")
    public int limit;

    @SerializedName("location")
    public String location;

    @SerializedName("source")
    public String source;

    @SerializedName("timeFormat")
    public String timeFormat;

    public NewsData(String query) {
        this(query, DEFAULT_LIMIT, DEFAULT_LOCATION, null, DEFAULT_DATE_FORMAT);
    }

    NewsData(String query, int limit, String location, String source, String timeFormat) {
        this.symbol = null;
        this.query = query;
        this.limit = limit;
        this.location = location;
        this.source = source;
        this.timeFormat = timeFormat;
    }
}
