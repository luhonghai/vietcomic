package com.halosolutions.vietcomic.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.google.gson.Gson;
import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.util.SimpleAppLog;

/**
 * Created by cmg on 18/08/15.
 */
public class BroadcastHelper {

    public interface OnComicBookUpdated {
        void onUpdated(ComicBook comicBook);
    }

    public static final String ON_COMIC_BOOK_UPDATED = "ON_COMIC_BOOK_UPDATED";

    private final Context context;

    private final Gson gson;

    private BroadcastReceiver onComicBookUpdatedReceiver;

    public BroadcastHelper(Context context) {
        this.context = context;
        gson = new Gson();
    }

    public void registerOnComicBookUpdated(final OnComicBookUpdated onComicBookUpdated) {
        try {
            onComicBookUpdatedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (onComicBookUpdated != null) {
                        String raw = intent.getExtras().getString(ComicBook.class.getName());
                        //SimpleAppLog.debug("On comic book updated: " + raw);
                        onComicBookUpdated.onUpdated(
                                gson.fromJson(raw,
                                        ComicBook.class));
                    }
                }
            };
            this.context.registerReceiver(onComicBookUpdatedReceiver, new IntentFilter(ON_COMIC_BOOK_UPDATED));
        } catch (Exception e) {
            SimpleAppLog.error("Could not register receiver " + ON_COMIC_BOOK_UPDATED, e);
        }
    }

    public void unregister() {
        try {
            if (onComicBookUpdatedReceiver != null)
                this.context.unregisterReceiver(onComicBookUpdatedReceiver);
        } catch (Exception e) {
            SimpleAppLog.error("Could not unregister receiver " + ON_COMIC_BOOK_UPDATED, e);
        }
    }

    public void sendComicUpdate(ComicBook comicBook) {
        Intent intent = new Intent(ON_COMIC_BOOK_UPDATED);
        intent.putExtra(ComicBook.class.getName(), gson.toJson(comicBook));
        context.sendBroadcast(intent);
    }
}
