package com.fondova.finance.util;


import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.provider.Settings;

import com.fondova.finance.App;
import com.fondova.finance.AppExecutors;
import com.fondova.finance.api.session.ConnectivityListener;
import com.fondova.finance.api.session.NetworkConnectivityService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class InternetConnectivityUtils implements NetworkConnectivityService {

    private final Context context;
    private final AppExecutors appExecutors;
    private ConnectivityManager.NetworkCallback networkCallback;
    private final List<ConnectivityListener> listeners = new ArrayList<>();
    private boolean hasWifi = false;
    private boolean hasCellular = false;


    @Inject
    public InternetConnectivityUtils(App context, AppExecutors appExecutors) {
        this.context = context;
        this.appExecutors = appExecutors;
    }


    // ---------------------------------------------------------------------------------------------
    // NetworkConnectivityService
    // ---------------------------------------------------------------------------------------------
    @Override
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = getConnectivityManager();
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public boolean hasWifi() {
        return hasWifi;
    }

    @Override
    public boolean hasCellular() {
        return hasCellular;
    }

    @Override
    public void registerConnectivityChangeListener(ConnectivityListener listener) {
        listeners.add(listener);
        if (!isRegisteredForConnectivityChanges()) {
            registerConnectivityAction();
        }
    }

    @Override
    public void unregisterConnectivityChangeListener(ConnectivityListener listener) {
        listeners.remove(listener);
        if (listeners.size() == 0) {
            unregisterConnectivityAction();
        }
    }


    // ---------------------------------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------------------------------
    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private boolean isRegisteredForConnectivityChanges() {
        return networkCallback != null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void unregisterConnectivityAction() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        connectivityManager.unregisterNetworkCallback(networkCallback);
        networkCallback = null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void registerConnectivityAction() {
        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(builder.build(), initNetworkCallback());

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ConnectivityManager.NetworkCallback initNetworkCallback() {
        networkCallback =
                new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        ConnectivityManager connectivityManager = getConnectivityManager();
                        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            hasWifi = true;
                        }
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            hasCellular = true;
                        }
                        notifyConnectivityChanged();
                    }

                    @Override
                    public void onLost(Network network) {
                        ConnectivityManager connectivityManager = getConnectivityManager();
                        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                        if (capabilities == null) {
                            hasWifi = false;
                            hasCellular = false;
                            notifyConnectivityChanged();
                            return;
                        }

                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            hasWifi = false;
                        }
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            hasCellular = false;
                        }
                        if(isAirplaneModeOn(context) || !isNetworkAvailable()) {
                            hasWifi = false;
                            hasCellular = false;
                        }
                        notifyConnectivityChanged();
                    }
                };
        return networkCallback;
    }

    private static boolean isAirplaneModeOn(Context context) {

        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

    }

    private void notifyConnectivityChanged() {
        appExecutors.mainThread().execute(() -> {
            for (ConnectivityListener listener : listeners) {
                listener.onConnectivityChanged(hasWifi, hasCellular);
            }
        });
    }


    // ---------------------------------------------------------------------------------------------
    // Callback
    // ---------------------------------------------------------------------------------------------
}
