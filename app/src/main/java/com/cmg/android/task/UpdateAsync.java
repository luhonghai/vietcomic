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

import com.cmg.android.plmobile.SplashScreen;
import com.cmg.android.util.FileUtils;
import com.cmg.mobile.shared.util.FileHelper;
import com.cmg.mobile.shared.util.StringUtils;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class UpdateAsync extends AsyncTask<Void, Void, Void> {
    private static Logger log = Logger.getLogger(UpdateAsync.class);
    private static final int BUFFER_LENGTH = 1024;
    private final Context context;
    private final String apkUrl;
    private String apkPath;
    private int progress = -1;
    private boolean isDone = false;
    private Intent intent;

    /**
     * Constructor
     *
     * @param context
     * @param apkUrl
     */
    public UpdateAsync(Context context, String apkUrl) {
        this.context = context;
        this.apkUrl = apkUrl;
        intent = new Intent(SplashScreen.SPLASH_SCREEN_MESSAGE);
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Void doInBackground(Void... params) {
        update(apkUrl);
        return null;
    }

    /**
     * update progress bar
     */
    private void postProgress() {
        try {
            intent.putExtra(SplashScreen.MESSAGE_ACTION,
                    SplashScreen.UPDATE_PROGRESS);
            intent.putExtra(SplashScreen.PROGRESS_VALUE, progress);
            context.sendBroadcast(intent);
        } catch (Exception ex) {
            log.error("Error when post progress to view", ex);
        }
    }

    @Override
    protected void onPostExecute(Void v) {
        try {
            intent.getExtras().clear();
            if (isDone) {
                intent.putExtra(SplashScreen.MESSAGE_ACTION,
                        SplashScreen.START_INSTALL_UPDATE);
                intent.putExtra(SplashScreen.APK_PATH, apkPath);
                context.sendBroadcast(intent);
            } else {
                intent.putExtra(SplashScreen.MESSAGE_ACTION,
                        SplashScreen.START_MAIN_ACTIVITY);
                context.sendBroadcast(intent);
            }
        } catch (Exception ex) {
            log.error("Error when post action to view", ex);
        }
        super.onPostExecute(v);
    }

    /**
     * download new apk file
     *
     * @param apkUrl
     */
    public void update(String apkUrl) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            String fileName = StringUtils.getFileName(apkUrl);
            URL url = new URL(apkUrl);
            //URLConnection connection = url.openConnection();
            HttpURLConnection connection = null;
            if (url.getProtocol().toLowerCase().equals("https")) {
                FileHelper.trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url
                        .openConnection();
                https.setHostnameVerifier(FileHelper.DO_NOT_VERIFY);
                connection = https;
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }


            connection.connect();
            int lenghtOfFile = connection.getContentLength();

            String path = FileUtils.getDownloadFolder();

            File file = new File(path);
            file.mkdirs();
            apkPath = path + fileName;
            File outputFile = new File(file, fileName);
            fos = new FileOutputStream(outputFile);

            is = connection.getInputStream();

            byte[] buffer = new byte[BUFFER_LENGTH];
            int len = 0;
            long total = 0;

            while ((len = is.read(buffer)) != -1) {
                total += len;
                int tmpProgress = (int) ((total * SplashScreen.MAX_DIALOG_PROGRESS) / lenghtOfFile);
                if (tmpProgress != progress) {
                    progress = tmpProgress;
                    postProgress();
                }
                fos.write(buffer, 0, len);
            }
            isDone = true;
        } catch (Exception e) {
            log.error("Cannot update APK", e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception ex) {
                log.error("Cannot close fos stream", ex);
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception ex) {
                log.error("Cannot close is stream", ex);
            }

        }
    }
}
