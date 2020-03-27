package com.fondova.finance.api.model.news;


import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.base.MetaResponse;

import java.util.Collections;
import java.util.List;

public class NewsWatchResponse {

    @SerializedName("meta")
    public NewsWatchResponseMeta meta;

    @SerializedName("data")
    public List<CategoryArticle> data;

    public static class NewsWatchResponseMeta extends MetaResponse {

        @SerializedName("keywords")
        public List<String> keywords;
    }


    public static NewsWatchResponse createEmptyResponse() {
        final NewsWatchResponse response = new NewsWatchResponse();
        response.data = Collections.emptyList();
        final NewsWatchResponseMeta meta = new NewsWatchResponseMeta();
        meta.keywords = Collections.emptyList();
        response.meta = meta;
        return response;
    }
}
