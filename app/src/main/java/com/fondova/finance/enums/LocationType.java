package com.fondova.finance.enums;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.fondova.finance.enums.LocationType.ALL;
import static com.fondova.finance.enums.LocationType.BODY;
import static com.fondova.finance.enums.LocationType.CATEGORY;
import static com.fondova.finance.enums.LocationType.TITLE;

@StringDef({ALL, TITLE, BODY, CATEGORY})
@Retention(RetentionPolicy.SOURCE)
public @interface LocationType {
    String ALL = "all";
    String TITLE = "title";
    String BODY = "body";
    String CATEGORY = "category";
}
