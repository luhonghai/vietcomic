package com.halosolutions.mangaworld.adapter;

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

import com.halosolutions.mangaworld.R;
import com.halosolutions.mangaworld.comic.ComicBook;
import com.halosolutions.mangaworld.sqlite.ext.ComicBookDBAdapter;
import com.halosolutions.mangaworld.util.AndroidHelper;
import com.halosolutions.mangaworld.util.SimpleAppLog;
import com.luhonghai.litedb.exception.AnnotationNotFound;
import com.luhonghai.litedb.exception.InvalidAnnotationData;
import com.luhonghai.litedb.exception.LiteDatabaseException;
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
        try {
            comicBookDBAdapter = new ComicBookDBAdapter(context);
        } catch (Exception e) {
            SimpleAppLog.error("Could not open database", e);
        }
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
        ComicBook comicBook = null;
        try {
            comicBook = comicBookDBAdapter.toObject(cursor);
        } catch (Exception e) {
            SimpleAppLog.error("Could not open database",e);
        }
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
