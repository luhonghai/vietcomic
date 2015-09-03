package com.halosolutions.mangaworld.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.halosolutions.mangaworld.util.SimpleAppLog;

/**
 * Created by cmg on 24/08/15.
 */
public class ComicScheduleReceiver extends BroadcastReceiver {

    private static final int INTERVAL_TIME = 6 * 60 * 60 * 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        SimpleAppLog.info("Call comic schedule");
        Intent comicChecker = new Intent(context, ComicCheckReceiver.class);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent comicCheckerSender = PendingIntent.getBroadcast(context, 0, comicChecker, 0);
        try {
            alarmManager.cancel(comicCheckerSender);
        } catch (Exception e) {}

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + INTERVAL_TIME, INTERVAL_TIME, comicCheckerSender);
    }
}
