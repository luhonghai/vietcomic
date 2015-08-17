package com.halosolutions.vietcomic.fragment;

import android.database.Cursor;

public class FavoriteComicFragment extends ComicFragment {

	@Override
	protected Cursor getCursor() throws Exception {
		return comicBookDBAdapter.cursorAllFavorites();
	}
}
