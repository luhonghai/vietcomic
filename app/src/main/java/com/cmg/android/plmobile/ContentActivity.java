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
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cmg.android.common.Environment;
import com.cmg.android.pension.activity.content.AboutActivity;
import com.cmg.android.pension.activity.content.FeedbackActivity;
import com.cmg.android.pension.activity.content.HelpActivity;
import com.cmg.android.pension.activity.content.ShareActivity;
import com.cmg.android.util.AndroidCommonUtils;
import com.cmg.android.util.ExceptionHandler;

public class ContentActivity extends SherlockActivity {

    @Override
    protected void onStart() {
        super.onStart();
    }

    ;

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
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.default_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//			String fromActivity = getIntent().getExtras().getString(CommonIntent.FROM_ACTIVITY_CLASS);
//			Class<?> fromClass = null;
//			try {
//				fromClass = Class.forName(fromActivity);
//			} catch (ClassNotFoundException e) {
//				fromClass = MainActivity.class;
//			}
//
//			Intent upIntent = new Intent(this, fromClass);
//
//			Bundle bundle = getIntent().getExtras();
//			upIntent.putExtras(bundle);
//			if (getIntent().getData() != null) {
//				upIntent.setData(getIntent().getData());
//			}
//			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
//				TaskStackBuilder.from(this).addNextIntent(upIntent)
//						.startActivities();
//				finish();
//			} else {
//				NavUtils.navigateUpTo(this, upIntent);
//			}
                super.onBackPressed();
                return true;
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
        if (bundle == null) {
            bundle = new Bundle();
        }
        //bundle.putString(CommonIntent.FROM_ACTIVITY_CLASS, getIntent().getExtras().getString(CommonIntent.FROM_ACTIVITY_CLASS));
        intent.setClass(this, c);
        intent.putExtras(bundle);
        if (getIntent().getData() != null) {
            intent.setData(getIntent().getData());
        }
        startActivity(intent);
    }

}
