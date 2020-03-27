package com.fondova.finance.api.model.category;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.vo.Quote;

import java.util.List;

public class CategoriesData {

    @SerializedName("categoryList")
    @Expose
    public List<Category> categoryList = null;
    @SerializedName("symbolList")
    @Expose
    public List<Quote> symbolList = null;


}