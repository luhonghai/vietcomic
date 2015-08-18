package com.halosolutions.vietcomic.fragment;

import android.database.Cursor;

import com.halosolutions.vietcomic.R;

public class NewComicFragment extends ComicFragment {
	@Override
	protected Cursor getCursor() throws Exception {
		return comicBookDBAdapter.cursorAllNew();
	}

	@Override
	protected int getViewLayout() {
		return R.layout.fragment_comic_grid;
	}
}
