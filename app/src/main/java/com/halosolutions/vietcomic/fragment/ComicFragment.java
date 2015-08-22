package com.halosolutions.vietcomic.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
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

	private static final int REFRESH_UI_TIME = 400;

	protected ComicBookDBAdapter comicBookDBAdapter;

	protected BroadcastHelper broadcastHelper;

	private Handler handlerUIUpdate = new Handler();

	private boolean isRunning;

	private Runnable runnableUIUpdate = new Runnable() {
		@Override
		public void run() {
			if (needUpdate && isRunning) {
				needUpdate = false;
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							reloadListView();
						} catch (Exception e) {
							SimpleAppLog.error("Could not reload list view",e);
						}
					}
				});
			}
			handlerUIUpdate.postDelayed(runnableUIUpdate, REFRESH_UI_TIME);
		}
	};

	private boolean needUpdate = false;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(getViewLayout(), container, false);
		final View view = v.findViewById(R.id.listComic);
		try {
			if (view == null) return v;
			int itemLayout = getItemLayout();
			if (itemLayout == -1) {
				itemLayout = R.layout.comic_item;
				if (view instanceof GridView) {
					itemLayout = R.layout.comic_item_grid;
				}
			}
			ComicBookCursorAdapter bookCursorAdapter = new ComicBookCursorAdapter(getActivity(), getCursor(), itemLayout);
			((AbsListView) view).setAdapter(bookCursorAdapter);
			((AbsListView) view).setOnItemClickListener(this);
			((AbsListView) view).setEmptyView(v.findViewById(R.id.txtEmpty));
		} catch (Exception e) {
			SimpleAppLog.error("Could not list comic", e);
		}
		return v;
	}

	protected int getItemLayout() {
		return -1;
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
		if (reload)
			setNeedUpdate(true);
	}

	protected void reloadListView() throws Exception {
		final View root = getView();
		if (root != null) {
			final AbsListView listView = (AbsListView) root.findViewById(R.id.listComic);
			if (listView != null) {
				final CursorAdapter cursorAdapter = (CursorAdapter) listView.getAdapter();
				if (cursorAdapter != null) {
					cursorAdapter.changeCursor(getCursor());
				}
			}
		}
	}

	protected void setNeedUpdate(boolean needUpdate) {
		this.needUpdate = needUpdate;
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
		handlerUIUpdate.postDelayed(runnableUIUpdate, REFRESH_UI_TIME);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		handlerUIUpdate.removeCallbacks(runnableUIUpdate);
		if (comicBookDBAdapter != null) {
			comicBookDBAdapter.close();
		}
		broadcastHelper.unregister();
	}

	@Override
	public void onResume() {
		super.onResume();
		isRunning = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		isRunning = false;
	}
}
