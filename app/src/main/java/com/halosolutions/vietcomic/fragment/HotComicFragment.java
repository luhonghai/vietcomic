package com.halosolutions.vietcomic.fragment;

import android.database.Cursor;

import com.halosolutions.vietcomic.R;

public class HotComicFragment extends ComicFragment {

	@Override
	protected Cursor getCursor() throws Exception {
		return comicBookDBAdapter.cursorAllHot();
	}

	@Override
	protected int getViewLayout() {
		return R.layout.fragment_comic_grid;
	}
}
