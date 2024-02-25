package com.example.employeerestaurantappfirestore.utils;

import android.content.Context;
import android.os.PowerManager;

public class WakeLockManager {
    private static PowerManager.WakeLock wakeLock;

    public static void acquire(Context context) {
        if (wakeLock == null) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MyApp:MyWakeLockTag");
        }

        if (!wakeLock.isHeld()) {
            wakeLock.acquire(60 * 60 * 1000L );
        }
    }

    public static void release() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}
