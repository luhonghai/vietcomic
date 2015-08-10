/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */

package com.halosolutions.itranslator.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.halosolutions.itranslator.R;
import com.halosolutions.itranslator.common.DeviceInfoCommon;
import com.halosolutions.itranslator.http.UploadFeedbackAsync;
import com.halosolutions.itranslator.utilities.AndroidHelper;
import com.halosolutions.itranslator.utilities.ContentUtils;
import com.halosolutions.itranslator.utilities.DeviceUuidFactory;
import com.halosolutions.itranslator.utilities.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FeedbackActivity extends BaseActivity {

    public static final String SEND_FEEDBACK_FINISH = "com.halosolutions.itranslator.activity.FeedbackActivity";

    private String stackTrace;

    private SweetAlertDialog dialogProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_feedback);
        Toolbar mToolbar = (Toolbar)findViewById(R.id.pref_toolbar);
        mToolbar.setTitle("Feedback");
        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback();
            }
        });
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(ExceptionHandler.STACK_TRACE)) {
            stackTrace =bundle.getString(ExceptionHandler.STACK_TRACE);

            SweetAlertDialog d = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
            d.setTitleText(getString(R.string.error_message_title));
            d.setContentText(getResources().getString(R.string.error_message));
            d.setConfirmText(getResources().getString(R.string.dialog_ok));
            d.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismissWithAnimation();
                }
            });
            d.show();
        }
        registerReceiver(mHandleMessageReader, new IntentFilter(SEND_FEEDBACK_FINISH));
    }

    private void showProcessDialog() {
        dialogProcess = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        dialogProcess.setTitleText(getString(R.string.processing));
        dialogProcess.setCancelable(false);
        dialogProcess.show();
    }

    public String getTextDescription() {
        String desc = null;
        EditText text = (EditText) findViewById(R.id.textDescription);
        desc = text.getText().toString();
        return desc;
    }


    private void sendFeedback() {
        if (checkNetwork(false)) {
            if (getTextDescription().trim().length() == 0) {
                SweetAlertDialog d = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
                d.setTitleText(getString(R.string.please_enter_your_message));
                d.setContentText("");
                d.setConfirmText(getString(R.string.dialog_ok));
                d.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                });
                d.show();
            } else {
                Map<String, String> params = new HashMap<String, String>();
                params.put("firstName","Halo");
                params.put("lastName", "Solutions");
                params.put("email", "halo.app.solutions@gmail.com");
                params.put("happy", "true");
                params.put("message", ContentUtils.generatePreviewHtmlFeedback(getFormData()));
                UploadFeedbackAsync uploadAsync = new UploadFeedbackAsync(this.getApplicationContext(), params);
                uploadAsync.execute();
                showProcessDialog();
            }
        }
    }

    private Map<String, String> getFormData() {
        Map<String, String> infos = new HashMap<String, String>();
        infos.put(DeviceInfoCommon.FEEDBACK_DESCRIPTION, getTextDescription());
        DeviceUuidFactory uIdFac = new DeviceUuidFactory(this);
        infos.put(DeviceInfoCommon.IMEI, uIdFac.getDeviceUuid().toString());
        infos.put(DeviceInfoCommon.APP_VERSION, AndroidHelper.getVersionName(this.getApplicationContext()));
        infos.put(DeviceInfoCommon.MODEL, android.os.Build.MODEL);
        infos.put(DeviceInfoCommon.OS_VERSION, System.getProperty("os.version"));
        infos.put(DeviceInfoCommon.OS_API_LEVEL, android.os.Build.VERSION.SDK);
        infos.put(DeviceInfoCommon.DEVICE_NAME, android.os.Build.DEVICE);
        if (stackTrace != null && stackTrace.length() > 0) {
            infos.put(DeviceInfoCommon.STACK_TRACE, stackTrace);
        }
        return infos;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mHandleMessageReader);
        } catch (Exception e) {

        }
    }

    private void closeFeedBack(){
        this.finish();
    }

    /**
     * receive message from server
     */
    private final BroadcastReceiver mHandleMessageReader = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dialogProcess != null)
                        dialogProcess.dismissWithAnimation();
                    SweetAlertDialog d = new SweetAlertDialog(FeedbackActivity.this, SweetAlertDialog.SUCCESS_TYPE);
                    d.setTitleText(getString(R.string.successfully_submitted));
                    d.setContentText(getString(R.string.feedback_success_message));
                    d.setConfirmText(getString(R.string.dialog_ok));
                    d.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            FeedbackActivity.this.finish();
                        }
                    });
                    d.show();
                }
            });
        }
    };

}
