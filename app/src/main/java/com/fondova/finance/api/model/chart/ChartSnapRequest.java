package com.fondova.finance.api.model.chart;

public class ChartSnapRequest extends ChartRequest {

    public static final String COMMAND_CHART_SNAP = "ChartSnap";

    private static final int LIMIT = 1500;

    public static ChartRequest create(String reqId, String expression, int intervalCount) {
        return create(reqId, expression, intervalCount, COMMAND_CHART_SNAP, LIMIT);
    }
}
