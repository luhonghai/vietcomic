package com.halosolutions.vietcomic.fragment.detail;

import android.database.Cursor;

import com.halosolutions.vietcomic.R;

/**
 * Created by cmg on 19/08/15.
 */
public class SameAuthorComicFragment extends DetailComicFragment {

    @Override
    protected Cursor getCursor() throws Exception {
        return comicBookDBAdapter.cursorByAuthor(getComicBook().getAuthor());
    }

    @Override
    protected int getItemLayout() {
        return R.layout.comic_item_lite;
    }
}
