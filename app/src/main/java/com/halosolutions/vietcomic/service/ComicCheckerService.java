package com.halosolutions.vietcomic.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;

import com.google.gson.Gson;
import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.sqlite.ext.ComicBookDBAdapter;
import com.halosolutions.vietcomic.util.SimpleAppLog;

/**
 * Created by cmg on 24/08/15.
 */
public class ComicCheckerService extends IntentService {

    public ComicCheckerService() {
        super(ComicCheckerService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SimpleAppLog.info("Start comic checker service");
        ComicBookDBAdapter dbAdapter = new ComicBookDBAdapter(getApplicationContext());
        Gson gson = new Gson();
        Cursor favoriteBooks = null;
        try {
            dbAdapter.open();
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
        comicDownloadIntent.putExtra(ComicDownloaderService.Action.class.getName(), ComicDownloaderService.Action.DOWNLOAD);
        startService(comicDownloadIntent);
    }
}
