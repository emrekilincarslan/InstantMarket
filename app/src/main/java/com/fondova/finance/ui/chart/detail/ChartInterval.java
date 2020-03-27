package com.fondova.finance.ui.chart.detail;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.fondova.finance.ui.chart.detail.ChartInterval.DAY;
import static com.fondova.finance.ui.chart.detail.ChartInterval.MIN_30;
import static com.fondova.finance.ui.chart.detail.ChartInterval.MIN_5;
import static com.fondova.finance.ui.chart.detail.ChartInterval.MIN_60;
import static com.fondova.finance.ui.chart.detail.ChartInterval.MONTH;
import static com.fondova.finance.ui.chart.detail.ChartInterval.WEEK;


@IntDef({MIN_5, MIN_30, MIN_60, DAY, WEEK, MONTH})
@Retention(RetentionPolicy.SOURCE)
public @interface ChartInterval {
    int MIN_5 = 5;
    int MIN_30 = 30;
    int MIN_60 = 60;
    int DAY = 24 * MIN_60;
    int WEEK = 7 * DAY;
    int MONTH = 30 * DAY;
}
