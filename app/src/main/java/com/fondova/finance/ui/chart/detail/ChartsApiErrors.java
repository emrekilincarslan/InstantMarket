package com.fondova.finance.ui.chart.detail;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.fondova.finance.ui.chart.detail.ChartsApiErrors.API_ERROR_UNKNOWN_EXPRESSION;

@Retention(RetentionPolicy.SOURCE)
@StringDef({API_ERROR_UNKNOWN_EXPRESSION})
@interface ChartsApiErrors {
    String API_ERROR_UNKNOWN_EXPRESSION = "Unknown failure. Unknown expression value";
}
