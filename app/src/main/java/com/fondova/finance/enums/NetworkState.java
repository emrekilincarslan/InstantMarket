package com.fondova.finance.enums;

import static com.fondova.finance.enums.NetworkState.CONNECTED_TO_SERVER;
import static com.fondova.finance.enums.NetworkState.NOT_CONNECTED_TO_SERVER;
import static com.fondova.finance.enums.NetworkState.NO_INTERNET;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({NO_INTERNET, NOT_CONNECTED_TO_SERVER, CONNECTED_TO_SERVER})
@Retention(RetentionPolicy.SOURCE)
public @interface NetworkState {
    int NO_INTERNET = 0;
    int NOT_CONNECTED_TO_SERVER = 1;
    int CONNECTED_TO_SERVER = 2;
}
