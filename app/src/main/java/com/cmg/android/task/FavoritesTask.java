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

import com.cmg.android.pension.activity.NewsletterDetailActivity;
import com.cmg.android.pension.database.DatabaseHandler;
import com.cmg.mobile.shared.data.Newsletter;

import org.apache.log4j.Logger;

/**
 * Created by Hai Lu on 10/23/13.
 */
public class FavoritesTask {
    private static final Logger log = Logger.getLogger(FavoritesTask.class);
    private final Context context;
    private DatabaseHandler db;
    private Runnable runner;
    private final Handler mHandler = new Handler();

    /**
     * Constructor
     *
     * @param context
     */
    public FavoritesTask(Context context) {
        this.context = context;
    }

    public void execute(final Newsletter newsletter) {
        if (runner != null) {
            mHandler.removeCallbacks(runner);
        }
        runner = new Runnable() {

            @Override
            public void run() {
                doInBackground(newsletter);
            }
        };
        mHandler.post(runner);
    }

    protected Void doInBackground(final Newsletter newsletter) {
        if (db == null) {
            db = new DatabaseHandler(context);
        }
        log.info("Set newsletter favorites status: " + (newsletter.checkFavor() ? " IS_FAVOR " : " NOT_FAVOR"));
        if (db.updateFavorStatus(newsletter) == 1) {
            if (newsletter.checkFavor()) {
            } else {
            }
            Intent intent = new Intent(NewsletterDetailActivity.REFRESH_MAIN_ACTIVITY_MESSAGE);
            intent.putExtra(NewsletterDetailActivity.NOTIFY_REFESH_MAIN_ACTIVITY, "true");
            context.sendBroadcast(intent);
            log.info("Favorites status updated");
        }
        return null;
    }
}
