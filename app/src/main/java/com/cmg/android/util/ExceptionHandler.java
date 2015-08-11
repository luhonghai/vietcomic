/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.util;

import android.content.Context;
import android.content.Intent;
import android.os.Process;

import com.cmg.android.pension.activity.ForceCloseActivity;
import com.cmg.android.pension.activity.content.FeedbackActivity;


import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class ExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler {
    private final Context context;
    public final static String STACK_TRACE = "bug";

    /**
     * Constructor
     *
     * @param context
     */
    public ExceptionHandler(Context context) {
        this.context = context;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        // TODO Auto-generated method stub
        ex.printStackTrace();
        SimpleAppLog.error("Uncaught exception", ex);
        StringWriter stackTrace = new StringWriter();
        ex.printStackTrace(new PrintWriter(stackTrace));
        //SimpleAppLog.error(stackTrace);
        SimpleAppLog.info("Redirect to Feedback page");
        Intent forceClose = new Intent(context, FeedbackActivity.class);
        forceClose.putExtra(STACK_TRACE, stackTrace.toString());
        context.startActivity(forceClose);
        Process.killProcess(Process.myPid());
        System.exit(10);
    }

}
