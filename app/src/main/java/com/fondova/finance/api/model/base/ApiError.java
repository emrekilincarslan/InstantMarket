package com.fondova.finance.api.model.base;

import com.google.gson.annotations.SerializedName;

public class ApiError {
    @SerializedName("code")
    public String code;

    @SerializedName("detail")
    public String detail;
}
