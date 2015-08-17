package com.halosolutions.vietcomic.fragment;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.halosolutions.vietcomic.R;
import com.halosolutions.vietcomic.adapter.ComicBookCursorAdapter;
import com.halosolutions.vietcomic.sqlite.ext.ComicBookDBAdapter;
import com.halosolutions.vietcomic.util.SimpleAppLog;

import java.sql.SQLException;

public abstract class ComicFragment extends Fragment {

	protected ComicBookDBAdapter comicBookDBAdapter;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_comic_list, container, false);
		ListView listView = (ListView) v.findViewById(R.id.listComic);
		try {
			ComicBookCursorAdapter bookCursorAdapter = new ComicBookCursorAdapter(getActivity(), getCursor());
			listView.setAdapter(bookCursorAdapter);
		} catch (Exception e) {
			SimpleAppLog.error("Could not list comic", e);
		}
		return v;
	}

	protected abstract Cursor getCursor() throws Exception;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		comicBookDBAdapter = new ComicBookDBAdapter(getActivity());
		try {
			comicBookDBAdapter.open();
		} catch (SQLException e) {
			SimpleAppLog.error("Could not open comic book database",e);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (comicBookDBAdapter != null) {
			comicBookDBAdapter.close();
		}
	}
}
