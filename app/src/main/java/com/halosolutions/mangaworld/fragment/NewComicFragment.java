package com.halosolutions.mangaworld.fragment;

import android.database.Cursor;

import com.halosolutions.mangaworld.R;

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
