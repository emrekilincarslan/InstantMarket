package com.fondova.finance.api.auth

import com.fondova.finance.api.Resource
import com.fondova.finance.api.model.Credentials
import com.fondova.finance.api.model.login.LoginResponse
import com.fondova.finance.api.socket.WebsocketResponseHandler
import com.fondova.finance.api.socket.WebsocketServiceListener

interface AuthenticationResponseListener {
    fun onAuthenticationResponse(response: Resource<LoginResponse>)
}

interface AuthService: WebsocketResponseHandler, WebsocketServiceListener {

    fun authenticate(credentials: Credentials, listener: AuthenticationResponseListener?)
    fun reconnect(listener: AuthenticationResponseListener?)
}