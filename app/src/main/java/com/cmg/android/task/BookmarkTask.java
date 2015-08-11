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
import android.os.Handler;

import com.cmg.android.cmgpdf.CmgPDFActivity;
import com.cmg.android.pension.activity.NewsletterDetailActivity;
import com.cmg.android.pension.database.DatabaseHandler;
import com.cmg.android.plmobile.MainActivity;
import com.cmg.android.util.SimpleAppLog;
import com.cmg.mobile.shared.data.Newsletter;


/**
 * Created by Hai Lu on 10/24/13.
 */
public class BookmarkTask {
    private final Context context;
    private DatabaseHandler db;
    private Runnable runner;
    private final Handler mHandler = new Handler();

    /**
     * Constructor
     *
     * @param context
     */
    public BookmarkTask(Context context) {
        this.context = context;
    }

    public void execute(final Newsletter newsletter, final int page, final boolean isAdd) {
        if (runner != null) {
            mHandler.removeCallbacks(runner);
        }
        runner = new Runnable() {

            @Override
            public void run() {
                doInBackground(newsletter, page, isAdd);
            }
        };
        mHandler.post(runner);
    }

    protected Void doInBackground(final Newsletter newsletter, final int page, final boolean isAdd) {
        if (db == null) {
            db = new DatabaseHandler(context);
        }
        SimpleAppLog.info((isAdd ? "Add" : "Remove") + " bookmark page " +page+ " newsletter id : " + newsletter.getId());
        boolean done = false;
        if (isAdd) {
            done = db.addBookmark(newsletter.getId(), page);
            if (done) {
                newsletter.addBookmark(page);
                Intent intent = new Intent(CmgPDFActivity.NEED_REFRESH_PREV_ACTIVITY);
                intent.putExtra(CmgPDFActivity.NEED_REFRESH_PREV_ACTIVITY, MainActivity.class.getName());
                context.sendBroadcast(intent);

                if (newsletter.getBookmarkPages().size() >= 1 && !newsletter.checkFavor()) {
                    newsletter.setIsFavor(Newsletter.IS_FAVOR);
                    if (db.updateFavorStatus(newsletter) == 1) {
                        if (newsletter.getBookmarkPages().size() == 1) {
                            intent = new Intent(CmgPDFActivity.NEED_REFRESH_PREV_ACTIVITY);
                            intent.putExtra(CmgPDFActivity.NEED_REFRESH_PREV_ACTIVITY, NewsletterDetailActivity.class.getName());
                            context.sendBroadcast(intent);
                        }
                    }
                }
            }
        } else {
            done = db.removeBookmark(newsletter.getId(), page);
            if (done) {
                newsletter.removeBookmark(page);
                Intent intent = new Intent(CmgPDFActivity.NEED_REFRESH_PREV_ACTIVITY);
                intent.putExtra(CmgPDFActivity.NEED_REFRESH_PREV_ACTIVITY, MainActivity.class.getName());
                context.sendBroadcast(intent);
            }

        }
        if (done) {
            Intent intent = new Intent(NewsletterDetailActivity.REFRESH_MAIN_ACTIVITY_MESSAGE);
            intent.putExtra(NewsletterDetailActivity.NOTIFY_REFESH_MAIN_ACTIVITY, "true");
            context.sendBroadcast(intent);
            SimpleAppLog.info("Bookmark updated");
        }
        return null;
    }
}
