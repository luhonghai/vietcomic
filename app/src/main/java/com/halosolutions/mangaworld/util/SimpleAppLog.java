package com.halosolutions.mangaworld.util;

import android.util.Log;

/**
 * Created by cmg on 14/08/15.
 */
public class SimpleAppLog {

    private static final String TAG = "Vietcomic";

    public static void error(String log) {
        Log.e(TAG, log);
    }

    public static void error(String log, Throwable e) {
        Log.e(TAG, log, e);
    }

    public static void error(Throwable e) {
        Log.e(TAG, "", e);
    }

    public static void info(String log) {
        Log.i(TAG, log);
    }

    public static void debug(String log) {
        Log.d(TAG, log);
    }
}
