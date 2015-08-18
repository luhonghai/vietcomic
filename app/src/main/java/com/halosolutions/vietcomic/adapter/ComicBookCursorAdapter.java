package com.halosolutions.vietcomic.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.halosolutions.vietcomic.R;
import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.sqlite.ext.ComicBookDBAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.DecimalFormat;

/**
 * Created by cmg on 17/08/15.
 */
public class ComicBookCursorAdapter extends CursorAdapter {

    private LayoutInflater layoutInflater;

    private ComicBookDBAdapter comicBookDBAdapter;

    private DisplayImageOptions displayImageOptions;

    private int itemLayout;



    public ComicBookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        itemLayout = R.layout.comic_item;
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

    public ComicBookCursorAdapter(Context context, Cursor c, int itemLayout) {
        this(context, c);
        this.itemLayout = itemLayout;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(itemLayout, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ComicBook comicBook = comicBookDBAdapter.toObject(cursor);
        updateView(view, comicBook);
    }

    public void updateView(final View view, final ComicBook comicBook) {
        ((TextView) view.findViewById(R.id.txtName)).setText(comicBook.getName());
        RelativeLayout rlComicRate = (RelativeLayout) view.findViewById(R.id.rlComicRate);
        if (rlComicRate != null) {
            float rate = comicBook.getRate();
            if (rate > 0.0) {
                String strRate;
                if (rate == 10.0) {
                    strRate = "10";
                } else {
                    strRate = new DecimalFormat("#.##").format(rate);
                }
                view.findViewById(R.id.rlComicRate).setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.txtComicRate)).setText(strRate);
            } else {
                view.findViewById(R.id.rlComicRate).setVisibility(View.GONE);
            }
        }
        if (comicBook.isFavorite()) {
            view.findViewById(R.id.imgFavorite).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.imgFavorite).setVisibility(View.GONE);
        }

        if (comicBook.isDownloaded()) {
            view.findViewById(R.id.imgDownloaded).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.imgDownloaded).setVisibility(View.GONE);
        }

        if (comicBook.isWatched()) {
            view.findViewById(R.id.imgWatched).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.imgWatched).setVisibility(View.GONE);
        }
        final ImageView imgThumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        String thumbnail = comicBook.getThumbnail();
        ImageLoader.getInstance().displayImage(thumbnail,
                imgThumbnail,
                displayImageOptions);
        view.setTag(comicBook);
    }
}
