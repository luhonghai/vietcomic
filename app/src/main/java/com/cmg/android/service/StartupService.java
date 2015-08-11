/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */

package com.cmg.android.service;

import android.content.Context;
import android.preference.PreferenceManager;

import com.cmg.android.plmobile.R;
import com.cmg.android.task.CheckUpdateAsync;
import com.cmg.android.util.Utilities;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator Hai Lu
 * @Last changed: $LastChangedDate$
 */

public class StartupService {

    private final Context context;

    private final boolean isOnline;

    private final String currentVersion;

    /**
     * Constructor
     *
     * @param context
     * @param isOnline
     * @param currentVersion
     */
    public StartupService(Context context, boolean isOnline,
                          String currentVersion) {
        this.context = context;
        this.isOnline = isOnline;
        this.currentVersion = currentVersion;

    }

    /**
     * check update new version
     *
     * @param context
     */
    void checkUpdate(Context context) {
        CheckUpdateAsync checkUpdate = new CheckUpdateAsync(context, context
                .getResources().getString(R.string.update_server),
                currentVersion);
        checkUpdate.execute();
    }

    /**
     * clear cache
     */
    public void recycle() {

    }

    /**
     * initial data
     */
    public void init() {
        // configure log4j
        Utilities.configureLog4J(context);
        // set default value
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
        // Start check for update
        if (isOnline) {
            checkUpdate(context);
        }
    }
}
