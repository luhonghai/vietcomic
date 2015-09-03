package com.halosolutions.mangaworld.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.halosolutions.mangaworld.service.ComicCheckerService;
import com.halosolutions.mangaworld.util.SimpleAppLog;

/**
 * Created by cmg on 24/08/15.
 */
public class ComicCheckReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SimpleAppLog.info("Start comic check receiver");
        Intent comicChecker = new Intent(context, ComicCheckerService.class);
        context.startService(comicChecker);
    }
}
