package com.fondova.finance.api.model.news;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.base.MetaRequest;

public class NewsWatchRequest {

    public static final String COMMAND_NEWS_CATEGORY_WATCH = "NewsWatch";

    @SerializedName("meta")
    @Expose
    public MetaRequest meta;
    @SerializedName("data")
    @Expose
    public NewsData data;


    public static NewsWatchRequest create(String reqId, String query) {
        MetaRequest meta = new MetaRequest();
        meta.command = COMMAND_NEWS_CATEGORY_WATCH;
        meta.requestId = reqId;

        final NewsData newsData = new NewsData(query);

        final NewsWatchRequest newsRequest = new NewsWatchRequest();
        newsRequest.meta = meta;
        newsRequest.data = newsData;

        return newsRequest;
    }

    public static NewsWatchRequest createForCharts(String reqId, String symbol, int limit) {
        NewsWatchRequest newsWatchRequest = create(reqId, "");
        newsWatchRequest.data.symbol = symbol.replaceAll("\'", "");
        newsWatchRequest.data.limit = limit;
        return newsWatchRequest;
    }
}


