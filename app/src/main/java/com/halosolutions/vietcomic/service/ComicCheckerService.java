package com.halosolutions.vietcomic.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;

import com.google.gson.Gson;
import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.comic.ComicService;
import com.halosolutions.vietcomic.sqlite.ext.ComicBookDBAdapter;
import com.halosolutions.vietcomic.util.SimpleAppLog;

/**
 * Created by cmg on 24/08/15.
 */
public class ComicCheckerService extends IntentService {

    private static class CheckStatus {
        boolean isCleanHot = false;
        boolean isCleanNew = false;
    }

    public ComicCheckerService() {
        super(ComicCheckerService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SimpleAppLog.info("Start comic checker service");
        final ComicBookDBAdapter dbAdapter = new ComicBookDBAdapter(getApplicationContext());
        Gson gson = new Gson();
        Cursor favoriteBooks = null;
        try {
            dbAdapter.open();
            final CheckStatus checkStatus = new CheckStatus();
            for (String comicSource : ComicService.ALL_SOURCES) {
                try {
                    ComicService.getService(this, new ComicBook(comicSource)).fetchHotAndNewComic(new ComicService.FetchHotAndNewListener() {
                        @Override
                        public void onHotComicFound(String bookId) {
                            if (!checkStatus.isCleanHot) {
                                try {
                                    //dbAdapter.cleanHotComic();
                                    checkStatus.isCleanHot = true;
                                } catch (Exception e) {
                                    SimpleAppLog.error("Could not clean all hot comic", e);
                                }
                            }
                            try {
                                ComicBook comicBook = dbAdapter.getComicByBookId(bookId);
                                if (comicBook != null) {
                                    comicBook.setIsHot(true);
                                    dbAdapter.update(comicBook);
                                }
                            } catch (Exception e) {
                                SimpleAppLog.error("Could not update comic book", e);
                            }
                        }

                        @Override
                        public void onNewComicFound(String bookId) {
                            if (!checkStatus.isCleanNew) {
                                try {
                                    //dbAdapter.cleanNewComic();
                                    checkStatus.isCleanNew = true;
                                } catch (Exception e) {
                                    SimpleAppLog.error("Could not clean all new comic", e);
                                }
                            }
                            try {
                                ComicBook comicBook = dbAdapter.getComicByBookId(bookId);
                                if (comicBook != null) {
                                    comicBook.setIsNew(true);
                                    dbAdapter.update(comicBook);
                                }
                            } catch (Exception e) {
                                SimpleAppLog.error("Could not update comic book", e);
                            }
                        }
                    });
                } catch (Exception e) {
                    SimpleAppLog.error("Could not update comic hot and new source", e);
                }
            }
            if (checkStatus.isCleanNew || checkStatus.isCleanHot) {
                BroadcastHelper broadcastHelper = new BroadcastHelper(this);
                broadcastHelper.sendComicUpdate(new ComicBook());
            }

            favoriteBooks = dbAdapter.cursorAllFavorites();
            favoriteBooks.moveToFirst();
            while (!favoriteBooks.isAfterLast()) {
                ComicBook comicBook = dbAdapter.toObject(favoriteBooks);
                Intent updateBookIntent = new Intent(this, ComicUpdateService.class);
                updateBookIntent.putExtra(ComicBook.class.getName(), gson.toJson(comicBook));
                startService(updateBookIntent);
                favoriteBooks.moveToNext();
            }
        } catch (Exception e) {
            SimpleAppLog.error("Could not check comic book",e);
        } finally {
            if (favoriteBooks != null)  {
                try {
                    favoriteBooks.close();
                } catch (Exception e) {}
            }
            dbAdapter.close();
        }
        // Start check for download
        Intent comicDownloadIntent = new Intent(this, ComicDownloaderService.class);
        comicDownloadIntent.putExtra(ComicDownloaderService.Action.class.getName(), ComicDownloaderService.Action.CHECK);
        startService(comicDownloadIntent);
    }
}
