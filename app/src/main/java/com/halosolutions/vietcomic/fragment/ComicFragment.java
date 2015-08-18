package com.halosolutions.vietcomic.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.gson.Gson;
import com.halosolutions.vietcomic.DetailActivity;
import com.halosolutions.vietcomic.R;
import com.halosolutions.vietcomic.adapter.ComicBookCursorAdapter;
import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.service.BroadcastHelper;
import com.halosolutions.vietcomic.sqlite.ext.ComicBookDBAdapter;
import com.halosolutions.vietcomic.util.SimpleAppLog;

import java.sql.SQLException;

public abstract class ComicFragment extends Fragment implements AdapterView.OnItemClickListener {

	protected ComicBookDBAdapter comicBookDBAdapter;

	private BroadcastHelper broadcastHelper;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(getViewLayout(), container, false);
		final View view = v.findViewById(R.id.listComic);
		try {
			if (view == null) return v;
			int itemLayout = R.layout.comic_item;
			if (view instanceof GridView) {
				itemLayout = R.layout.comic_item_grid;
			}
			ComicBookCursorAdapter bookCursorAdapter = new ComicBookCursorAdapter(getActivity(), getCursor(), itemLayout);
			((AbsListView) view).setAdapter(bookCursorAdapter);
			((AbsListView) view).setOnItemClickListener(this);
		} catch (Exception e) {
			SimpleAppLog.error("Could not list comic", e);
		}
		return v;
	}

	protected int getViewLayout() {
		return R.layout.fragment_comic_list;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Gson gson = new Gson();
		Intent intent = new Intent(getActivity(), DetailActivity.class);
		intent.putExtra(ComicBook.class.getName(), gson.toJson(view.getTag()));
		startActivity(intent);
	}

	protected void onUpdateComic(ComicBook comicBook, boolean reload) throws Exception {
		final View root = getView();
		if (root != null) {
			final AbsListView listView = (AbsListView) root.findViewById(R.id.listComic);
			if (listView != null) {
				final ComicBookCursorAdapter bookCursorAdapter = (ComicBookCursorAdapter) listView.getAdapter();
				if (bookCursorAdapter != null) {
					if (reload) {
						bookCursorAdapter.changeCursor(getCursor());
					} else {
						int count = listView.getChildCount();
						SimpleAppLog.debug("Listview child count: " + count);
						if (count > 0) {
							for (int i = 0; i < count; i++) {
								final View v = listView.getChildAt(i);
								ComicBook oldObj = (ComicBook) v.getTag();
								if (oldObj.getBookId().equals(comicBook.getBookId())) {
									SimpleAppLog.debug("Found matched view");
									bookCursorAdapter.updateView(v, comicBook);
								}
							}
						}
					}
				}
			}
		}
	}

	protected abstract Cursor getCursor() throws Exception;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		broadcastHelper = new BroadcastHelper(getActivity());
		comicBookDBAdapter = new ComicBookDBAdapter(getActivity());
		try {
			comicBookDBAdapter.open();
		} catch (SQLException e) {
			SimpleAppLog.error("Could not open comic book database",e);
		}
		broadcastHelper.registerOnComicBookUpdated(new BroadcastHelper.OnComicBookUpdated() {
			@Override
			public void onUpdated(ComicBook comicBook) {
				try {
					onUpdateComic(comicBook, true);
				} catch (Exception e) {
					SimpleAppLog.error("Could not update list view with new comic book", e);
				}
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (comicBookDBAdapter != null) {
			comicBookDBAdapter.close();
		}
		broadcastHelper.unregister();
	}
}
