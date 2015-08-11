/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */

package com.cmg.android.task;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.cmg.android.plmobile.R;
import com.cmg.android.plmobile.SplashScreen;
import com.cmg.android.preference.Preference;
import com.cmg.android.util.SimpleAppLog;
import com.cmg.mobile.shared.data.Version;
import com.cmg.mobile.shared.util.ContentGenerater;
import com.cmg.mobile.shared.util.FileHelper;


import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator Hai Lu
 * @Last changed: $LastChangedDate$
 */
public class CheckUpdateAsync extends AsyncTask<Void, Void, Void> {
    public static final String CHANGESimpleAppLog_FILE = "CHANGESimpleAppLog.md";

    private final Context context;
    private final String projectUrl;
    private Version ver;
    private final String currentVersion;
    private final UpdateNewsletterAsync updateNewsletter;

    /**
     * Constructor
     *
     * @param context
     * @param projectUrl
     * @param currentVersion
     */
    public CheckUpdateAsync(Context context, String projectUrl,
                            String currentVersion) {
        this.context = context;
        this.projectUrl = projectUrl;
        this.currentVersion = currentVersion;
        updateNewsletter = new UpdateNewsletterAsync(context);
    }

    @Override
    protected Void doInBackground(Void... params) {

        return null;
    }

    private void downloadChangeSimpleAppLog(String url) throws Exception {
        SimpleAppLog.info("start check changeSimpleAppLog file");
        // String[] files = context.fileList();
        // boolean isExisted = false;
        // if (files != null && files.length > 0) {
        // for (String f : files) {
        // if (f.equals(CHANGESimpleAppLog_FILE)) {
        // isExisted = true;
        // break;
        // }
        // }
        // }
        //
        // if (!isExisted) {

        SimpleAppLog.info("start download changeSimpleAppLog");
        FileHelper.downloadFile(url, context.openFileOutput(CHANGESimpleAppLog_FILE, 0),
                true);
        // }
    }

    @Override
    protected void onPostExecute(Void v) {
        try {
            // ImageLoaderHelper.getImageLoader(context);
            // ImageLoaderHelper.silentLoadNewsletterToDiscCache(newsletters);
        } catch (Exception ex) {
            SimpleAppLog.error("Error when silent load newsletter disc cache", ex);
        }
        // Start new theard to load newsletter
        updateNewsletter.execute();
        try {
            if (ver != null
                    && !currentVersion.equalsIgnoreCase(ver.getVersionName())) {
                SimpleAppLog.info("A new version is available " + ver.getVersionName()
                        + " (Current version " + currentVersion + ")");
                postMessage(SplashScreen.SHOW_CONFIRM_UPDATE);

            } else {
                postMessage(SplashScreen.START_MAIN_ACTIVITY);
            }
        } catch (Exception ex) {
            SimpleAppLog.error("Error when post message to view", ex);
        }
        super.onPostExecute(v);
    }

    /**
     * post message to another view
     *
     * @param action
     */
    public void postMessage(String action) {
        try {
            Intent intent = new Intent(SplashScreen.SPLASH_SCREEN_MESSAGE);
            if (action.equals(SplashScreen.SHOW_CONFIRM_UPDATE)) {
                intent.putExtra(SplashScreen.UPDATE_MESSAGE, ContentGenerater
                        .generateNewVersionMessasge(ver, currentVersion));
                intent.putExtra(SplashScreen.APK_URL,
                        projectUrl + "/" + ver.getApkFile());
            }
            intent.putExtra(SplashScreen.MESSAGE_ACTION, action);
            context.sendBroadcast(intent);
        } catch (Exception ex) {
            SimpleAppLog.error("Error when post message to view", ex);
        }
    }

}
