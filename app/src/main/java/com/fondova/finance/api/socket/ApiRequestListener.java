package com.fondova.finance.api.socket;

public interface ApiRequestListener<T> {

    void onResponse(T item);
}
