package com.halosolutions.vietcomic.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.cmg.android.cmgpdf.AsyncTask;
import com.google.gson.Gson;
import com.halosolutions.vietcomic.MainActivity;
import com.halosolutions.vietcomic.R;
import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.comic.ComicChapter;
import com.halosolutions.vietcomic.comic.ComicChapterPage;
import com.halosolutions.vietcomic.comic.ComicService;
import com.halosolutions.vietcomic.sqlite.ext.ComicBookDBAdapter;
import com.halosolutions.vietcomic.sqlite.ext.ComicChapterDBAdapter;
import com.halosolutions.vietcomic.sqlite.ext.ComicChapterPageDBAdapter;
import com.halosolutions.vietcomic.util.AndroidHelper;
import com.halosolutions.vietcomic.util.SimpleAppLog;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by cmg on 20/08/15.
 */
public class ComicDownloaderService extends Service {

    public static class Action {

        public static final int DOWNLOAD = 1;

        public static final int STOP = 2;

        public static final int CHECK = 3;
    }

    private static final int INIT_POOL_SIZE = 5;

    private static final int POOL_SIZE = 3;

    private static final int ONGOING_NOTIFICATION_ID = 17031989;

    private Gson gson;
    private ComicBookDBAdapter bookDBAdapter;
    private ComicChapterDBAdapter chapterDBAdapter;
    private ComicChapterPageDBAdapter chapterPageDBAdapter;
    private ChapterDownloadManager downloadManager;
    private BroadcastHelper broadcastHelper;
    private boolean isForeGround;

    private final Map<String, Future> downloadQueue = new HashMap<String, Future>();

    private final ExecutorService executorDownload = Executors.newFixedThreadPool(AndroidHelper.isLowerThanApiLevel11() ? 1 : POOL_SIZE);

    private final ExecutorService executorCheck = Executors.newFixedThreadPool(AndroidHelper.isLowerThanApiLevel11() ? 1 : POOL_SIZE);

    private int currentDownloading = 0;

    private static final Object lock = new Object();

    private void submitDownloadChapter(final String chapterId) {
        synchronized (downloadQueue) {
            if (!downloadQueue.containsKey(chapterId)) {
                downloadQueue.put(chapterId, executorDownload.submit(new Runnable() {
                    @Override
                    public void run() {
                        ComicChapter comicChapter = chapterDBAdapter.getByChapterId(chapterId);
                        try {
                            comicChapter.setStatus(ComicChapter.STATUS_INIT_DOWNLOADING);
                            //comicChapter.setCompletedCount(0);
                            sendUpdateChapter(comicChapter);
                            SimpleAppLog.debug("Start download chapter: " + comicChapter.getName() + ". URL: " + comicChapter.getUrl());
                            downloadChapterPage(comicChapter, true);
                        } catch (Exception e) {
                            SimpleAppLog.error("Could not download chapter: "
                                            + (comicChapter == null ? "null" : (comicChapter.getName() + " " + comicChapter.getUrl())),
                                    e);
                            if (comicChapter != null) {
                                comicChapter.setStatus(ComicChapter.STATUS_DOWNLOAD_FAILED);
                                sendUpdateChapter(comicChapter);
                            }
                        }
                    }
                }));
            }
        }
    }

    private void updateChapterPage(ComicChapterPage page) {
        try {
            chapterPageDBAdapter.update(page);
        } catch (Exception e) {
            SimpleAppLog.error("Could not update page " + page.getUrl(), e);
        }
    }

    private void sendUpdateChapter(ComicChapter chapter) {
        synchronized (lock) {
            try {
                chapterDBAdapter.update(chapter);
                broadcastHelper.sendComicChaptersUpdate(chapter);
                SimpleAppLog.debug("Send chapter update. Status: " + chapter.getStatus()
                        + ". Name: " + chapter.getName()
                        + ". URL: " + chapter.getUrl());
            } catch (Exception e) {
                SimpleAppLog.error("Could not send chapter update. Status " + chapter.getStatus()
                        + ". Name: " + chapter.getName()
                        + ". URL: " + chapter.getUrl(), e);
            }
        }
    }


