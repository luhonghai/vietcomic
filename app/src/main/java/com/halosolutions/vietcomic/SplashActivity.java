package com.halosolutions.vietcomic;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.cmg.android.cmgpdf.AsyncTask;
import com.halosolutions.vietcomic.receiver.ComicScheduleReceiver;
import com.halosolutions.vietcomic.service.ComicCheckerService;
import com.halosolutions.vietcomic.service.DataPrepareService;
import com.halosolutions.vietcomic.util.SimpleAppLog;
import com.rey.material.widget.ProgressView;


/**
 * Created by cmg on 14/08/15.
 */
public class SplashActivity extends BaseActivity {
    private static final int MIN_LOAD_TIME = 2 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ((ProgressView)findViewById(R.id.progress_pv_circular_colors)).start();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                long start = System.currentTimeMillis();
                SimpleAppLog.info("Start load data");
                DataPrepareService prepareService = new DataPrepareService(SplashActivity.this);
                prepareService.prepare();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent comicCheckerIntent = new Intent(SplashActivity.this, ComicCheckerService.class);
                        SplashActivity.this.startService(comicCheckerIntent);

                        Intent comicScheduleIntent = new Intent(SplashActivity.this, ComicScheduleReceiver.class);
                        SplashActivity.this.sendBroadcast(comicScheduleIntent);
                    }
                });
                final long executionTime = System.currentTimeMillis() - start;
                SimpleAppLog.info("Finish load data. Execution time: " + executionTime + "ms");
                if (executionTime >= MIN_LOAD_TIME) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                                }
                            }, MIN_LOAD_TIME - executionTime);
                        }
                    });
                }
                return null;
            }
        }.execute();
    }
}
