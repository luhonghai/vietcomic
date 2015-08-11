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

import com.cmg.android.pension.activity.NewsletterDetailActivity;
import com.cmg.android.pension.database.DatabaseHandler;
import com.cmg.android.plmobile.SplashScreen;
import com.cmg.mobile.shared.data.Newsletter;
import com.cmg.mobile.shared.util.ContentGenerater;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class UpdateNewStatusAsync {
    private final Context context;
    private final Newsletter newsletter;
    private DatabaseHandler db;
    private Runnable runner = new Runnable() {

        @Override
        public void run() {
            doInBackground();
        }
    };

    /**
     * Constructor
     *
     * @param context
     * @param newsletter
     */
    public UpdateNewStatusAsync(Context context, Newsletter newsletter) {
        this.context = context;
        this.newsletter = newsletter;
    }

    public void execute() {
        Thread t = new Thread(runner);
        t.start();
    }

    protected Void doInBackground() {
        if (newsletter.getIsNew() == Newsletter.IS_NEW) {
            db = new DatabaseHandler(context);
            newsletter.setNew(Newsletter.NOT_NEW);
            db.updateNewStatusById(newsletter);

            Intent intent = new Intent(NewsletterDetailActivity.REFRESH_MAIN_ACTIVITY_MESSAGE);
            intent.putExtra(NewsletterDetailActivity.NOTIFY_REFESH_MAIN_ACTIVITY, "true");
            context.sendBroadcast(intent);
        }
        return null;
    }

}
