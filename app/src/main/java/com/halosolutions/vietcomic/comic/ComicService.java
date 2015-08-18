package com.halosolutions.vietcomic.comic;

import android.content.Context;

import com.cmg.android.cmgpdf.AsyncTask;
import com.halosolutions.vietcomic.util.Hash;
import com.halosolutions.vietcomic.util.SimpleAppLog;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

/**
 * Created by cmg on 11/08/15.
 */
public abstract class ComicService {

    public interface DownloadListener {
        public void onError(String message, Throwable e);

        public void onComplete(ComicChapter chapter);
    }

    private final Context context;

    public ComicService(Context context) {
        this.context = context;
    }

    protected Context getContext() {
        return this.context;
    }

    public void downloadAsync(final ComicChapter chapter, final DownloadListener listener) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    listener.onComplete(downloadBook(chapter));
                } catch (Exception e) {
                    listener.onError("Could not download comic", e);
                }
                return null;
            }
        }.execute();
    }

    public abstract ComicChapter downloadBook(final ComicChapter chapter) throws Exception;
}
