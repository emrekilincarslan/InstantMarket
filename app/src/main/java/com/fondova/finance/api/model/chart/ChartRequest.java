package com.fondova.finance.api.model.chart;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.base.MetaRequest;

public class ChartRequest {

    private static final String MINUTE = "MINUTE";
    private static final String DAY = "DAY";
    private static final String WEEK = "WEEK";
    private static final String MONTH = "MONTH";

    @SerializedName("meta")
    @Expose
    public MetaRequest meta;

    @SerializedName("data")
    @Expose
    public ChartReqData data;

    protected static ChartRequest create(String reqId, String expression, int intervalCount, String command, int limit) {
        MetaRequest meta = new MetaRequest();
        meta.command = command;
        meta.requestId = reqId;

        ChartReqData data = new ChartReqData();
        data.expression = expression;
        data.priceFormat = "dict";
        data.timeFormat = "text";
        data.symbolFormat = "text";
        switch (intervalCount) {
            case 5:
            case 30:
                data.interval = MINUTE;
                data.intervalCount = intervalCount;
                break;
            case 60:
                data.interval = MINUTE;
                data.intervalCount = 60;
                break;
            case 60 * 24:
                data.interval = DAY;
                data.intervalCount = 1;
                break;
            case 60 * 24 * 7:
                data.interval = WEEK;
                data.intervalCount = 1;
                break;
            default:
                data.interval = MONTH;
                data.intervalCount = 1;
                break;
        }

        data.limit = limit;

        ChartRequest request = new ChartRequest();
        request.meta = meta;
        request.data = data;

        return request;
    }

    private static class ChartReqData {
        @SerializedName("expression") String expression;

        @SerializedName("limit") Integer limit;

        @SerializedName("interval") String interval;

        @SerializedName("intervalCount") Integer intervalCount;

        @SerializedName("priceFormat") String priceFormat;

        @SerializedName("timeFormat") String timeFormat;

        @SerializedName("symbolFormat") String symbolFormat;
    }
}
