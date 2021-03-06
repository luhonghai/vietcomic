package com.halosolutions.mangaworld.service;

import com.halosolutions.mangaworld.comic.ComicChapterPage;
import com.halosolutions.mangaworld.util.AndroidHelper;
import com.halosolutions.mangaworld.util.Hash;
import com.halosolutions.mangaworld.util.SimpleAppLog;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    private static final ExecutorService tpExecutor
            = Executors.newFixedThreadPool(AndroidHelper.isLowerThanApiLevel11() ? 2 : MAX_POOL_SIZE);

    private final DownloadListener listener;

    private static final Map<String, Future> downloadingChapters = new ConcurrentHashMap<String, Future>();

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
                                File dest = new File(page.getFilePath());
                                try {
                                    if (!dest.exists()) {
                                        tmp = new File(FileUtils.getTempDirectory(), Hash.md5(page.getFilePath()) + ".tmp");
                                        if (listener != null)
                                            listener.onDownloadStart(page);
                                        FileUtils.copyURLToFile(new URL(page.getUrl())
                                                , tmp, CONNECTION_TIMEOUT, READ_TIMEOUT);
                                        if (tmp.exists()) {
                                            if (dest.exists()) {
                                                try {
                                                    FileUtils.forceDelete(dest);
                                                } catch (Exception e) {

                                                }
                                            }
                                            FileUtils.moveFile(tmp, dest);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
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
                                if (dest.exists()) {
                                    if (listener != null)
                                        listener.onDownloadCompleted(page);
                                }
                            }
                        }));
            } else {
                SimpleAppLog.error("This chapter page is downloading. " + page.getUrl());
            }
        }
    }

    public void cancel(String pageId) {
        synchronized (downloadingChapters) {
            if (downloadingChapters.containsKey(pageId)) {
                final Future future = downloadingChapters.get(pageId);
                if (!future.isCancelled()) {
                    try {
                        future.cancel(true);
                    } catch (Exception e) {

                    }
                }
                downloadingChapters.remove(pageId);
            }
        }
    }

    public int getDownloadingCount() {
        return downloadingChapters.size();
    }

    public void destroy() {
        synchronized (downloadingChapters) {
            try {
                if (downloadingChapters.size() > 0) {
                    for (Future future : downloadingChapters.values()) {
                        future.cancel(true);
                    }
                }
                tpExecutor.shutdownNow();
            } catch (Exception e) {
            }
        }
    }
}
