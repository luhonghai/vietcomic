package com.halosolutions.vietcomic.service;

import com.halosolutions.vietcomic.comic.ComicChapterPage;
import com.halosolutions.vietcomic.util.Hash;
import com.halosolutions.vietcomic.util.SimpleAppLog;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * Created by luhonghai on 8/20/15.
 */
public class ChapterDownloadManager {

    public interface DownloadListener {

        void onDownloadStart(ComicChapterPage page);

        void onDownloadCompleted(ComicChapterPage page);

        void onError(ComicChapterPage page, Throwable e);
    }

    private static final int READ_TIMEOUT = 30 * 1000;

    private static final int CONNECTION_TIMEOUT = 10 * 1000;

    private static final int MAX_POOL_SIZE = 5;

    private ExecutorService tpExecutor = Executors.newFixedThreadPool(MAX_POOL_SIZE);

    private final DownloadListener listener;

    private final Map<String, Future> downloadingChapters = new WeakHashMap<String, Future>();

    public ChapterDownloadManager(DownloadListener listener) {
        this.listener = listener;
    }

    public void startDownload(final ComicChapterPage page) {
        synchronized (downloadingChapters) {
            if (!downloadingChapters.containsKey(page.getPageId())) {
                downloadingChapters.put(page.getPageId(),
                        tpExecutor.submit(new Runnable() {
                            @Override
                            public void run() {
                                File tmp = null;
                                try {
                                    tmp = new File(FileUtils.getTempDirectory(), Hash.md5(page.getFilePath()) + ".tmp");
                                    if (listener != null)
                                        listener.onDownloadStart(page);
                                    FileUtils.copyURLToFile(new URL(page.getUrl())
                                            , tmp, CONNECTION_TIMEOUT, READ_TIMEOUT);
                                    if (tmp.exists()) {
                                        File dest = new File(page.getFilePath());
                                        if (dest.exists()) {
                                            try {
                                                FileUtils.forceDelete(dest);
                                            } catch (Exception e) {

                                            }
                                        }
                                        FileUtils.moveFile(tmp, dest);
                                        if (listener != null)
                                            listener.onDownloadCompleted(page);
                                    }
                                } catch (Exception e) {
                                    if (listener != null)
                                        listener.onError(page, e);
                                } finally {
                                    if (tmp != null && tmp.exists()) {
                                        try {
                                            FileUtils.forceDelete(tmp);
                                        } catch (Exception e) {

                                        }
                                    }
                                    synchronized (downloadingChapters) {
                                        downloadingChapters.remove(page.getPageId());
                                    }
                                }
                            }
                        }));

            } else {
                SimpleAppLog.error("This chapter page is downloading " + page.getUrl());
            }
        }
    }

    public void cancel(String pageId) {
        if (downloadingChapters.containsKey(pageId)) {
            final Future future = downloadingChapters.get(pageId);
            if (future.isCancelled()) {
                try {
                    future.cancel(true);
                } catch (Exception e) {

                }
            }
            downloadingChapters.remove(pageId);
        }
    }

    public int getDownloadingCount() {
        return downloadingChapters.size();
    }

    public void destroy() {
        try {
            while (!tpExecutor.isTerminated()) {
                tpExecutor.shutdownNow();
            }
        } catch (Exception e) {}
    }
}
