package com.halosolutions.vietcomic.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.google.gson.Gson;
import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.comic.ComicChapter;
import com.halosolutions.vietcomic.util.SimpleAppLog;

/**
 * Created by cmg on 18/08/15.
 */
public class BroadcastHelper {

    public interface OnComicBookUpdated {
        void onUpdated(ComicBook comicBook);
    }

    public interface OnComicChapterUpdated {
        void onUpdated(ComicChapter chapter);
    }

    public static final String ON_COMIC_BOOK_UPDATED = "ON_COMIC_BOOK_UPDATED";

    public static final String ON_COMIC_CHAPTER_UPDATED = "ON_COMIC_CHAPTER_UPDATED";

    private final Context context;

    private final Gson gson;

    private BroadcastReceiver onComicBookUpdatedReceiver;

    private BroadcastReceiver onComicChapterUpdateReceiver;

    public BroadcastHelper(Context context) {
        this.context = context;
        gson = new Gson();
    }

    public void registerOnComicChapterUpdated(final OnComicChapterUpdated onComicChapterUpdated) {
        try {
            if (onComicChapterUpdateReceiver != null) {
                try {
                    this.context.unregisterReceiver(onComicChapterUpdateReceiver);
                } catch (Exception e) {}
            }
            onComicChapterUpdateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (onComicChapterUpdated != null) {
                        String raw = intent.getExtras().getString(ComicChapter.class.getName());
                        //SimpleAppLog.debug("On comic book updated: " + raw);
                        onComicChapterUpdated.onUpdated(
                                gson.fromJson(raw,
                                        ComicChapter.class));
                    }
                }
            };
            this.context.registerReceiver(onComicChapterUpdateReceiver, new IntentFilter(ON_COMIC_CHAPTER_UPDATED));
        } catch (Exception e) {
            SimpleAppLog.error("Could not register receiver " + ON_COMIC_CHAPTER_UPDATED, e);
        }
    }

    public void registerOnComicBookUpdated(final OnComicBookUpdated onComicBookUpdated) {
        try {
            if (onComicBookUpdatedReceiver != null) {
                try {
                    this.context.unregisterReceiver(onComicBookUpdatedReceiver);
                } catch (Exception e) {}
            }
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
            if (onComicBookUpdatedReceiver != null) {
                this.context.unregisterReceiver(onComicBookUpdatedReceiver);
                onComicBookUpdatedReceiver = null;
            }
        } catch (Exception e) {
            SimpleAppLog.error("Could not unregister receiver " + ON_COMIC_BOOK_UPDATED, e);
        }
        try {
            if (onComicChapterUpdateReceiver != null) {
                this.context.unregisterReceiver(onComicChapterUpdateReceiver);
                onComicChapterUpdateReceiver = null;
            }
        } catch (Exception e) {
            SimpleAppLog.error("Could not unregister receiver " + ON_COMIC_BOOK_UPDATED, e);
        }
    }

    public void sendComicUpdate(ComicBook comicBook) {
        Intent intent = new Intent(ON_COMIC_BOOK_UPDATED);
        intent.putExtra(ComicBook.class.getName(), gson.toJson(comicBook));
        context.sendBroadcast(intent);
    }

    public void sendComicChaptersUpdate(ComicChapter chapter) {
        Intent intent = new Intent(ON_COMIC_CHAPTER_UPDATED);
        intent.putExtra(ComicChapter.class.getName(), gson.toJson(chapter));
        context.sendBroadcast(intent);
    }
}
