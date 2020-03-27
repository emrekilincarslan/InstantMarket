package com.fondova.finance.api.session

enum class SessionStatus {
    connecting,
    connected,
    disconnected,
    disconnectedNoRetry,
    userLoggingIn,
    connectionFailed,
    connectionLost,
    seatbump,
    loginFailure,
    loggedOut,
    userLoggedOut;

    fun shouldReconnect(): Boolean {
        return when (this) {
            connecting, connectionLost, connectionFailed, loggedOut, disconnected -> true
            else -> false
        }
    }
}