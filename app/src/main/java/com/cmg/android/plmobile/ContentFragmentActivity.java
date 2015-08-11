/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */

package com.cmg.android.plmobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.cmg.android.common.CommonIntent;
import com.cmg.android.common.Environment;
import com.cmg.android.pension.activity.content.AboutActivity;
import com.cmg.android.pension.activity.content.FeedbackActivity;
import com.cmg.android.pension.activity.content.HelpActivity;
import com.cmg.android.pension.activity.content.ShareActivity;
import com.cmg.android.util.AndroidCommonUtils;
import com.cmg.android.util.ExceptionHandler;

public class ContentFragmentActivity extends SherlockFragmentActivity {
    /**
     * start thread
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * stop thread
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        AndroidCommonUtils.takeScreenShot(this);
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                startActivity(AboutActivity.class);
                break;
            case R.id.action_help:
                startActivity(HelpActivity.class);
                break;
            case R.id.action_share_app:
                startActivity(ShareActivity.class);
                break;
            case R.id.action_preferences:
                startActivity(PreferenceAcitivity.class);
                break;
            case R.id.action_feedback:
                startActivity(FeedbackActivity.class);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void startActivity(Class<?> c) {
        Intent intent = new Intent();
        Bundle bundle = getIntent().getExtras();
        Uri uri = getIntent().getData();
        if (uri != null) {
            intent.setData(uri);
        }
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putString(CommonIntent.FROM_ACTIVITY_CLASS, this.getClass().getName());
        intent.setClass(this, c);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
