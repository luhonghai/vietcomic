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
import com.cmg.mobile.shared.data.Version;
import com.cmg.mobile.shared.util.ContentGenerater;
import com.cmg.mobile.shared.util.FileHelper;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

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
    private final static Logger log = Logger.getLogger(CheckUpdateAsync.class);
    public static final String CHANGELOG_FILE = "CHANGELOG.md";

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
        try {
            // Init preference
            try {
                if (!updateNewsletter.checkExists()) {
                    log.info("Load newsletter the 1st time");
                    updateNewsletter.doSync();
                }
            } catch (Exception ex) {
                log.error("Cannot sync newsletters", ex);
            }
            try {
                log.info("Start init preferences");
                Preference.init(context);
            } catch (Exception ex) {
                log.error("Cannot init preferences", ex);
            }

            if (context.getResources().getBoolean(R.bool.allow_update)) {
                URL url = new URL(projectUrl + "/VERSION");
                // HttpURLConnection con = (HttpURLConnection) new URL(url)
                // .openConnection();

                HttpURLConnection con = null;
                if (url.getProtocol().toLowerCase().equals("https")) {
                    FileHelper.trustAllHosts();
                    HttpsURLConnection https = (HttpsURLConnection) url
                            .openConnection();
                    https.setHostnameVerifier(FileHelper.DO_NOT_VERIFY);
                    con = https;
                } else {
                    con = (HttpURLConnection) url.openConnection();
                }
                con.setConnectTimeout(FileHelper.CONNECTION_TIMEOUT);
                con.setRequestMethod("GET");
                con.connect();
                int code = con.getResponseCode();
                if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                    log.error("Page " + url + " not found");
                } else if (code == HttpURLConnection.HTTP_OK) {
                    ObjectMapper mapper = new ObjectMapper();
                    ver = mapper.readValue(con.getInputStream(), Version.class);
                } else {
                    log.error("Page " + url + " not found. Response code: "
                            + code);
                }
                if (ver != null) {
                    downloadChangelog(projectUrl + "/" + ver.getChangeLog());
                } else {
                    downloadChangelog(projectUrl + "/CHANGELOG.md");
                }
            } else {
                downloadChangelog(projectUrl + "/CHANGELOG.md");
            }

        } catch (Exception e) {
            log.error("Cannot update application", e);
        }
        return null;
    }

    private void downloadChangelog(String url) throws Exception {
        log.info("start check changelog file");
        // String[] files = context.fileList();
        // boolean isExisted = false;
        // if (files != null && files.length > 0) {
        // for (String f : files) {
        // if (f.equals(CHANGELOG_FILE)) {
        // isExisted = true;
        // break;
        // }
        // }
        // }
        //
        // if (!isExisted) {

        log.info("start download changelog");
        FileHelper.downloadFile(url, context.openFileOutput(CHANGELOG_FILE, 0),
                true);
        // }
    }

    @Override
    protected void onPostExecute(Void v) {
        try {
            // ImageLoaderHelper.getImageLoader(context);
            // ImageLoaderHelper.silentLoadNewsletterToDiscCache(newsletters);
        } catch (Exception ex) {
            log.error("Error when silent load newsletter disc cache", ex);
        }
        // Start new theard to load newsletter
        updateNewsletter.execute();
        try {
            if (ver != null
                    && !currentVersion.equalsIgnoreCase(ver.getVersionName())) {
                log.info("A new version is available " + ver.getVersionName()
                        + " (Current version " + currentVersion + ")");
                postMessage(SplashScreen.SHOW_CONFIRM_UPDATE);

            } else {
                postMessage(SplashScreen.START_MAIN_ACTIVITY);
            }
        } catch (Exception ex) {
            log.error("Error when post message to view", ex);
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
            log.error("Error when post message to view", ex);
        }
    }

}
