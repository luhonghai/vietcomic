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

import com.cmg.android.pension.database.DatabaseHandler;
import com.cmg.mobile.shared.data.Newsletter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class SyncFileUtil {
    private final List<Newsletter> newList;
    private final List<Newsletter> oldList;
    private final DatabaseHandler db;
    private final Context context;

    /**
     * Constructor
     *
     * @param context
     * @param list
     */
    public SyncFileUtil(Context context, DatabaseHandler db,
                        List<Newsletter> newList, List<Newsletter> oldList) {
        this.context = context;
        this.db = db;
        this.oldList = oldList;
        this.newList = newList;
    }

    /**
     * Sync file from devices with server, and delete it of it doesn't exist on
     * server
     */
    public void syncClientServer() {
        List<Newsletter> tempList = new ArrayList<Newsletter>();
        if (oldList != null && oldList.size() > 0) {
            for (Newsletter newsletter : oldList) {
                boolean isExisted = false;
                if (newList != null && newList.size() > 0) {
                    for (Newsletter n : newList) {
                        if (n.getId().equalsIgnoreCase(newsletter.getId())) {
                            isExisted = true;
                            break;
                        }
                    }
                }
                if (!isExisted) {
                    tempList.add(newsletter);
                }
            }
        }

        for (Newsletter n : tempList) {
            File f = new File(FileUtils.getPdfFile(n, context));
            if (f.exists()) {
                f.delete();
            }
            db.deleteNewsletter(n.getId());
        }
    }
}
