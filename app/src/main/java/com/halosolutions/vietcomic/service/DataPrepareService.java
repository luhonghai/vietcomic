package com.halosolutions.vietcomic.service;

import android.content.Context;

import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.comic.ComicVersion;
import com.halosolutions.vietcomic.sqlite.ext.ComicBookDBAdapter;
import com.halosolutions.vietcomic.util.SimpleAppLog;

import java.util.List;

/**
 * Created by cmg on 14/08/15.
 */
public class DataPrepareService {

    private final Context context;

    private ComicBookDBAdapter comicBookDBAdapter;


    public DataPrepareService(Context context) {
        this.context = context;
        comicBookDBAdapter = new ComicBookDBAdapter(context);
    }

    public void prepare() {
        try {
            comicBookDBAdapter.open();
            ComicVersion comicVersion = ComicVersion.getComicVersion(context);
            ComicVersion latestVersion = ComicVersion.fetchComicVersion(context);
            List<ComicBook> books = null;
            if (comicBookDBAdapter.count() == 0) {
                books = ComicVersion.getBookData(context, comicVersion);
            } else if (latestVersion != null
                        && comicVersion != null
                        && latestVersion.getVersion() > comicVersion.getVersion()) {
                books = ComicVersion.getBookData(context, latestVersion);
                ComicVersion.saveComicVersion(context, latestVersion);
                ComicVersion.deleteBookData(context, comicVersion);
            }
            if (books != null && books.size() > 0) {
                comicBookDBAdapter.bulkInsert(books);
            }
        } catch (Exception e) {
            SimpleAppLog.error("Could not prepare comic books", e);
        } finally {
            comicBookDBAdapter.close();
        }
    }
}
