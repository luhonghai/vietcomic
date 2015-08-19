package com.halosolutions.vietcomic.fragment.detail;

import android.database.Cursor;

/**
 * Created by cmg on 19/08/15.
 */
public class AllChapterComicFragment extends DetailComicFragment {

    @Override
    protected Cursor getCursor() throws Exception {
        return comicBookDBAdapter.cursorAllHot();
    }
}
