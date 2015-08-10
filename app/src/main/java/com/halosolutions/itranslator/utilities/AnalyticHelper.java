package com.halosolutions.itranslator.utilities;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.halosolutions.itranslator.R;

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
            e.printStackTrace();
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
