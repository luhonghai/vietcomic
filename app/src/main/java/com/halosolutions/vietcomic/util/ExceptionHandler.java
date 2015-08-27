package com.halosolutions.vietcomic.util;

import android.content.Context;
import android.content.Intent;
import android.os.Process;


import com.halosolutions.vietcomic.FeedbackActivity;

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
public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

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
        StringWriter stackTrace = new StringWriter();
        ex.printStackTrace(new PrintWriter(stackTrace));
        SimpleAppLog.error("UncaughtExceptionHandler catch",ex);
        Intent forceClose = new Intent(context, FeedbackActivity.class);
        forceClose.putExtra(STACK_TRACE, stackTrace.toString());
        context.startActivity(forceClose);
        Process.killProcess(Process.myPid());
        System.exit(10);
    }

}
