package com.fondova.finance.api.model.optionsSeries;

import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.optionsSnap.OptionsSnap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionsSeriesData {

    private static final List<String> options = Arrays.asList("", "");
    private static final Integer OPTION_SERIES_STRIKE_COUNT = 21;

    @SerializedName("futRoot")
    public String futRoot;

    @SerializedName("putRoot")
    public String putRoot;

    @SerializedName("callRoot")
    public String callRoot;

    @SerializedName("maturity")
    public String maturity;

    @SerializedName("strikeCount")
    public Integer strikeCount;

    @SerializedName("underlyingFields")
    public List<String> underlyingFields;

    @SerializedName("optionFields")
    public List<String> optionFields;


    public OptionsSeriesData(OptionsSnap optionsSnap) {
        this.futRoot = optionsSnap.futRoot;
        this.putRoot = optionsSnap.putRoot;
        this.callRoot = optionsSnap.callRoot;
        this.maturity = optionsSnap.maturity;
        this.strikeCount = OPTION_SERIES_STRIKE_COUNT;
        this.underlyingFields = new ArrayList<>();
        this.optionFields = options;
    }
}