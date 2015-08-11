/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.pension.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.cmg.android.common.Environment;
import com.cmg.android.plmobile.R;
import com.cmg.android.util.ExceptionHandler;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class ForceCloseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bug);
        String bugReport = getIntent().getStringExtra(ExceptionHandler.STACK_TRACE);
        TextView txtBug = (TextView) findViewById(R.id.bug_report);

        if (Environment.isEnableAnalytics(getResources())) {
        }

        txtBug.setText(bugReport);
        txtBug.setMovementMethod(new ScrollingMovementMethod());
    }
}
