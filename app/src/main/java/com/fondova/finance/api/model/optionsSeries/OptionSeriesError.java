package com.fondova.finance.api.model.optionsSeries;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OptionSeriesError {
    @SerializedName("code")
    @Expose
    public String code;

    @SerializedName("detail")
    @Expose
    public String detail;
}
