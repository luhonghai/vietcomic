package com.halosolutions.mangaworld.service;

import android.content.Context;

import com.halosolutions.mangaworld.comic.ComicBook;
import com.halosolutions.mangaworld.comic.ComicVersion;
import com.halosolutions.mangaworld.sqlite.ext.ComicBookDBAdapter;
import com.halosolutions.mangaworld.util.SimpleAppLog;
import com.luhonghai.litedb.exception.AnnotationNotFound;
import com.luhonghai.litedb.exception.InvalidAnnotationData;

import java.util.List;

/**
 * Created by cmg on 14/08/15.
 */
public class DataPrepareService {

    private final Context context;

    private ComicBookDBAdapter comicBookDBAdapter;


    public DataPrepareService(Context context) {
        this.context = context;
        try {
            comicBookDBAdapter = new ComicBookDBAdapter(context);
        } catch (Exception e) {
            SimpleAppLog.error("Could not open database", e);
        }
    }

    public void prepare() {
        try {
            comicBookDBAdapter.open();
            ComicVersion comicVersion = ComicVersion.getComicVersion(context);
            if (comicBookDBAdapter.count() == 0) {
                List<ComicBook> books = ComicVersion.getBookData(context, comicVersion);
                if (books != null && books.size() > 0) {
                    comicBookDBAdapter.bulkInsert(books, true);
                }
            }
        } catch (Exception e) {
            SimpleAppLog.error("Could not prepare comic books", e);
        } finally {
            comicBookDBAdapter.close();
        }
    }
}
