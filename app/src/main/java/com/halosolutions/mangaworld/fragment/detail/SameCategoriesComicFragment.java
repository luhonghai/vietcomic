package com.halosolutions.mangaworld.fragment.detail;

import android.database.Cursor;

import com.halosolutions.mangaworld.R;

/**
 * Created by cmg on 19/08/15.
 */
public class SameCategoriesComicFragment extends DetailComicFragment {

    @Override
    protected Cursor getCursor() throws Exception {
        return comicBookDBAdapter.cursorByCategories(getComicBook().getCategories());
    }

    @Override
    protected int getItemLayout() {
        return R.layout.comic_item_lite;
    }
}
