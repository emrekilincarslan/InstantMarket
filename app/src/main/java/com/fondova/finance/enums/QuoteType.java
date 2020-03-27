package com.fondova.finance.enums;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.fondova.finance.enums.QuoteType.EXPRESSION;
import static com.fondova.finance.enums.QuoteType.LABEL;
import static com.fondova.finance.enums.QuoteType.SYMBOL;

@IntDef({LABEL, SYMBOL, EXPRESSION})
@Retention(RetentionPolicy.SOURCE)
public @interface QuoteType {
    int SYMBOL = 0;
    int LABEL = 1;
    int EXPRESSION = 2;
}
