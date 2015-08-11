/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */

package com.cmg.android.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator HaiLu
 * @Last changed: $LastChangedDate$
 */
public class Utilities {
    public static final String LOG_FOLDER = "logs";
    public static final String LOG_FILE = "app.log";

    /**
     * Get application version name
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        PackageInfo pi = getPackageInfo(context);
        if (pi == null) {
            return "";
        }
        return pi.versionName;
    }

    /**
     * get package information
     *
     * @param context
     * @return
     */
    public static PackageInfo getPackageInfo(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            SimpleAppLog.error("Cannot get package info", e);
        }
        return null;
    }

    /**
     * configure Log4J
     *
     * @param context
     */
    public static void configureLog4J(Context context) {

    }
}
