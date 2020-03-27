package com.fondova.finance.api.model.optionsSnap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OptionsSnap {
    @SerializedName("futRoot")
    @Expose
    public String futRoot;

    @SerializedName("callRoot")
    @Expose
    public String callRoot;

    @SerializedName("putRoot")
    @Expose
    public String putRoot;

    @SerializedName("displayMaturity")
    @Expose
    public String displayMaturity;

    @SerializedName("maturity")
    @Expose
    public String maturity;

    @SerializedName("default")
    @Expose
    public Boolean defaultVal;
}
