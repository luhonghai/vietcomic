/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */

package com.cmg.android.pension.activity.content;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cmg.android.plmobile.ContentActivity;
import com.cmg.android.plmobile.R;
import com.cmg.android.task.UploadFeedbackAsync;
import com.cmg.android.util.AndroidCommonUtils;
import com.cmg.android.util.ContentUtils;
import com.cmg.android.util.DeviceUuidFactory;
import com.cmg.android.util.ExceptionHandler;
import com.cmg.android.util.Utilities;
import com.cmg.mobile.shared.common.Common;
import com.cmg.mobile.shared.common.DeviceInfoCommon;
import com.cmg.mobile.shared.common.FileCommon;
import com.cmg.mobile.shared.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FeedbackActivity extends ContentActivity {
    public static final String SEND_FEEDBACK_FINISH = "com.cmg.android.pension.activity.content.FeedbackActivity";

    TextView txtDescription;
    Spinner spin;
    String stackTrace;

    private AlertDialog dialogWaiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        initSpinner();
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey(ExceptionHandler.STACK_TRACE)) {
            stackTrace =bundle.getString(ExceptionHandler.STACK_TRACE);
            final AlertDialog dialogError = new AlertDialog.Builder(this).setTitle("An unexpected error occurred")
                    .setMessage(getResources().getString(R.string.error_message))
                    .setNegativeButton(getResources().getString(R.string.dialog_close),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                }
                            }).create();
            dialogError.show();
        }
        TextView t3 = (TextView) findViewById(R.id.textView2);
        t3.setText(Html.fromHtml("<a href=\"http://www.c-mg.com/mobile/privacy\">How is my information stored and shared</a>"));
        t3.setMovementMethod(LinkMovementMethod.getInstance());
        dialogWaiting = new AlertDialog.Builder(this)
                .setMessage("Please wait a moment while your feedback is being processed")
                .setNegativeButton(getResources().getString(R.string.dialog_close),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        }).create();
        registerReceiver(mHandleMessageReader, new IntentFilter(SEND_FEEDBACK_FINISH));
    }


    public void initSpinner() {
        spin = (Spinner) findViewById(R.id.spinner1);
        ArrayList<String> list = new ArrayList<String>();
        list.add("Anonymous");
        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] accounts = manager.getAccounts();
        if (accounts != null && accounts.length != 0) {
            for (Account acc : accounts) {
                if (acc.type.equalsIgnoreCase("com.google"))
                    list.add(acc.name);

            }
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(dataAdapter);
    }

    public String getItemSelectSpin() {
        String account = "Anonymous";
        Spinner mySpinner = (Spinner) findViewById(R.id.spinner1);
        String text = mySpinner.getSelectedItem().toString();
        if (text != null && text != "") {
            account = text;
        }

        return account;
    }

    public String getTextDescription() {
        String desc = null;
        EditText text = (EditText) findViewById(R.id.textDescription);
        desc = text.getText().toString();
        return desc;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //super.onCreateOptionsMenu(menu);
        menu.add("Cancel").setShowAsAction(
                MenuItem.SHOW_AS_ACTION_ALWAYS
                        | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        menu.add("Preview").setShowAsAction(
                MenuItem.SHOW_AS_ACTION_ALWAYS
                        | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        menu.add("Send").setShowAsAction(
                MenuItem.SHOW_AS_ACTION_ALWAYS
                        | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Cancel")) {
            super.onBackPressed();
        } else if (item.getTitle().equals("Preview")) {
            String html = ContentUtils.generatePreviewHtmlFeedback(getFormData());
            View view = getLayoutInflater().inflate(R.layout.change_log, null);
            ((WebView) view.findViewById(R.id.webView)).loadDataWithBaseURL(
                    html, html, "text/html", null, null);
            final AlertDialog dialogChangeLog = new AlertDialog.Builder(this)
                    .setTitle("Preview Feedback")
                    .setView(view)
                    .setNegativeButton(getResources().getString(R.string.dialog_close),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();

                                }
                            }).create();

            dialogChangeLog.show();
        } else if (item.getTitle().equals("Send")) {
            Map<String, String> params = getFormData();
            String screenshootPath = AndroidCommonUtils.getLatestScreenShootPath(this.getApplicationContext());
            params.put(FileCommon.PARA_FILE_PATH, screenshootPath);
            params.put(FileCommon.PARA_FILE_NAME, StringUtils.getFileName(screenshootPath));
            params.put(FileCommon.PARA_FILE_TYPE, FileCommon.PNG_MIME_TYPE);
            UploadFeedbackAsync uploadAsync = new UploadFeedbackAsync(this.getApplicationContext());
            uploadAsync.execute(params);

            dialogWaiting.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private Map<String, String> getFormData() {
        Map<String, String> infos = new HashMap<String, String>();
        DeviceUuidFactory uIdFac = new DeviceUuidFactory(this);
        CheckBox cbcreen = (CheckBox) findViewById(R.id.cb_screen);
        if (cbcreen.isChecked()) {
            String pathLastScreenShot = AndroidCommonUtils.getLatestScreenShootURL(this);
            if (pathLastScreenShot != null && pathLastScreenShot.length() > 0) {
                infos.put(ContentUtils.KEY_SCREENSHOOT, pathLastScreenShot);
            }
        }

        infos.put(DeviceInfoCommon.ACCOUNT, getItemSelectSpin());
        infos.put(DeviceInfoCommon.FEEDBACK_DESCRIPTION, getTextDescription());
        infos.put(DeviceInfoCommon.IMEI, uIdFac.getDeviceUuid().toString());
        CheckBox includeData = (CheckBox) findViewById(R.id.cb_data);
        if (includeData.isChecked()) {
            infos.put(DeviceInfoCommon.APP_VERSION, Utilities.getVersionName(this.getApplicationContext()));
            infos.put(DeviceInfoCommon.MODEL, android.os.Build.MODEL);
            infos.put(DeviceInfoCommon.OS_VERSION, System.getProperty("os.version"));
            infos.put(DeviceInfoCommon.OS_API_LEVEL, android.os.Build.VERSION.SDK);
            infos.put(DeviceInfoCommon.DEVICE_NAME, android.os.Build.DEVICE);
        }
        if (stackTrace != null && stackTrace.length() > 0) {
            infos.put(DeviceInfoCommon.STACK_TRACE, stackTrace);
        }
        infos.put(Common.TYPE, Common.FEEDBACK);
        return infos;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mHandleMessageReader);
    }

    private void closeFeedBack(){
        super.onBackPressed();
    }

    /**
     * receive message from server
     */
    private final BroadcastReceiver mHandleMessageReader = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().containsKey(SEND_FEEDBACK_FINISH)) {
                if (dialogWaiting == null)
                    return;
                if (dialogWaiting.isShowing()) {
                    dialogWaiting.dismiss();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            closeFeedBack();
                        }
                    }, 3000);

                }
            }
        }
    };

}
