package com.halosolutions.vietcomic;

import android.app.Application;

import com.rey.material.app.ThemeManager;

/**
 * Created by luhonghai on 5/22/2015.
 */
public class MainApplication extends Application {

    public static final int DEFAULT_THEME = 0;

    @Override public void onCreate() {
        super.onCreate();
        ThemeManager.init(this, 2, DEFAULT_THEME, null);
    }
}
