package com.fondova.finance.api.model.base;

import android.support.annotation.StringDef;

import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static com.fondova.finance.api.model.base.ApiErrorResponse.Codes.NOT_READY;

public class ApiErrorResponse {


    @Retention(RetentionPolicy.SOURCE)
    @StringDef({NOT_READY})
    public @interface Codes {
        String NOT_READY = "Not Ready";
    }

    @SerializedName("meta")
    public MetaResponse meta;

    @SerializedName("errors")
    public List<ApiError> errors;
}
