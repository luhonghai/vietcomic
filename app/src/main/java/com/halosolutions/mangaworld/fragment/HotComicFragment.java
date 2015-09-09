package com.halosolutions.mangaworld.fragment;

import android.database.Cursor;

import com.halosolutions.mangaworld.R;

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
