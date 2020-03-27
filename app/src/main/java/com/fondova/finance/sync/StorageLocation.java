package com.fondova.finance.sync;

import static com.fondova.finance.sync.StorageLocation.CLOUD;
import static com.fondova.finance.sync.StorageLocation.LOCAL;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({LOCAL, CLOUD})
@Retention(RetentionPolicy.SOURCE)
public @interface StorageLocation {
    int LOCAL = 0;
    int CLOUD = 1;
}