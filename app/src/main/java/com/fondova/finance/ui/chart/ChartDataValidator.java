package com.fondova.finance.ui.chart;


import android.support.annotation.Nullable;

import com.fondova.finance.vo.ChartData;

public class ChartDataValidator {

    public static boolean isValidChartData(@Nullable final ChartData toBeChecked) {
        return toBeChecked != null && !toBeChecked.isNull && toBeChecked.open != null && toBeChecked.close != null && toBeChecked.high != null && toBeChecked.low != null && toBeChecked.dateTime != null;
    }
}
