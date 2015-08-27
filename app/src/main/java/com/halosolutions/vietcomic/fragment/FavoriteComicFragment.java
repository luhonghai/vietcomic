package com.halosolutions.vietcomic.fragment;

import android.database.Cursor;

import com.halosolutions.vietcomic.R;

public class FavoriteComicFragment extends ComicFragment {

	@Override
	protected Cursor getCursor() throws Exception {
		return comicBookDBAdapter.cursorAllFavorites();
	}

	@Override
	protected int getViewLayout() {
		return R.layout.fragment_comic_grid;
	}
}
