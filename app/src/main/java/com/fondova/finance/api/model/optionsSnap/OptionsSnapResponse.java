package com.fondova.finance.api.model.optionsSnap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OptionsSnapResponse {
    @SerializedName("data")
    @Expose
    public List<OptionsSnap> data;
}