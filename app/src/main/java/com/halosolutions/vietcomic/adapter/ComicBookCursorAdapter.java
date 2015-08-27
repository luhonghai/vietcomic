package com.halosolutions.vietcomic.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.halosolutions.vietcomic.R;
import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.sqlite.ext.ComicBookDBAdapter;
import com.halosolutions.vietcomic.util.AndroidHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

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
        RelativeLayout rlFavorite = (RelativeLayout) view.findViewById(R.id.rlFavorite);
        if (rlFavorite != null) {
            if (comicBook.isFavorite()) {
                rlFavorite.setVisibility(View.VISIBLE);
            } else {
                rlFavorite.setVisibility(View.GONE);
            }
        }

        LinearLayout llRateStar = (LinearLayout) view.findViewById(R.id.llRateStar);
        if (llRateStar != null) {
            AndroidHelper.showRateStar(mContext, llRateStar, comicBook.getRate());
        }

        final ImageView imgThumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        if (AndroidHelper.isLowerThanApiLevel11()) {
            imgThumbnail.setBackgroundDrawable(null);
        }
        String thumbnail = comicBook.getThumbnail();
        ImageLoader.getInstance().displayImage(thumbnail,
                imgThumbnail,
                displayImageOptions);
        view.setTag(comicBook);
    }

}
