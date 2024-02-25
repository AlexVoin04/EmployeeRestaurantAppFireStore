package com.example.employeerestaurantappfirestore.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

public class NetworkUtils {
    public static void setupNetworkMonitoring(Activity activity, Runnable onAvailableAction, Runnable onLostAction) {
        NetworkMonitor networkMonitor = new NetworkMonitor();
        networkMonitor.registerNetworkCallback(
                activity,
                () -> {
                    if (onAvailableAction != null) {
                        onAvailableAction.run();
                    }
                },
                () -> {
                    if (onLostAction != null) {
                        onLostAction.run();
                    }
                }
        );
        activity.getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                networkMonitor.registerNetworkCallback(
                        activity,
                        () -> {
                            if (onAvailableAction != null) {
                                onAvailableAction.run();
                            }
                        },
                        () -> {
                            if (onLostAction != null) {
                                onLostAction.run();
                            }
                        }
                );
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                networkMonitor.unregisterNetworkCallback(activity);
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                Log.d("Activity", "Destroyed");
                networkMonitor.unregisterNetworkCallback(activity);
            }
        });
    }

    public static boolean isNetworkAvailable(Context context) {
        NetworkMonitor networkMonitor = new NetworkMonitor();
        return networkMonitor.isNetworkAvailable(context);
    }

    public static boolean checkNetworkAndShowSnackbar(Activity activity) {
        if (!isNetworkAvailable(activity)) {
            Snackbar.make(activity.findViewById(android.R.id.content), "Отсутствует соединение сети", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
