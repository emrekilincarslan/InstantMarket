package com.fondova.finance.api.model.chart;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.base.MetaResponse;

import java.util.List;

public class MetaChartWatch extends MetaResponse {

    @SerializedName("upToDate")
    @Expose
    public Boolean upToDate;
    @SerializedName("symbols")
    @Expose
    public List<String> symbols = null;
    @SerializedName("expression")
    @Expose
    public String expression;
    @SerializedName("expressionDesc")
    @Expose
    public String expressionDesc;
}
