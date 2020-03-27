package com.fondova.finance.api.model.news;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NewsSnap {

    @SerializedName("body")
    @Expose
    public String body;
}
