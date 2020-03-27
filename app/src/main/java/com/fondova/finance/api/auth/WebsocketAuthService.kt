package com.fondova.finance.api.auth

import android.util.Log
import com.google.gson.Gson
import com.fondova.finance.AppExecutors
import com.fondova.finance.api.Resource
import com.fondova.finance.api.model.Credentials
import com.fondova.finance.api.model.base.MetaResponse
import com.fondova.finance.api.model.login.LoginRequest
import com.fondova.finance.api.model.login.LoginResponse
import com.fondova.finance.api.socket.WebsocketService
import com.fondova.finance.persistance.AppStorageInterface
import com.fondova.finance.repo.TextsRepository
import java.io.IOException

class WebsocketAuthService(val websocketService: WebsocketService,
                           val appExecutors: AppExecutors,
                           val appStorage: AppStorageInterface,
                           val textsRepository: TextsRepository): AuthService {

    val loginRequestId = "C974AAA9-8796-425C-98E7-E8ADEF7CCD71"

    private var storedCredentials: Credentials? = null

    private var loginListener: AuthenticationResponseListener? = null

    init {
        websocketService.addListener(this)
    }

    override fun onErrorMessage(message: String) {
        // Don't care
    }

    override fun onConnected(websocketService: WebsocketService) {
        val username = storedCredentials?.username?.toLowerCase() ?: ""
        val password = storedCredentials?.password ?: ""
        val refreshRate = appStorage.getRefreshRateAsInt().toDouble()
        val request = LoginRequest.create(username,
                password,
                textsRepository.appNameForApiCalls(),
                textsRepository.appVersion(),
                refreshRate)
        request.meta.requestId = loginRequestId
        val json = Gson().toJson(request)

        appExecutors.networkIO().execute {
            websocketService.sendMessage(json)
        }
    }

    override fun onDisconnected(websocketService: WebsocketService, code: Int, reason: String, closedByServer: Boolean) {
        // Don't care
    }

    override fun onSocketError(websocketService: WebsocketService, exception: IOException) {
        // Don't care
    }

    override fun authenticate(credentials: Credentials, listener: AuthenticationResponseListener?) {
        Log.i("AuthService", "authenticating")
        if (credentials.username.isNullOrEmpty() || credentials.password.isNullOrEmpty()) {
            listener?.onAuthenticationResponse(createMissingCredentialsResponse())
            return
        }
        storedCredentials = credentials
        loginListener = listener
        websocketService.connect()
    }

    override fun reconnect(listener: AuthenticationResponseListener?) {
        val credentials = storedCredentials
        if (credentials == null) {
            listener?.onAuthenticationResponse(createMissingCredentialsResponse())
            return
        }
        authenticate(credentials, listener)
    }

    fun createMissingCredentialsResponse(): Resource<LoginResponse> {
        val response = LoginResponse()
        response.meta = MetaResponse()
        response.meta.status = 400
        return Resource.error("Login Failed", "Missing Credentials")
    }

    override fun handleMessage(message: String): Boolean {
        if (message.contains(loginRequestId)) {
            val response = Gson().fromJson(message, LoginResponse::class.java)
            if (isSuccessResponse(response)) {
                appExecutors.mainThread().execute {
                    loginListener?.onAuthenticationResponse(Resource.success(response))
                }
                val obtions =  response.user[0].features.optionsseriesview;
                if (obtions) {
                    Log.d("vds", "vddsvdsvds")
                }
            } else {
                val error = response?.errors?.firstOrNull()
                val errorTitle = error?.code
                val errorMessage = error?.detail
                appExecutors.mainThread().execute {
                    loginListener?.onAuthenticationResponse(Resource.error(errorTitle, errorMessage))
                }
            }
            return true
        }
        return false
    }

    fun isSuccessResponse(response: LoginResponse?): Boolean {
        val status = response?.meta?.status ?: 500
        return status in 200..299
    }

}