package com.halosolutions.vietcomic.fragment.detail;

import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.halosolutions.vietcomic.R;
import com.halosolutions.vietcomic.adapter.ComicChapterCursorAdapter;
import com.halosolutions.vietcomic.sqlite.ext.ComicChapterDBAdapter;
import com.halosolutions.vietcomic.util.SimpleAppLog;
import com.rey.material.widget.ProgressView;

import java.sql.SQLException;

/**
 * Created by cmg on 19/08/15.
 */
public class AllChapterComicFragment extends DetailComicFragment implements AdapterView.OnItemClickListener {

    private ComicChapterDBAdapter dbAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_comic_list, container, false);
        final View view = v.findViewById(R.id.listComic);
        try {
            CursorAdapter bookCursorAdapter = new ComicChapterCursorAdapter(getActivity(), getCursor());
            ((AbsListView) view).setAdapter(bookCursorAdapter);
            ((AbsListView) view).setOnItemClickListener(this);
            ColorDrawable sage = new ColorDrawable(this.getResources().getColor(R.color.colorPrimary));
            ((ListView) view).setDivider(sage);
            ((ListView) view).setDividerHeight(1);
            ((ProgressView)v.findViewById(R.id.progressEmpty)).start();
            ((ListView) view).setEmptyView(v.findViewById(R.id.progressEmpty));
        } catch (Exception e) {
            SimpleAppLog.error("Could not list comic", e);
        }
        return v;
    }

    @Override
    protected Cursor getCursor() throws Exception {
        return dbAdapter.listByComic(getComicBook());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbAdapter = new ComicChapterDBAdapter(getActivity());
        try {
            dbAdapter.open();
        } catch (SQLException e) {
            SimpleAppLog.error("Could not open database",e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbAdapter.close();
    }
}
