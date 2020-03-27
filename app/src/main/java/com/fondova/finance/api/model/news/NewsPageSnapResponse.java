package com.fondova.finance.api.model.news;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.base.BaseResponse;

import java.util.List;

public class NewsPageSnapResponse extends BaseResponse {

    @SerializedName("data")
    @Expose
    public List<NewsSnap> data;

}