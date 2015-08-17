package com.halosolutions.vietcomic.fragment;

import android.database.Cursor;

public class NewComicFragment extends ComicFragment {
	@Override
	protected Cursor getCursor() throws Exception {
		return comicBookDBAdapter.cursorAllNew();
	}
}