    private void fetchChapterPage(final ComicChapter chapter) throws Exception {
        final ComicBook comicBook = bookDBAdapter.getComicByBookId(chapter.getBookId());
        if (comicBook != null) {
            ComicService comicService = ComicService.getService(getApplicationContext(), comicBook);
            if (comicService != null) {
                SimpleAppLog.debug("Try to fetch all chapter page");
                comicService.fetchChapterPage(chapter, new ComicService.FetchChapterPageListener() {
                    @Override
                    public void onChapterPageFound(ComicChapterPage page) {
                        try {
                            SimpleAppLog.debug("Found page: " + page.getUrl() + ". Try to insert to database");
                            chapterPageDBAdapter.insert(page);
                        } catch (Exception e) {
                            SimpleAppLog.error("Could not put comic page to database. " + page.getUrl(),e);
                        }
                    }
                });
            } else {
                SimpleAppLog.error("No comic service found .Could not download chapter: "
                        + chapter.getName() + " " + chapter.getUrl());
            }
        } else {
            SimpleAppLog.error("No book found .Could not download chapter: "
                    + chapter.getName() + " " + chapter.getUrl());
        }
    }

    private void downloadChapterPage(final ComicChapter chapter, boolean willFetch) throws Exception {
        List<ComicChapterPage> pages = chapterPageDBAdapter.listByComicChapter(chapter.getChapterId());
        if (pages != null && pages.size() > 0) {
            for (ComicChapterPage page : pages) {
                downloadChapterPage(page, true);
            }
            chapter.setImageCount(pages.size());
            chapter.setStatus(ComicChapter.STATUS_DOWNLOADING);
            sendUpdateChapter(chapter);
            verifyDownloadedPages(chapter);
        } else {
            if (willFetch) {
                fetchChapterPage(chapter);
                downloadChapterPage(chapter, false);
            } else {
//                SimpleAppLog.error("No chapter page found from database");
//                chapter.setStatus(ComicChapter.STATUS_DOWNLOAD_FAILED);
//                sendUpdateChapter(chapter);
                checkDownloading();
            }
        }
    }

    private void downloadChapterPage(final ComicChapterPage page, boolean forceDownload) throws Exception {
        String fileName = page.getBookId() + "-" + page.getChapterId() + "-" + page.getPageId();
        SimpleAppLog.debug("Current status: " + page.getStatus()
                + ". URL: " + page.getUrl());
        if (page.getStatus() == ComicChapterPage.STATUS_DEFAULT || forceDownload) {
            SimpleAppLog.debug("Add to download queue: " + page.getUrl());
            page.setFilePath(new File(AndroidHelper.getFolder(
                    getApplicationContext(),
                    AndroidHelper.DOWNLOAD_TEMP_CACHE_DIR),
                    fileName).getAbsolutePath());
            page.setStatus(ComicChapterPage.STATUS_DOWNLOADING);
            downloadManager.startDownload(page);
            SimpleAppLog.debug("Update status to database: " + page.getUrl());
            chapterPageDBAdapter.update(page);
        }
        SimpleAppLog.debug("Next status: " + page.getStatus()
                + ". URL: " + page.getUrl());
    }

