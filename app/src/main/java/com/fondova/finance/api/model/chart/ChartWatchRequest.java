package com.fondova.finance.api.model.chart;

public class ChartWatchRequest extends ChartRequest {

    public static final String COMMAND_CHART_WATCH = "ChartWatch";

    private static final int LIMIT = 1500;

    public static ChartRequest create(String reqId, String expression, int intervalCount) {
        return create(reqId, expression, intervalCount, COMMAND_CHART_WATCH, LIMIT);
    }
}
