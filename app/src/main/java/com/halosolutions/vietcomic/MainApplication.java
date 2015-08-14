package com.halosolutions.vietcomic;

import android.app.Application;

import com.rey.material.app.ThemeManager;

/**
 * Created by luhonghai on 5/22/2015.
 */
public class MainApplication extends Application {

    @Override public void onCreate() {
        super.onCreate();
        ThemeManager.init(this, 2, 0, null);
    }
}
