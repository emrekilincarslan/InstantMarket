package com.fondova.finance.api.model.chart;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.vo.ChartData;

import java.util.Collections;
import java.util.List;

public class ChartWatchResponse {

    @SerializedName("meta")
    @Expose
    public MetaChartWatch meta;

    @SerializedName("data")
    @Expose
    public List<ChartData> data = null;


    @Override
    public String toString() {
        return "ChartWatchResponse{" +
                "meta=" + meta +
                ", data=" + data +
                '}';
    }

    public static ChartWatchResponse empty() {
        ChartWatchResponse response = new ChartWatchResponse();
        response.data = Collections.emptyList();
        return response;
    }
}
