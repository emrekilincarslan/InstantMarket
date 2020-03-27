package com.fondova.finance.api.model.news;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.base.MetaRequest;

public class NewsPageSnapRequest {
    public static final String COMMAND_NEWS_PAGE_SNAP = "NewsPageSnap";

    @SerializedName("meta")
    @Expose
    public MetaRequest meta;
    @SerializedName("data")
    @Expose
    public NewsPageSnapData data;


    public static NewsPageSnapRequest create(String reqId, String storyId) {
        MetaRequest meta = new MetaRequest();
        meta.command = COMMAND_NEWS_PAGE_SNAP;
        meta.requestId = reqId;

        NewsPageSnapData newsPageSnapData = new NewsPageSnapData(storyId);

        NewsPageSnapRequest newsPageSnapRequest = new NewsPageSnapRequest();
        newsPageSnapRequest.meta = meta;
        newsPageSnapRequest.data = newsPageSnapData;

        return newsPageSnapRequest;
    }
}
