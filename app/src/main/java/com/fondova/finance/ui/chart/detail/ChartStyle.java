package com.fondova.finance.ui.chart.detail;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.fondova.finance.ui.chart.detail.ChartStyle.BAR;
import static com.fondova.finance.ui.chart.detail.ChartStyle.CANDLESTICK;
import static com.fondova.finance.ui.chart.detail.ChartStyle.LINE;


@IntDef({BAR, LINE, CANDLESTICK})
@Retention(RetentionPolicy.SOURCE)
public @interface ChartStyle {
    int BAR = 0;
    int LINE = 1;
    int CANDLESTICK = 2;
}
