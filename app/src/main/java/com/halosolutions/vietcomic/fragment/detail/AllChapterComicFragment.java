package com.halosolutions.vietcomic.fragment.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;

import com.halosolutions.vietcomic.R;
import com.halosolutions.vietcomic.adapter.ComicBookCursorAdapter;
import com.halosolutions.vietcomic.adapter.ComicChapterCursorAdapter;
import com.halosolutions.vietcomic.util.SimpleAppLog;

/**
 * Created by cmg on 19/08/15.
 */
public class AllChapterComicFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_comic_list, container, false);
        final View view = v.findViewById(R.id.listComic);
        try {
            CursorAdapter bookCursorAdapter = new ComicChapterCursorAdapter(getActivity(), getCursor(), itemLayout);
            ((AbsListView) view).setAdapter(bookCursorAdapter);
            ((AbsListView) view).setOnItemClickListener(this);
        } catch (Exception e) {
            SimpleAppLog.error("Could not list comic", e);
        }
        return v;
    }


}
