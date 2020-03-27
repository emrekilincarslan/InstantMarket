package com.fondova.finance.api.model.quote;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.fondova.finance.vo.QuoteValue;

public class QuoteData {
    @SerializedName("expression")
    public String expression;

    @SerializedName("priceFormat")
    public String priceFormat;

    @SerializedName("timeFormat")
    public String timeFormat;

    @SerializedName("symbolFormat")
    public String symbolFormat;

    @SerializedName("fields")
    public String[] fields;

    public QuoteData(String... expressions) {
        this.expression = TextUtils.join(", ", expressions);
        this.priceFormat = "text";
        this.timeFormat = "text";
        this.symbolFormat = "dict";
        this.fields = QuoteValue.WATCH_FIELDS;
    }

}
