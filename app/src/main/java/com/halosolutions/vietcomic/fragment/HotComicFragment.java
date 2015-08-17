package com.halosolutions.vietcomic.fragment;

import android.database.Cursor;

public class HotComicFragment extends ComicFragment {

	@Override
	protected Cursor getCursor() throws Exception {
		return comicBookDBAdapter.cursorAllHot();
	}
}
