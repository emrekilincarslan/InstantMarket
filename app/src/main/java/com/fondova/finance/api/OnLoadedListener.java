package com.fondova.finance.api;


public interface OnLoadedListener {
    void onDone(String response);
    void onError(String error);
}
