package com.cmg.android.util;

import android.util.Log;

/**
 * Created by cmg on 11/08/15.
 */
public class SimpleAppLog {

    private static final String TAG = "Vietcomic";

    public static void info(String info) {
        Log.i(TAG, info);
    }

    public static void debug(String debug) {
        Log.d(TAG, debug);
    }

    public static void error(String error) {
        Log.e(TAG, error);
    }

    public static void error(String error, Throwable e) {
        Log.e(TAG, error, e);
    }

    public static void error(Throwable e) {
        Log.e(TAG, "", e);
    }
}
