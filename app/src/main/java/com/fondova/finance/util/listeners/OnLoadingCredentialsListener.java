package com.fondova.finance.util.listeners;


import com.fondova.finance.api.model.Credentials;

public interface OnLoadingCredentialsListener {
    void onDone(Credentials credentials);
}
