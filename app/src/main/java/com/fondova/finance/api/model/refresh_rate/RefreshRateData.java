package com.fondova.finance.api.model.refresh_rate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

class RefreshRateData {

    @SerializedName("updateInterval")
    @Expose
    public int updateInterval;


    @Override
    public String toString() {
        return "RefreshRateData{" +
                "updateInterval=" + updateInterval +
                '}';
    }
}