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
import android.os.AsyncTask;
import android.widget.Toast;

import com.cmg.android.http.FileUploader;
import com.cmg.android.http.exception.UploaderException;
import com.cmg.android.pension.activity.content.FeedbackActivity;
import com.cmg.android.plmobile.R;
import com.cmg.android.util.SimpleAppLog;


import java.io.FileNotFoundException;
import java.util.Map;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator Hai Lu
 * @Last changed: $LastChangedDate$
 */
public class UploadFeedbackAsync extends AsyncTask<Map<String, String>, Void, String> {
    private final Context context;

    public UploadFeedbackAsync(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected String doInBackground(Map<String, String>... params) {
        try {
            StringBuffer results = new StringBuffer();
            if (params != null && params.length > 0) {
                for (Map<String, String> param : params) {
                    results.append(FileUploader.upload(param, context.getResources().getString(R.string.feedback_url)));
                }
            }

            return results.toString();
        } catch (FileNotFoundException e) {
            SimpleAppLog.error(e);
            return e.getMessage();
        } catch (UploaderException e) {
            SimpleAppLog.error(e);
            return e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String v) {
        Intent intent = new Intent(FeedbackActivity.SEND_FEEDBACK_FINISH);
        intent.putExtra(FeedbackActivity.SEND_FEEDBACK_FINISH, "true");
        context.sendBroadcast(intent);
        Toast.makeText(context, "Completed", Toast.LENGTH_LONG).show();

        super.onPostExecute(v);
    }

}
