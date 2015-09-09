package com.halosolutions.mangaworld.util;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.halosolutions.mangaworld.R;

/**
 * Created by longnguyen on 4/17/15.
 *
 */
public class AnalyticHelper {

    public static synchronized Tracker getTracker(Context context) {
        try {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
            Tracker t = analytics.newTracker(R.xml.global_tracker);
            t.enableAdvertisingIdCollection(true);
            t.enableAutoActivityTracking(true);
            t.enableExceptionReporting(true);
            return t;
        } catch (Exception e) {
            SimpleAppLog.error("Could not get google analytic tracker", e);
            return null;
        }
    }


    public static synchronized void sendEvent(Context context, String category, String action, String label) {
        try {
            Tracker t = getTracker(context);
            if (t != null)
                t.send(new HitBuilders.EventBuilder()
                        .setCategory(category)
                        .setAction(action)
                        .setLabel(label)
                        .build());
        } catch (Exception e) {

        }
    }

    public static synchronized void sendEvent(Context context, String category, String action, String label, long value) {
        try {
            Tracker t = getTracker(context);
            if (t != null)
                t.send(new HitBuilders.EventBuilder()
                        .setCategory(category)
                        .setAction(action)
                        .setLabel(label)
                        .setValue(value)
                        .build());
        } catch (Exception e) {

        }
    }

}
