package com.halosolutions.vietcomic.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        ((EllipsizingTextView) view.findViewById(R.id.txtName)).setText(comicChapter.getName());
        if (comicChapter.getPublishDate() != null)
            ((TextView) view.findViewById(R.id.txtPublishDate)).setText(sdf.format(comicChapter.getPublishDate()));
    }
}
