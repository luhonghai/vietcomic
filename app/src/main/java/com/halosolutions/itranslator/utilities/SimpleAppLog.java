package com.halosolutions.itranslator.utilities;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

/**
 * Created by luhonghai on 2/28/15.
 * Simple log
 */
public class SimpleAppLog {

    private static final String TAG = "Global Translator";

    public static void info(String log) {
        Log.i(TAG, log);
        Crashlytics.log(log);
    }

    public static void debug(String log) {
        Log.d(TAG,log);
    }

    public static void error(String log) {
        error(log, null);
    }

    public static void error(String log, Throwable throwable) {
        Crashlytics.log(Log.ERROR, TAG, log);
        if (throwable == null) {
            Log.e(TAG, log);
        } else {
            Log.e(TAG, log, throwable);
            throwable.printStackTrace();
        }
    }
}
