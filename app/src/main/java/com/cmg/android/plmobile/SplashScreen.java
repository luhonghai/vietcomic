/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.plmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

import com.cmg.android.pension.animation.SplashPanel;
import com.cmg.android.service.StartupService;
import com.cmg.android.task.UpdateAsync;
import com.cmg.android.util.ContentUtils;
import com.cmg.android.util.SimpleAppLog;
import com.cmg.android.util.Utilities;


import java.io.File;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class SplashScreen extends Activity {
    public static final String SPLASH_SCREEN_MESSAGE = "com.cmg.android.plmobile.SplashScreen";
    public static final String MESSAGE_ACTION = "MESSAGE_ACTION";
    public static final String START_MAIN_ACTIVITY = "START_MAIN_ACTIVITY";
    public static final String SHOW_CONFIRM_UPDATE = "SHOW_CONFIRM_UPDATE";
    public static final String START_INSTALL_UPDATE = "START_INSTALL_UPDATE";
    public static final String UPDATE_PROGRESS = "UPDATE_PROGRESS";
    public static final String COMPLETE_ANIMATION = "COMPLETE_ANIMATION";
    public static final String APK_URL = "APK_URL";
    public static final String APK_PATH = "APK_PATH";
    public static final String PROGRESS_VALUE = "PROGRESS_VALUE";
    public static final String UPDATE_MESSAGE = "UPDATE_MESSAGE";
    public static final int WAITING_TIME = 2000;
    public static final int MAX_DIALOG_PROGRESS = 100;
    private Context context;
    private boolean isOnline;
    private AlertDialog alert;
    private boolean isShowConfirm = false;
    private boolean isCompleteAnimation = true;
    private boolean isStartMainActivity = false;
    private String updateMessage;
    private ProgressDialog progressDialog;
    private String apkUrl;
    private Handler startUpHandler = new Handler();
    private SplashPanel splashPanel;
    private StartupService startupSerice;

    /**
     * handle startup thread
     */
    private Runnable startUp = new Runnable() {
        @Override
        public void run() {
            startAnimation();
            registerReceiver(mHandleMessageReceiver, new IntentFilter(
                    SPLASH_SCREEN_MESSAGE));
            isOnline = isOnline();
            startupSerice = new StartupService(getApplicationContext(),
                    isOnline, Utilities.getVersionName(context));
            startupSerice.init();
            if (!isOnline) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isStartMainActivity = true;
                        if (isCompleteAnimation) {
                            startMainActivity();
                        }

                    }
                }, 1000);

            }
        }
    };

    /**
     * The thread to process splash screen events
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    ;

    /**
     * stop thread
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * start animation
     */
    void startAnimation() {
        View view = findViewById(R.id.splash_image);
        if (view instanceof SplashPanel) {
            splashPanel = ((SplashPanel) view);
            splashPanel.start();
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_screen);
        context = this;
        startUpHandler.post(startUp);
    }

    /**
     * destroy view
     */
    @Override
    protected void onDestroy() {
        try {
            if (mHandleMessageReceiver != null) {
                unregisterReceiver(mHandleMessageReceiver);
            }
            if (startupSerice != null) {
                startupSerice.recycle();
            }
            startUpHandler.removeCallbacks(startUp);
            startUpHandler = null;
            if (splashPanel != null) {
                splashPanel.recycle();
                splashPanel = null;
            }
        } catch (Exception ex) {
            SimpleAppLog.error("Error when destroy splash screen", ex);
        }
        super.onDestroy();
    }

    /**
     * check if device connect to Internet
     *
     * @return
     */
    public boolean isOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            }
        } catch (Exception ex) {
            SimpleAppLog.error("Error when check is online", ex);
        }
        return false;
    }

    /**
     * start main activity
     */
    public void startMainActivity() {
        SimpleAppLog.info("start main activity");
        try {
            Class<?> mainActivity = Class.forName(context
                    .getResources().getString(R.string.main_activity));
            Intent intent = new Intent(this, mainActivity);
//			if (NavUtils.shouldUpRecreateTask((Activity) context,
//					intent)) {
//				TaskStackBuilder.from(context).addNextIntent(intent)
//						.startActivities();
//				finish();
//			} else {
//				NavUtils.navigateUpTo((Activity) context, intent);
//			}
            startActivity(intent);
        } catch (Exception e) {
            SimpleAppLog.error("Cannot start main activity", e);
        }
//		return null;
//		AsyncTask<Void, Void, Void> startUp = new AsyncTask<Void, Void, Void>() {
//			@Override
//			protected Void doInBackground(Void... params) {
//
//			}
//
//		};
//		startUp.execute();
    }

    /**
     * receive message from server
     */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString(MESSAGE_ACTION);
            if (action != null && action.length() > 0) {
                if (action.equals(START_MAIN_ACTIVITY)) {
                    isStartMainActivity = true;
                    if (isCompleteAnimation) {
                        startMainActivity();
                    }
                } else if (action.equals(SHOW_CONFIRM_UPDATE)) {
                    isShowConfirm = true;
                    updateMessage = intent.getExtras()
                            .getString(UPDATE_MESSAGE);
                    apkUrl = intent.getExtras().getString(APK_URL);
                    if (isCompleteAnimation) {
                        showConfirmUpdate(updateMessage, apkUrl);
                    }
                } else if (action.equals(UPDATE_PROGRESS)) {
                    updateProgress(intent.getExtras().getInt(PROGRESS_VALUE));
                } else if (action.equals(START_INSTALL_UPDATE)) {
                    installAPK(intent.getExtras().getString(APK_PATH));
                } else if (action.equals(COMPLETE_ANIMATION)) {
                    isCompleteAnimation = true;
                    if (isShowConfirm) {
                        showConfirmUpdate(updateMessage, apkUrl);
                    } else if (isStartMainActivity) {
                        startMainActivity();
                    }
                }
            }
        }
    };

    /**
     * handle update function
     *
     * @author LongNguyen
     */
    class OnClickUpdate implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            showProgressUpdate();
            UpdateAsync update = new UpdateAsync(getApplicationContext(),
                    apkUrl);
            update.execute();
        }

    }

    /**
     * @author LUHONGHAI
     */
    class OnClickViewChangeLog implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String html = ContentUtils.generateChangelogHTML(context);
            View view = getLayoutInflater().inflate(R.layout.change_log, null);
            ((WebView) view.findViewById(R.id.webView)).loadDataWithBaseURL(
                    html, html, "text/html", null, null);
            final AlertDialog dialogChangeLog = new AlertDialog.Builder(context)
                    .setTitle(getResources().getString(R.string.title_changelog))
                    .setView(view)
                    .setNegativeButton(getResources().getString(R.string.dialog_close),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                    if (!alert.isShowing())
                                        alert.show();
                                }
                            }).create();

            dialogChangeLog.show();
        }
    }

    /**
     * alert update notify
     */
    private void initConfirmUpdate() {
        try {
            if (alert == null) {
                alert = new AlertDialog.Builder(context)
                        .setTitle("Confirm Update")
                        .setPositiveButton("Update", new OnClickUpdate())
                        .setNeutralButton("Changelog",
                                new OnClickViewChangeLog())
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    /*
                                     * (non-Javadoc)
                                     *
                                     * @see android.content.DialogInterface.
                                     * OnClickListener
                                     * #onClick(android.content.DialogInterface,
                                     * int)
                                     */
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        dialog.dismiss();
                                        startMainActivity();
                                    }
                                }).create();
                alert.setCanceledOnTouchOutside(false);
            }
        } catch (Exception ex) {
            SimpleAppLog.error("Error when create confirm dialog", ex);
            startMainActivity();
        }
    }

    /**
     * show alert message
     *
     * @param updateMessage
     * @param apkUrl
     */
    private void showConfirmUpdate(final String updateMessage,
                                   final String apkUrl) {
        try {
            this.apkUrl = apkUrl;
            initConfirmUpdate();
            alert.setMessage(updateMessage);
            alert.show();
        } catch (Exception ex) {
            SimpleAppLog.error("Error when show confirm dialog", ex);
            startMainActivity();
        }
    }

    /**
     * install new apk
     *
     * @param filePath
     */

    private void installAPK(String filePath) {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Intent promtInstall = new Intent(Intent.ACTION_VIEW);
            promtInstall.setDataAndType(Uri.fromFile(new File(filePath)),
                    "application/vnd.android.package-archive");
           // startActivity(promtInstall);
            TaskStackBuilder.from(this).addNextIntent(promtInstall)
                    .startActivities();
            finish();
        } catch (Exception ex) {
            SimpleAppLog.error("Error when install updated APK", ex);
            startMainActivity();
        }
    }

    /**
     * update download apk progress
     *
     * @param progress
     */
    private void updateProgress(int progress) {
        try {
            showProgressUpdate();
            progressDialog.setProgress(progress);
        } catch (Exception ex) {
            SimpleAppLog.error("Error when updateProgress", ex);
        }
    }

    /**
     * show the update progress
     */
    private void showProgressUpdate() {
        try {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog
                        .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setMessage("Please wait ...");
                progressDialog.setIndeterminate(false);
                progressDialog.setMax(MAX_DIALOG_PROGRESS);
            }
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        } catch (Exception ex) {
            SimpleAppLog.error("Error when show progress update", ex);
        }
    }
}
