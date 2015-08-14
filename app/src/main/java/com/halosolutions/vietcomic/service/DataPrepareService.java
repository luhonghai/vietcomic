package com.halosolutions.vietcomic.service;

import android.content.Context;

import com.halosolutions.vietcomic.sqlite.ext.ComicBookDBAdapter;
import com.halosolutions.vietcomic.util.SimpleAppLog;

import org.apache.commons.io.IOUtils;

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

            if (comicBookDBAdapter.count() == 0) {

            }

        } catch (Exception e) {
            SimpleAppLog.error("Could not prepare comic books",e);
        } finally {
            comicBookDBAdapter.close();
        }
    }
}
