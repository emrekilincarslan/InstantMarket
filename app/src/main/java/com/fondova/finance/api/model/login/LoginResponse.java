package com.fondova.finance.api.model.login;

import com.google.gson.annotations.SerializedName;
import com.fondova.finance.api.model.base.ApiError;
import com.fondova.finance.api.model.base.BaseResponse;
import com.fondova.finance.vo.User;

import java.util.List;

public class LoginResponse extends BaseResponse {

    @SerializedName("data")
    public List<User> user;

    @SerializedName("errors")
    public List<ApiError> errors;

}
