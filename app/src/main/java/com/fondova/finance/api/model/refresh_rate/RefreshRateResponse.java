package com.fondova.finance.api.model.refresh_rate;

import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.base.BaseResponse;

import java.util.List;

public class RefreshRateResponse extends BaseResponse {

    @SerializedName("updateInterval")
    public List<RefreshRateData> updateInterval;


    @Override
    public String toString() {
        return "RefreshRateResponse{" +
                "meta=" + meta +
                ", updateInterval=" + updateInterval +
                '}';
    }
}
