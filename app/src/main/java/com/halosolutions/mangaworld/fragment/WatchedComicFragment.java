package com.halosolutions.mangaworld.fragment;

import android.database.Cursor;

import com.halosolutions.mangaworld.R;

public class WatchedComicFragment extends ComicFragment {

	@Override
	protected Cursor getCursor() throws Exception {
		return comicBookDBAdapter.cursorWatched();
	}

	@Override
	protected int getViewLayout() {
		return R.layout.fragment_comic_grid;
	}
}
