package com.fondova.finance.api;

public enum ConnectionStatus {
    CONNECTED,

    USER_LOGGING_IN,
    CONNECTING,

    LOGIN_FAILURE,
    DISCONNECTED,
    CONNECTION_LOST,
    FORCE_LOGOUT,
    USER_LOGGED_OUT
}