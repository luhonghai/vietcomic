package com.halosolutions.mangaworld.fragment.detail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.Gson;
import com.halosolutions.mangaworld.BuildConfig;
import com.halosolutions.mangaworld.R;
import com.halosolutions.mangaworld.adapter.ComicChapterCursorAdapter;
import com.halosolutions.mangaworld.comic.ComicBook;
import com.halosolutions.mangaworld.comic.ComicChapter;
import com.halosolutions.mangaworld.service.BroadcastHelper;
import com.halosolutions.mangaworld.service.ComicDownloaderService;
import com.halosolutions.mangaworld.sqlite.ext.ComicChapterDBAdapter;
import com.halosolutions.mangaworld.util.SimpleAppLog;
import com.rey.material.widget.ProgressView;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by cmg on 19/08/15.
 */
public class AllChapterComicFragment extends DetailComicFragment {

    private static class ReadCount {
        public static int MAX_COUNT = 7;
    }

    private ComicChapterDBAdapter dbAdapter;

    private Gson gson;

    private InterstitialAd mInterstitialAd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_comic_list, container, false);
        final View view = v.findViewById(R.id.listComic);
        try {
            CursorAdapter bookCursorAdapter = new ComicChapterCursorAdapter(getActivity(), getCursor());
            setSafeAdapter(view, bookCursorAdapter);
            ((AbsListView) view).setOnItemClickListener(this);
            ColorDrawable sage = new ColorDrawable(this.getResources().getColor(R.color.colorPrimary));
            ((ListView) view).setDivider(sage);
            ((ListView) view).setDividerHeight(1);
            ((ProgressView)v.findViewById(R.id.progressEmpty)).start();
            ((ListView) view).setEmptyView(v.findViewById(R.id.progressEmpty));
        } catch (Exception e) {
            SimpleAppLog.error("Could not list comic", e);
        }

        if (BuildConfig.IS_FREE) {
            mInterstitialAd = new InterstitialAd(getActivity());
            mInterstitialAd.setAdUnitId(getString(R.string.ad_unit_popup));

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    requestNewInterstitial();
                }
            });
            requestNewInterstitial();
        }

        return v;
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("7898660F3293A11BB56ED538658F9B0F")
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    protected Cursor getCursor() throws Exception {
        return dbAdapter.listByComic(getComicBook());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final ComicChapter comicChapter = (ComicChapter) view.getTag();
        SimpleAppLog.debug("Chapter status: " + comicChapter.getStatus());

        if ((comicChapter.getStatus() == ComicChapter.STATUS_DOWNLOADED
                || comicChapter.getStatus() == ComicChapter.STATUS_WATCHED)) {
            File pdfComic;
            if (comicChapter.getFilePath().length() > 0
                    && (pdfComic = new File(comicChapter.getFilePath())).exists()) {
                readComicChapter(comicChapter, pdfComic);
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

    private void readComicChapter(final ComicChapter comicChapter, final File pdfComic) {
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        int currentCount = preferences.getInt(ReadCount.class.getName(), 0);
        boolean readBook = true;
        if (currentCount >= ReadCount.MAX_COUNT && BuildConfig.IS_FREE && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            readBook = false;
            currentCount = 0;
        }
        preferences.edit().putInt(ReadCount.class.getName(), ++currentCount).apply();
        if (readBook) {
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
                        SimpleAppLog.error("Could not update chapter status", e);
                    }
                    return null;
                }
            }.execute();
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
        try {
            dbAdapter = new ComicChapterDBAdapter(getActivity());

            dbAdapter.open();
        } catch (Exception e) {
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
