package com.fondova.finance.enums;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.fondova.finance.enums.RefreshType.HIST;
import static com.fondova.finance.enums.RefreshType.REAL_TIME;

@StringDef({REAL_TIME, HIST})
@Retention(RetentionPolicy.SOURCE)
public @interface RefreshType {
    String REAL_TIME = "realtime";
    String HIST = "hist";
}
