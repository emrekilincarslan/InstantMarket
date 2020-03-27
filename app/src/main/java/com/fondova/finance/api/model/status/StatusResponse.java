package com.fondova.finance.api.model.status;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.base.BaseResponse;

import java.util.List;

public class StatusResponse extends BaseResponse {

    public static final String COMMAND = "status";

    @SerializedName("data")
    @Expose
    public List<Status> data;
}