    private void verifyDownloadedPages(final ComicChapter chap) {
        executorCheck.submit(new Runnable() {
            @Override
            public void run() {
                final ComicChapter chapter = chapterDBAdapter.getByChapterId(chap.getChapterId());
                try {
                    boolean willCheck = false;
                    List<ComicChapterPage> pages = chapterPageDBAdapter.listByComicChapter(chapter.getChapterId());
                    if (pages != null && pages.size() > 0) {
                        int completedCount = 0;
                        int failedCount = 0;
                        for (ComicChapterPage page : pages) {
                            if (page.getStatus() == ComicChapterPage.STATUS_DOWNLOADED) {
                                completedCount++;
                            } else if (page.getStatus() == ComicChapterPage.STATUS_DOWNLOAD_FAILED) {
                                failedCount++;
                            }
                        }
                        SimpleAppLog.debug("Completed task " + completedCount + "/" + pages.size());
                        chapter.setCompletedCount(completedCount);
                        if ( (completedCount == pages.size() || ((completedCount + failedCount) == pages.size() && failedCount <= 2))
                                && chapter.getStatus() != ComicChapter.STATUS_DOWNLOAD_JOINING) {
                            chapter.setStatus(ComicChapter.STATUS_DOWNLOAD_JOINING);
                            sendUpdateChapter(chapter);
                            File pdf = new File(AndroidHelper.getFolder(getApplicationContext(), AndroidHelper.DOWNLOADED_BOOK_DIR),
                                    chapter.getBookId() + "-" + chapter.getChapterId() + ".pdf");
                            chapter.setFilePath(pdf.getAbsolutePath());
                            SimpleAppLog.debug("Try to join pdf to " + pdf);

                            if (ComicService.joinComicBook(chapter, pages)) {
                                chapter.setStatus(ComicChapter.STATUS_DOWNLOADED);
                                sendUpdateChapter(chapter);
                                ComicBook comicBook = bookDBAdapter.getComicByBookId(chapter.getBookId());
                                if (comicBook != null) {
                                    comicBook.setIsDownloaded(true);
                                    comicBook.setTimestamp(new Date(System.currentTimeMillis()));
                                    bookDBAdapter.update(comicBook);
                                    broadcastHelper.sendComicUpdate(comicBook);
                                    willCheck = true;
                                }

                                for (ComicChapterPage page : pages) {
                                    File f = new File(page.getFilePath());
                                    if (f.exists()) {
                                        try {
                                            FileUtils.forceDelete(f);
                                        } catch (Exception e) {
                                        }
                                    }
                                    chapterPageDBAdapter.delete(page);
                                }
                            } else {
                                SimpleAppLog.error("Could not join comic chapter: " + chapter.getName() + ". URL: " + chapter.getUrl());
                                chapter.setFilePath(null);
                                chapter.setStatus(ComicChapter.STATUS_DOWNLOAD_FAILED);
                                sendUpdateChapter(chapter);
                                willCheck = true;
                            }
                        } else if (failedCount >= 3) {
                            stopDownloadChapter(chapter);
                            chapter.setStatus(ComicChapter.STATUS_DOWNLOAD_FAILED);
                            sendUpdateChapter(chapter);
                        } else {
                            sendUpdateChapter(chapter);
                        }
                    } else {
                        SimpleAppLog.error("No pending download pages found!");
                        chapter.setStatus(ComicChapter.STATUS_DOWNLOAD_FAILED);
                        sendUpdateChapter(chapter);
                        willCheck = true;
                    }
                    if (willCheck) {
                        synchronized (downloadQueue) {
                            if (downloadQueue.containsKey(chapter.getChapterId()))
                                downloadQueue.remove(chapter.getChapterId());
                        }
//                handlerCheckDownloading.removeCallbacks(runnableCheckDownloading);
//                handlerCheckDownloading.post(runnableCheckDownloading);
                        checkDownloading();
                    }
                } catch (Exception e) {
                    SimpleAppLog.error("Could not verify comic chapter: "+ chapter.getName() + ". URL: " + chapter.getUrl(), e);
                }
            }
        });

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        broadcastHelper = new BroadcastHelper(getApplicationContext());

        gson = new Gson();

        bookDBAdapter = new ComicBookDBAdapter(getApplicationContext());
        try {
            bookDBAdapter.open();
        } catch (SQLException e) {
            SimpleAppLog.error("Could not open comic book database",e);
        }
        chapterDBAdapter = new ComicChapterDBAdapter(getApplicationContext());
        try {
            chapterDBAdapter.open();
        } catch (SQLException e) {
            SimpleAppLog.error("Could not open comic chapter database",e);
        }
        chapterPageDBAdapter = new ComicChapterPageDBAdapter(getApplicationContext());
        try {
            chapterPageDBAdapter.open();
        } catch (SQLException e) {
            SimpleAppLog.error("Could not open comic chapter page database",e);
        }
        downloadManager = new ChapterDownloadManager(new ChapterDownloadManager.DownloadListener() {
            @Override
            public void onDownloadStart(ComicChapterPage page) {
                SimpleAppLog.debug("Start download chapter page: " + page.getUrl());
            }

            @Override
            public void onDownloadCompleted(ComicChapterPage page) {
                page.setStatus(ComicChapterPage.STATUS_DOWNLOADED);
                updateChapterPage(page);
                ComicChapter chapter = chapterDBAdapter.getByChapterId(page.getChapterId());
                if (chapter != null) {
                    verifyDownloadedPages(chapter);
                } else {
                    SimpleAppLog.error("Could not found chapter with page URL: " + page.getUrl());
                }
            }

            @Override
            public void onError(ComicChapterPage page, Throwable e) {
                SimpleAppLog.error("Could not download comic chapter page: " + page.getUrl(),e);
                page.setStatus(ComicChapterPage.STATUS_DOWNLOAD_FAILED);
                updateChapterPage(page);
                ComicChapter chapter = chapterDBAdapter.getByChapterId(page.getChapterId());
                if (chapter != null) {
                    SimpleAppLog.debug("Mask download chapter as failed. So user can resume it." +
                            ". Chapter URL: " + chapter.getUrl()
                            + ". Page URL: " + page.getUrl());
                    chapter.setStatus(ComicChapter.STATUS_DOWNLOAD_FAILED);
                    sendUpdateChapter(chapter);
                    stopDownloadChapter(chapter);
                    //verifyDownloadedPages(chapter);
                    checkDownloading();
                } else {
                    SimpleAppLog.error("Could not found chapter with Page URL: " + page.getUrl());
                }

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handlerCheckDownloading.removeCallbacks(runnableCheckDownloading);
        SimpleAppLog.info("Download service destroy");
        bookDBAdapter.close();
        chapterDBAdapter.close();
        chapterPageDBAdapter.close();
        broadcastHelper.unregister();
        downloadManager.destroy();
        if (downloadQueue.size() > 0) {
            for (Future future : downloadQueue.values()) {
                try {
                    future.cancel(true);
                } catch (Exception e) {}
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SimpleAppLog.debug("Receiver new download request");
        if (intent == null) return START_STICKY;
        final Bundle bundle = intent.getExtras();
        if (bundle != null) {
            int action = bundle.getInt(Action.class.getName());
            SimpleAppLog.info("DownloadService start command action:  " + action);
            switch (action) {
                case Action.CHECK:
                    checkDownloading(false);
                    break;
                case Action.DOWNLOAD:
                    checkDownloading(true);
                    break;
                case Action.STOP:
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            final ComicChapter comicChapter = gson.fromJson(bundle.getString(ComicChapter.class.getName()), ComicChapter.class);
                            comicChapter.setStatus(ComicChapter.STATUS_SELECTED);
                            stopDownloadChapter(comicChapter);
                            sendUpdateChapter(comicChapter);
                            return null;
                        }
                    }.execute();
                    break;

            }

        }
        return START_STICKY;
    }

    private void stopDownloadChapter(ComicChapter chapter) {
        try {
            //chapter.setStatus(ComicChapter.STATUS_WATCHED);
            //chapterDBAdapter.update(chapter);
            List<ComicChapterPage> pages = chapterPageDBAdapter.listByComicChapter(chapter.getChapterId());
            if (pages != null && pages.size() > 0) {
                for (ComicChapterPage page : pages) {
                    downloadManager.cancel(page.getPageId());
                    if (page.getStatus() != ComicChapterPage.STATUS_DOWNLOADED) {
                        page.setStatus(ComicChapterPage.STATUS_DEFAULT);
                        chapterPageDBAdapter.update(page);
                    }
                }
            }
            synchronized (downloadQueue) {
                if (downloadQueue.containsKey(chapter.getChapterId())) {
                    try {
                        downloadQueue.get(chapter.getChapterId()).cancel(true);
                    } catch (Exception e) {

                    }
                    downloadQueue.remove(chapter.getChapterId());
                }
            }
//            handlerCheckDownloading.removeCallbacks(runnableCheckDownloading);
//            handlerCheckDownloading.post(runnableCheckDownloading);
        } catch (Exception e) {
            SimpleAppLog.error("Could not stop download chapter " + chapter.getUrl(),e);
        }
        checkDownloading();
    }

    private Handler handlerCheckDownloading = new Handler();

    private Runnable runnableCheckDownloading = new Runnable() {
        @Override
        public void run() {
            checkDownloading();
        }
    };

    private void checkDownloading() {
        checkDownloading(false);
    }

    private void checkDownloading(boolean onlyInit) {
        boolean stopService = false;
        boolean isChapterDownloading;
        boolean isChapterPageDownloading;
        int downloadCount = 0;
        Cursor cursorChapters = null;
        Cursor cursorChapterPages = null;

        try {
            cursorChapters = chapterDBAdapter.listByStatus(new Integer[] {
                    ComicChapter.STATUS_INIT_DOWNLOADING,
                    ComicChapter.STATUS_DOWNLOADING,
                    ComicChapter.STATUS_DOWNLOAD_JOINING
            });
            cursorChapters.moveToFirst();
            downloadCount = cursorChapters.getCount();
            isChapterDownloading = downloadCount > 0;
            cursorChapterPages = chapterPageDBAdapter.listByStatus(new Integer[] {
                    ComicChapterPage.STATUS_DOWNLOADING
            });
            cursorChapterPages.moveToFirst();
            isChapterPageDownloading = cursorChapterPages.getCount() > 0;
            stopService = !(isChapterDownloading || isChapterPageDownloading);
            checkDownloadingByStatus(ComicChapter.STATUS_INIT_DOWNLOADING);
            if (!onlyInit) {
                checkDownloadingByStatus(ComicChapter.STATUS_DOWNLOADING);
                checkDownloadingByStatus(ComicChapter.STATUS_DOWNLOAD_JOINING);
            }
        } catch (Exception e) {
            SimpleAppLog.error("Could not check downloading",e);
        } finally {
            if (cursorChapters != null)
                cursorChapters.close();
            if (cursorChapterPages != null)
                cursorChapterPages.close();
        }
        if (stopService || (onlyInit && downloadCount == 0)) {
            stopForeground(true);
            isForeGround = false;
        } else {
            showForegroundNotification("Đang tải " + downloadCount + " tập truyện",
                    "Vui lòng chờ trong giây lát", downloadCount);
            //handlerCheckDownloading.postDelayed(runnableCheckDownloading, CHECK_DOWNLOADING_TIME);
        }
    }

    private void checkDownloadingByStatus(int status) {
        synchronized (downloadQueue) {
            Cursor cursorDownloadInit = null;
            try {
                int requestSize = INIT_POOL_SIZE - downloadQueue.size();
                if (requestSize > 0) {
                    cursorDownloadInit = chapterDBAdapter
                            .listByStatus(new Integer[]{
                                            status
                                    },
                                    downloadQueue.keySet(),
                                    Integer.toString(requestSize));
                    if (cursorDownloadInit.moveToFirst()) {
                        while (!cursorDownloadInit.isAfterLast()) {
                            ComicChapter chapter = chapterDBAdapter.toObject(cursorDownloadInit);
                            submitDownloadChapter(chapter.getChapterId());
                            cursorDownloadInit.moveToNext();
                        }
                    }
                }
            } catch (Exception e) {
                throw e;
            } finally {
                if (cursorDownloadInit != null)
                    cursorDownloadInit.close();
            }

        }
    }

    private void showForegroundNotification(String title, String description, int downloadingNumber) {
        SimpleAppLog.debug("Send foreground notification: " + title + ". Description: " + description);
        Notification notification = new Notification(R.drawable.launch_icon, getText(R.string.app_name),
                System.currentTimeMillis());
        Intent notificationIntent = new Intent(ComicDownloaderService.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ComicDownloaderService.this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(ComicDownloaderService.this, title,
                description, pendingIntent);
        if (!isForeGround) {
            currentDownloading = downloadingNumber;
            isForeGround = true;
            startForeground(ONGOING_NOTIFICATION_ID, notification);
        } else if (downloadingNumber != currentDownloading) {
            currentDownloading = downloadingNumber;
            final NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                    .getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
            notificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
        }
    }
}
