/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.pension.downloader.task;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.cmg.android.cmgpdf.CmgPDFActivity;
import com.cmg.android.common.ViewWrapperInfo;
import com.cmg.android.pension.activity.NewsletterDetailActivity;
import com.cmg.android.pension.database.DatabaseHandler;
import com.cmg.android.pension.view.DownloadImage;
import com.cmg.android.plmobile.MainActivity;
import com.cmg.android.plmobile.R;
import com.cmg.android.util.FileUtils;
import com.cmg.mobile.shared.data.Newsletter;
import com.cmg.mobile.shared.util.ContentGenerater;
import com.cmg.mobile.shared.util.FileHelper;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class DownloadAsync extends AsyncTask<Void, Void, Boolean> {
    private static Logger log = Logger.getLogger(DownloadAsync.class);
    public static final String DISPLAY_MESSAGE_ACTION = "com.cmg.android.pension.caching.DownloadAsync";
    public static final String PROGRESS_VALUE = "PROGRESS_VALUE";
    private static final int MAX_PROGRESS = 100;
    private static final int BUFFER_LENGTH = 1024;
    private final Context context;
    private final Newsletter newsletter;
    private boolean isDone = false;
    private int progress = 0;
    @SuppressWarnings("unused")
    private long lastPostTime = 0;
    private final Intent intent;
    @SuppressWarnings("unused")
    private static int countNotification = 0;
    private int lastProgress = 0;
    private String pdfPath;

    /**
     * Constructor
     */
    public DownloadAsync(Newsletter newsletter, Context context) {
        this.newsletter = newsletter;
        this.context = context;
        intent = new Intent(DISPLAY_MESSAGE_ACTION + "@" + newsletter.getId());
        intent.putExtra(Newsletter.NEWSLETTER_ID, newsletter.getId());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String urlPath = newsletter.getFileUrl();
        FileOutputStream fos = null;
        InputStream is = null;
        try {
            URL url = new URL(urlPath);
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
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.connect();
            int lenghtOfFile = connection.getContentLength();

            String path = FileUtils.getPdfFolder(newsletter.getType(), context);
            File file = new File(path);
            file.mkdirs();

            pdfPath = FileUtils.getPdfFile(newsletter, context);
            File outputFile = new File(pdfPath);
            fos = new FileOutputStream(outputFile);

            is = connection.getInputStream();

            byte[] buffer = new byte[BUFFER_LENGTH];
            int len = 0;
            long total = 0;
            while ((len = is.read(buffer)) != -1) {
                total += len;
                long current = System.currentTimeMillis();

                progress = (int) ((total * MAX_PROGRESS) / lenghtOfFile);
                if (progress != lastProgress && progress != MAX_PROGRESS) {
                    postProgress(context, Integer.toString(progress));
                    lastProgress = progress;
                }
                lastPostTime = current;

                fos.write(buffer, 0, len);
            }
            postProgress(context, Integer.toString(MAX_PROGRESS));

            newsletter.setDownloaded(1);
            DatabaseHandler db = new DatabaseHandler(context);
            db.updateStatusById(newsletter);
            log.info("Newsletter is downloaded " + newsletter.getId() + " | "
                    + newsletter.checkDownloaded());

            Intent intent = new Intent(NewsletterDetailActivity.REFRESH_MAIN_ACTIVITY_MESSAGE);
            intent.putExtra(NewsletterDetailActivity.NOTIFY_REFESH_MAIN_ACTIVITY, "true");
            context.sendBroadcast(intent);

            isDone = true;
        } catch (IOException e) {
            log.error("Error when download newsletter", e);
            isDone = false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception ex) {
                    log.error("Error when close stream fos", ex);
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ex) {
                    log.error("Error when close stream is", ex);
                }
            }
        }
        return isDone;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPostExecute(Boolean result) {
        if (isDone) {
            try {
                Toast toaster = Toast.makeText(context, "Download newsletter "
                        + newsletter.getTitle() + " successfully",
                        Toast.LENGTH_SHORT);
                toaster.show();
                int randomId = (int) (Math.random() * 100);
                NotificationManager notificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notification = new Notification(
                        R.drawable.pl_mobile, "Download successfully",
                        System.currentTimeMillis());
                // String title = context.getString(SR.string.app_name);
                notification.tickerText = "Download newsletter "
                        + newsletter.getTitle() + " successfully";
                Intent notificationIntent = null;
                File file = new File(pdfPath);
                if (file.exists()) {
                    Uri uri = Uri.fromFile(file);
                    notificationIntent = new Intent(Intent.ACTION_VIEW, uri);
                    Bundle bundle = new Bundle();
                    bundle.putString(ViewWrapperInfo.DETAIL_CLASS,
                            NewsletterDetailActivity.class.getName());
                    bundle.putInt(ViewWrapperInfo.ITEM_PAGE, -1);
                    bundle.putString(ViewWrapperInfo.MAIN_CLASS,
                            MainActivity.class.getName());
                    bundle.putString(ViewWrapperInfo.ITEM_ID_KEY,
                            Newsletter.NEWSLETTER_ID);
                    bundle.putString(ViewWrapperInfo.ITEM_ID_VALUE,
                            newsletter.getId());
                    bundle.putString(ViewWrapperInfo.SHARE_SUBJECT,
                            newsletter.getTitle());
                    bundle.putString(ViewWrapperInfo.SHARE_TEXT,
                            ContentGenerater.generateShareInfo(newsletter));
                    notificationIntent.putExtras(bundle);

                    notificationIntent.setClass(context,
                            CmgPDFActivity.class);
                }

                // Intent notificationIntent = new Intent(context,
                // NewsletterDetailActivity.class);
                // notificationIntent.putExtra(Newsletter.NEWSLETTER_ID,
                // newsletter.getId());
                // set intent so it does not start a new activity
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                } else {
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                }
                PendingIntent intent = PendingIntent.getActivity(context,
                        randomId, notificationIntent,
                        PendingIntent.FLAG_ONE_SHOT);
                notification.setLatestEventInfo(context,
                        "Download successfully",
                        "Newsletter: " + newsletter.getTitle(), intent);
                notification.defaults |= Notification.DEFAULT_SOUND;
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                // notification.flags |= Notification.FLAG_ONGOING_EVENT;
                notificationManager.notify(randomId, notification);
            } catch (Exception ex) {
                log.error("Error when download newsletter show notification",
                        ex);
            }
        } else {
            log.info("Could not download");
            Toast.makeText(
                    context,
                    "Could not download newsletter "
                            + newsletter.getTitle()
                            + ". Connection is slow or resource is not available",
                    Toast.LENGTH_SHORT).show();
            postProgress(context,
                    Integer.toString(DownloadImage.DRAW_DOWNLOADING_ANI));
        }
    }

    /**
     * Post data progress to other view
     *
     * @param context
     * @param value
     */
    public void postProgress(Context context, String value) {
        try {
            intent.putExtra(PROGRESS_VALUE, value);
            context.sendBroadcast(intent);
        } catch (Exception ex) {
            log.error("Error when post progress to view", ex);
        }
    }

}
