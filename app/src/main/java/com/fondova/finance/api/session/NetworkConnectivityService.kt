package com.fondova.finance.api.session

interface ConnectivityListener {
    fun onConnectivityChanged(isConnectedToWifi: Boolean, isConnectedToCellular: Boolean)
}

interface NetworkConnectivityService {

    fun isNetworkAvailable(): Boolean

    fun hasWifi(): Boolean

    fun hasCellular(): Boolean

    fun registerConnectivityChangeListener(listener: ConnectivityListener)

    fun unregisterConnectivityChangeListener(listener: ConnectivityListener)

}