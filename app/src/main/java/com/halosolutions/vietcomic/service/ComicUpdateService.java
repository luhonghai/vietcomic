package com.halosolutions.vietcomic.service;

import android.app.IntentService;
import android.content.Intent;

import com.google.gson.Gson;
import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.comic.ComicChapter;
import com.halosolutions.vietcomic.comic.ComicService;
import com.halosolutions.vietcomic.sqlite.ext.ComicBookDBAdapter;
import com.halosolutions.vietcomic.sqlite.ext.ComicChapterDBAdapter;
import com.halosolutions.vietcomic.util.SimpleAppLog;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by luhonghai on 8/19/15.
 */
public class ComicUpdateService extends IntentService {

    private BroadcastHelper broadcastHelper;

    private ComicBookDBAdapter comicBookDBAdapter;

    private ComicChapterDBAdapter comicChapterDBAdapter;

    public ComicUpdateService() {
        super(ComicUpdateService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcastHelper = new BroadcastHelper(getApplicationContext());
        comicBookDBAdapter = new ComicBookDBAdapter(getApplicationContext());
        comicChapterDBAdapter = new ComicChapterDBAdapter(getApplicationContext());

        try {
            comicBookDBAdapter.open();
        } catch (SQLException e) {
            SimpleAppLog.error("Could not open book database",e);
        }

        try {
            comicChapterDBAdapter.open();
        } catch (SQLException e) {
            SimpleAppLog.error("Could not open book chapter database",e);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Gson gson = new Gson();
        long start =System.currentTimeMillis();
        ComicBook comicBook = gson.fromJson(intent.getStringExtra(ComicBook.class.getName()), ComicBook.class);
        SimpleAppLog.info("Receive update comic book request: " + comicBook.getName()
                        + ". URL: " + comicBook.getUrl());
        ComicService comicService = ComicService.getService(getApplicationContext(), comicBook);
        try {
            if (comicService != null) {
                List<ComicChapter> chapterList = comicService.fetchChapter(comicBook);
                if (chapterList != null && chapterList.size() > 0) {
                    SimpleAppLog.info("Found " + chapterList.size() + " chapters of book " + comicBook.getName());
                    for (ComicChapter chapter : chapterList) {
                        ComicChapter oldChapter = comicChapterDBAdapter.getByChapterId(chapter.getChapterId());
                        if (oldChapter != null) {
                            chapter.setId(oldChapter.getId());
                            chapter.setStatus(oldChapter.getStatus());
                            chapter.setFilePath(oldChapter.getFilePath());
                            chapter.setImageCount(oldChapter.getImageCount());
                            comicChapterDBAdapter.update(chapter);
                        } else {
                            comicChapterDBAdapter.insert(chapter);
                        }
                    }
                    try {
                        if (comicBookDBAdapter.update(comicBook)) {
                           broadcastHelper.sendComicUpdate(comicBook);
                        }
                    } catch (Exception e) {
                        SimpleAppLog.error("Could not update comic book", e);
                    }
                }
            } else {
                SimpleAppLog.error("No comic service found for source: " + comicBook.getSource());
            }
        } catch (Exception e) {
            SimpleAppLog.error("Could not fetch chapter list",e);
        } finally {
            SimpleAppLog.info("Receive update comic book request: " + comicBook.getName()
                    + ". URL: " + comicBook.getUrl()
                    + ". Execution time: " + (System.currentTimeMillis() - start) + "ms");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        broadcastHelper.unregister();
        comicBookDBAdapter.close();
        comicChapterDBAdapter.close();
    }
}
