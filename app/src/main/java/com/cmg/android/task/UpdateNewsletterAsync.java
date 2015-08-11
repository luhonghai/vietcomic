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

import com.cmg.android.pension.database.DatabaseHandler;
import com.cmg.android.pension.downloader.DownloadXmlHelper;
import com.cmg.android.util.SimpleAppLog;
import com.cmg.android.util.SyncFileUtil;
import com.cmg.mobile.shared.data.Newsletter;


import java.util.List;

public class UpdateNewsletterAsync {
    private List<Newsletter> oldList;
    private final Context context;
    private DatabaseHandler db;
    private List<Newsletter> newList;
    private boolean completed = false;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            doSync();
        }
    };

    public void execute() {
        Thread t = new Thread(runnable);
        t.start();
    }

    public UpdateNewsletterAsync(Context context) {
        this.context = context;
        this.db = new DatabaseHandler(context);
    }

    public boolean checkExists() {
        oldList = db.getAllNewsletters();
        return (oldList != null && oldList.size() > 0);
    }

    public void doSync() {
        if (!completed) {
            try {
                SimpleAppLog.info("Start sync newsletter");
                newList = DownloadXmlHelper.downloadNewsletters(context);
                SyncFileUtil syncUtil = new SyncFileUtil(context, db, newList,
                        oldList);
                syncUtil.syncClientServer();
                completed = true;
                SimpleAppLog.info("Completed sync newsletter");
            } catch (Exception e) {
                SimpleAppLog.error("Cannot update newsletter", e);
            }
        }
    }

    public boolean isCompleted() {
        return completed;
    }
}
