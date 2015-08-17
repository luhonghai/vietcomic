package com.halosolutions.vietcomic.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.halosolutions.vietcomic.R;
import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.sqlite.ext.ComicBookDBAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by cmg on 17/08/15.
 */
public class ComicBookCursorAdapter extends CursorAdapter {

    private LayoutInflater layoutInflater;

    private ComicBookDBAdapter comicBookDBAdapter;

    private DisplayImageOptions displayImageOptions;

    public ComicBookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        layoutInflater = LayoutInflater.from(context);
        comicBookDBAdapter = new ComicBookDBAdapter(context);
        displayImageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.comic_thumbnail_default) // resource or drawable
                .showImageForEmptyUri(R.drawable.comic_thumbnail_default) // resource or drawable
                .showImageOnFail(R.drawable.comic_thumbnail_error) // resource or drawable
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.comic_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ComicBook comicBook = comicBookDBAdapter.toObject(cursor);
        ((TextView) view.findViewById(R.id.txtName)).setText(comicBook.getName());
        String thumbnail = comicBook.getThumbnail();
        ImageLoader.getInstance().displayImage(thumbnail,
                (ImageView) view.findViewById(R.id.thumbnail),
                displayImageOptions);
    }

}
