
package com.halosolutions.vietcomic.http;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;


import com.halosolutions.vietcomic.FeedbackActivity;
import com.halosolutions.vietcomic.R;
import com.halosolutions.vietcomic.util.SimpleAppLog;

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
public class UploadFeedbackAsync extends AsyncTask<Void, Void, String> {
    private final Context context;
    private final Map<String, String> params;

    public UploadFeedbackAsync(Context context, Map<String, String> params) {
        this.context = context;
        this.params = params;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected String doInBackground(Void... v) {
        try {
            HttpContacter httpContacter = new HttpContacter(context);
            String result = httpContacter.post(params, context.getResources().getString(R.string.feedback_url));
            SimpleAppLog.info("Feedback response: " + result);
            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String v) {
        Intent intent = new Intent(FeedbackActivity.SEND_FEEDBACK_FINISH);
        intent.putExtra(FeedbackActivity.SEND_FEEDBACK_FINISH, "true");
        context.sendBroadcast(intent);
        super.onPostExecute(v);
    }

}
