package com.halosolutions.vietcomic.service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.comic.ComicChapter;
import com.halosolutions.vietcomic.comic.ComicService;
import com.halosolutions.vietcomic.sqlite.ext.ComicBookDBAdapter;
import com.halosolutions.vietcomic.sqlite.ext.ComicChapterDBAdapter;
import com.halosolutions.vietcomic.util.SimpleAppLog;

import java.util.Date;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by luhonghai on 8/19/15.
 */
public class ComicUpdateService extends Service {

    private static final int REQUEST_DATA_TIMEOUT = 30 * 60 * 1000;

    private static final int UPDATE_POOL_SIZE = 3;

    private class ChapterCount {
        int count = 0;
    }

    private final Map<String, Future> updateQueue = new WeakHashMap<String, Future>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(UPDATE_POOL_SIZE);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Gson gson = new Gson();
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.containsKey(ComicBook.class.getName())) {
                final ComicBook comicBook = gson.fromJson(bundle.getString(ComicBook.class.getName()), ComicBook.class);
                synchronized (updateQueue) {
                    if (comicBook != null && !updateQueue.containsKey(comicBook.getBookId())) {
                        ComicChapterDBAdapter chapterDBAdapter = new ComicChapterDBAdapter(this);
                        Cursor cursorChapters = null;
                        try {
                            chapterDBAdapter.open();
                            cursorChapters = chapterDBAdapter.listByComic(comicBook);
                            cursorChapters.moveToFirst();
                            Date lastTimestamp = comicBook.getTimestamp();
                            if (cursorChapters.getCount() == 0 || lastTimestamp == null ||
                                    (System.currentTimeMillis() - lastTimestamp.getTime() > REQUEST_DATA_TIMEOUT)) {
                                SimpleAppLog.info("Submit comic book update: " + comicBook.getUrl() + ". Name: " + comicBook.getName());
                                updateQueue.put(comicBook.getBookId(),
                                        executorService.submit(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    doUpdateComicBook(comicBook.getBookId());
                                                } catch (Exception e) {
                                                    SimpleAppLog.error("Could not update comic book", e);
                                                } finally {
                                                    updateQueue.remove(comicBook.getBookId());
                                                }
                                            }
                                        }));
                            } else {
                                SimpleAppLog.info("Skip upload book info. " + comicBook.getUrl());
                            }
                        } catch (Exception e) {
                            SimpleAppLog.error("Could not check update book", e);
                        } finally {
                            if (cursorChapters != null)
                                cursorChapters.close();
                            chapterDBAdapter.close();
                        }

                    }
                }
            }
        } catch (Exception e) {
            SimpleAppLog.error("Could not start command update book",e);
        }
        return START_STICKY;
    }

    private void doUpdateComicBook(String bookId) throws Exception {
        final BroadcastHelper broadcastHelper =  new BroadcastHelper(getApplicationContext());
        final ComicBookDBAdapter comicBookDBAdapter =  new ComicBookDBAdapter(getApplicationContext());
        final ComicChapterDBAdapter comicChapterDBAdapter = new ComicChapterDBAdapter(getApplicationContext());
        try {
            comicBookDBAdapter.open();
            comicChapterDBAdapter.open();
            final ComicBook comicBook = comicBookDBAdapter.getComicByBookId(bookId);
            if (comicBook == null) {
                SimpleAppLog.error("No comic book found with id: " + bookId);
                return;
            }
            SimpleAppLog.info("Receive update comic book request: " + comicBook.getName()
                    + ". URL: " + comicBook.getUrl());
            ComicService comicService = ComicService.getService(getApplicationContext(), comicBook);

            if (comicService != null) {
                final ChapterCount count = new ChapterCount();
                comicService.fetchChapter(comicBook, new ComicService.FetchChapterListener() {
                    @Override
                    public void onChapterFound(ComicChapter chapter) {
                        try {
                            count.count++;
                            ComicChapter oldChapter = comicChapterDBAdapter.getByChapterId(chapter.getChapterId());
                            if (oldChapter != null) {
                                chapter.setId(oldChapter.getId());
                                chapter.setStatus(oldChapter.getStatus());
                                chapter.setFilePath(oldChapter.getFilePath());
                                chapter.setImageCount(oldChapter.getImageCount());
                                chapter.setCompletedCount(oldChapter.getCompletedCount());
                                chapter.setTimestamp(oldChapter.getTimestamp());
                                comicChapterDBAdapter.update(chapter);
                            } else {
                                chapter.setTimestamp(new Date(System.currentTimeMillis()));
                                comicChapterDBAdapter.insert(chapter);
                            }
                            if (count.count % 20 == 0) {
                                broadcastHelper.sendComicChaptersUpdate(chapter);
                            }
                        } catch (Exception e) {
                            SimpleAppLog.error("Could not put chapter to database. " + chapter.getName(),e);
                        }
                    }

                    @Override
                    public void onDescriptionFound(String description) {
                        comicBook.setDescription(description);
                        comicBook.setTimestamp(new Date(System.currentTimeMillis()));
                        try {
                            if (comicBookDBAdapter.update(comicBook)) {
                                broadcastHelper.sendComicUpdate(comicBook);
                            }
                        } catch (Exception e) {
                            SimpleAppLog.error("Could not update comic book", e);
                        }
                    }
                });
                broadcastHelper.sendComicChaptersUpdate(new ComicChapter(comicBook.getBookId()));
            } else {
                SimpleAppLog.error("No comic service found for source: " + comicBook.getSource());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                broadcastHelper.unregister();
                comicBookDBAdapter.close();
                comicChapterDBAdapter.close();
            } catch (Exception e) {}
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
