package com.halosolutions.vietcomic.fragment.detail;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cmg.android.cmgpdf.AsyncTask;
import com.cmg.android.cmgpdf.PDFActivity;
import com.google.gson.Gson;
import com.halosolutions.vietcomic.R;
import com.halosolutions.vietcomic.adapter.ComicChapterCursorAdapter;
import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.comic.ComicChapter;
import com.halosolutions.vietcomic.service.BroadcastHelper;
import com.halosolutions.vietcomic.service.ComicDownloaderService;
import com.halosolutions.vietcomic.sqlite.ext.ComicChapterDBAdapter;
import com.halosolutions.vietcomic.util.SimpleAppLog;
import com.rey.material.widget.ProgressView;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by cmg on 19/08/15.
 */
public class AllChapterComicFragment extends DetailComicFragment {

    private ComicChapterDBAdapter dbAdapter;

    private Gson gson;

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
        final ComicChapter comicChapter = (ComicChapter) view.getTag();
        SimpleAppLog.debug("Chapter status: " + comicChapter.getStatus());

        if (comicChapter.getStatus() == ComicChapter.STATUS_DOWNLOADED
                || comicChapter.getStatus() == ComicChapter.STATUS_WATCHED) {
            File pdfComic = new File(comicChapter.getFilePath());
            if (pdfComic.exists()) {
                comicChapter.setStatus(ComicChapter.STATUS_WATCHED);
                SimpleAppLog.debug("File is exists. Try to open");
                Uri uri = Uri.fromFile(pdfComic);
                final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                Bundle bundle = new Bundle();
                bundle.putString(ComicChapter.class.getName(), gson.toJson(comicChapter));
                intent.putExtras(bundle);
                intent.setClass(getActivity(), PDFActivity.class);
                startActivity(intent);
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            dbAdapter.update(comicChapter);
                            broadcastHelper.sendComicChaptersUpdate(comicChapter);
                            ComicBook comicBook = comicBookDBAdapter.getComicByBookId(comicChapter.getBookId());
                            if (comicBook != null) {
                                comicBook.setIsWatched(true);
                                comicBook.setTimestamp(new Date(System.currentTimeMillis()));
                                comicBookDBAdapter.update(comicBook);
                                broadcastHelper.sendComicUpdate(comicBook);
                            }
                        } catch (Exception e) {
                            SimpleAppLog.error("Could not update chapter status",e);
                        }
                        return null;
                    }
                }.execute();
            } else {
                SimpleAppLog.debug("File not is exists. Try to re-download");
                sendDownloadChapter(view, comicChapter);
            }
        } else if (comicChapter.getStatus() == ComicChapter.STATUS_DOWNLOAD_FAILED
                || comicChapter.getStatus() == ComicChapter.STATUS_NEW
                || comicChapter.getStatus() == ComicChapter.STATUS_SELECTED) {
            sendDownloadChapter(view, comicChapter);
        }
    }

    private void sendDownloadChapter(View view, ComicChapter comicChapter) {
        View root = getView();
        if (root != null) {
            if (comicChapter.getStatus() != ComicChapter.STATUS_DOWNLOADING
                    && comicChapter.getStatus() != ComicChapter.STATUS_INIT_DOWNLOADING) {
                comicChapter.setStatus(ComicChapter.STATUS_INIT_DOWNLOADING);
                try {
                    dbAdapter.update(comicChapter);
                    reloadListView();
                } catch (Exception e) {
                    SimpleAppLog.error("Could not update database",e);
                }
                Intent downloadIntent = new Intent(getActivity(), ComicDownloaderService.class);
                downloadIntent.putExtra(ComicChapter.class.getName(), gson.toJson(comicChapter));
                downloadIntent.putExtra(ComicDownloaderService.Action.class.getName(),
                        ComicDownloaderService.Action.DOWNLOAD);
                getActivity().startService(downloadIntent);
            }
        }

    }

    @Override
    protected void onUpdateComic(ComicBook comicBook, boolean reload) throws Exception {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broadcastHelper.registerOnComicChapterUpdated(new BroadcastHelper.OnComicChapterUpdated() {
            @Override
            public void onUpdated(ComicChapter chapter) {
                if (chapter.getBookId().equals(getComicBook().getBookId())) {
                    setNeedUpdate(true);
                }
            }
        });
        dbAdapter = new ComicChapterDBAdapter(getActivity());
        try {
            dbAdapter.open();
        } catch (SQLException e) {
            SimpleAppLog.error("Could not open database",e);
        }
        gson = new Gson();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbAdapter.close();
    }
}
