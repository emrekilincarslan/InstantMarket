package com.fondova.finance.api.model.news;

import com.google.gson.annotations.SerializedName;

public class NewsPageSnapData {

    @SerializedName("storyId")
    public String storyId;

    public NewsPageSnapData(String storyId) {
        this.storyId = storyId;
    }
}
