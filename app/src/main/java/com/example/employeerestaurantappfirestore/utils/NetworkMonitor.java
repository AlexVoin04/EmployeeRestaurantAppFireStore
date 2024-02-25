package com.example.employeerestaurantappfirestore.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.annotation.NonNull;

public class NetworkMonitor {
    private ConnectivityManager.NetworkCallback networkCallback;

    public void registerNetworkCallback(Context context, Runnable onAvailableAction, Runnable onLostAction) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    // Сеть доступна, выполните необходимые действия
                    Log.d("Network", "Network is available");
                    if (onAvailableAction != null) {
                        onAvailableAction.run();
                    }
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    // Сеть отсутствует, выполните необходимые действия
                    Log.w("Network", "Network is not available");
                    if (onLostAction != null) {
                        onLostAction.run();
                    }
                }
            };

            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        }
    }

    public void unregisterNetworkCallback(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        return false;
    }
}
