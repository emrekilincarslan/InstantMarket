package com.fondova.finance.api.model.news;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CategoryArticle {

    @SerializedName("storyId")
    @Expose
    public String storyId;

    @SerializedName("datetime")
    @Expose
    public String datetime;

    @SerializedName("title")
    @Expose
    public String title;

    @SerializedName("refreshType")
    @Expose
    public String refreshType;
}
