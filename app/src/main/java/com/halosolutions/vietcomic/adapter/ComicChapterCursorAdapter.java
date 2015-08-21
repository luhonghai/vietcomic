package com.halosolutions.vietcomic.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.halosolutions.vietcomic.R;
import com.halosolutions.vietcomic.comic.ComicChapter;
import com.halosolutions.vietcomic.sqlite.ext.ComicChapterDBAdapter;
import com.halosolutions.vietcomic.util.DateHelper;
import com.halosolutions.vietcomic.view.EllipsizingTextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by cmg on 19/08/15.
 */
public class ComicChapterCursorAdapter extends CursorAdapter {

    private LayoutInflater layoutInflater;

    private ComicChapterDBAdapter dbAdapter;

    private SimpleDateFormat sdf;

    public ComicChapterCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        layoutInflater = LayoutInflater.from(context);
        dbAdapter = new ComicChapterDBAdapter(context);
        sdf = new SimpleDateFormat(DateHelper.DISPLAY_DATE_FORMAT, Locale.getDefault());
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.comic_chapter_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ComicChapter comicChapter = dbAdapter.toObject(cursor);
        updateView(context, view, comicChapter);
    }

    public void updateView(Context context, View view, ComicChapter comicChapter) {
        view.setTag(comicChapter);
        EllipsizingTextView textName = (EllipsizingTextView) view.findViewById(R.id.txtName);
        textName.setText(comicChapter.getName());
        if (comicChapter.getPublishDate() != null)
            ((TextView) view.findViewById(R.id.txtPublishDate)).setText(sdf.format(comicChapter.getPublishDate()));

        NumberProgressBar progressBar = (NumberProgressBar) view.findViewById(R.id.progressDownload);
        if (progressBar != null) {
            progressBar.setMax(100);
            if (comicChapter.getStatus() == ComicChapter.STATUS_DOWNLOADING
                    || comicChapter.getStatus() == ComicChapter.STATUS_INIT_DOWNLOADING
                    || comicChapter.getStatus() == ComicChapter.STATUS_DOWNLOAD_JOINING) {
                progressBar.setVisibility(View.VISIBLE);
                if (comicChapter.getImageCount() == 0) {
                    progressBar.setProgress(0);
                } else {
                    progressBar.setProgress(Math.round(100 * (float)comicChapter.getCompletedCount() / comicChapter.getImageCount()));
                }
            } else {
                progressBar.setVisibility(View.GONE);
            }
        }
        if (ComicChapter.STATUS_DOWNLOAD_FAILED == comicChapter.getStatus()) {
            textName.setTextColor(context.getResources().getColor(R.color.colorError));
        } else {
            textName.setTextColor(context.getResources().getColor(R.color.colorBlack));
        }

        if (comicChapter.getStatus() == ComicChapter.STATUS_NEW) {
            textName.setTypeface(null, Typeface.BOLD);
        } else {
            textName.setTypeface(null, Typeface.NORMAL);
        }
        view.findViewById(R.id.imgStatusDownloaded).setVisibility(
                comicChapter.getStatus() == ComicChapter.STATUS_DOWNLOADED ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.imgStatusEye).setVisibility(
                comicChapter.getStatus() == ComicChapter.STATUS_READED ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.imgStatusDownloadFailed).setVisibility(
                comicChapter.getStatus() == ComicChapter.STATUS_DOWNLOAD_FAILED ? View.VISIBLE : View.GONE);
    }
}
