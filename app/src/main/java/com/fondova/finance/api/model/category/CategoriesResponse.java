package com.fondova.finance.api.model.category;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CategoriesResponse {

    @SerializedName("data")
    @Expose
    public CategoriesData categoriesData;

}