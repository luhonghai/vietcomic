package com.halosolutions.mangaworld.fragment;

import android.database.Cursor;

public class AllComicFragment extends ComicFragment {

	@Override
	protected Cursor getCursor() throws Exception {
		return comicBookDBAdapter.cursorSearch("");
	}
}
