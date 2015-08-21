package com.halosolutions.vietcomic.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
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
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by cmg on 20/08/15.
 */
public class ComicDownloaderService extends Service {

    public static class Action {

        public static final int DOWNLOAD = 1;

        public static final int STOP = 2;

        public static final int RECHECK = 3;
    }

    private static final int ONGOING_NOTIFICATION_ID = 17031989;

    private static final int CHECK_DOWNLOADING_TIME = 2 * 60 * 1000;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private Gson gson;
    private ComicBookDBAdapter bookDBAdapter;
    private ComicChapterDBAdapter chapterDBAdapter;
    private ComicChapterPageDBAdapter chapterPageDBAdapter;
    private ChapterDownloadManager downloadManager;
    private BroadcastHelper broadcastHelper;
    private boolean isForeGround;

    private Map<String, Integer> whatMessages = new WeakHashMap<String, Integer>();

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            SimpleAppLog.debug("Handle message download");
            Bundle bundle = msg.getData();
            ComicChapter comicChapter = null;
            try {
                comicChapter = gson.fromJson(bundle.getString(ComicChapter.class.getName()), ComicChapter.class);
                if (comicChapter == null) {
                    SimpleAppLog.error("No chapter found for download");
                    return;
                }
                comicChapter = chapterDBAdapter.getByChapterId(comicChapter.getChapterId());
                if (comicChapter.getStatus() != ComicChapter.STATUS_DOWNLOADING) {
                    comicChapter.setStatus(ComicChapter.STATUS_INIT_DOWNLOADING);
                    sendUpdateChapter(comicChapter);
                    SimpleAppLog.debug("Start download chapter: " + comicChapter.getName() + ". URL: " + comicChapter.getUrl());
                    fetchChapterPage(comicChapter);
                    downloadChapterPage(comicChapter);
                } else {
                    SimpleAppLog.debug("Chapter is download. Skip by default");
                }
            } catch (Exception e) {
                SimpleAppLog.error("Could not download chapter: "
                                + (comicChapter == null ? "null" : (comicChapter.getName() + " " + comicChapter.getUrl())),
                        e);
                if (comicChapter != null) {
                    comicChapter.setStatus(ComicChapter.STATUS_DOWNLOAD_FAILED);
                    sendUpdateChapter(comicChapter);
                }
            } finally {
                if (comicChapter != null) {
                    if (whatMessages.containsKey(comicChapter.getChapterId()))
                        whatMessages.remove(comicChapter.getChapterId());
                }
            }
            //stopSelf(msg.arg1);
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
        try {
            chapterDBAdapter.update(chapter);
            broadcastHelper.sendComicChaptersUpdate(chapter);
            SimpleAppLog.debug("Send chapter update. Status: " + chapter.getStatus()
                    + ". Name: " + chapter.getName()
                    + ". URL: " + chapter.getUrl());
        } catch (Exception e) {
            SimpleAppLog.error("Could not send chapter update. Status " + chapter.getStatus()
                    + ". Name: " + chapter.getName()
                    + ". URL: " + chapter.getUrl() , e);
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

    private void downloadChapterPage(final ComicChapter chapter) throws Exception {
        List<ComicChapterPage> pages = chapterPageDBAdapter.listByComicChapter(chapter.getChapterId());
        if (pages != null && pages.size() > 0) {
            for (ComicChapterPage page : pages) {
                downloadChapterPage(page, false);
            }
            chapter.setImageCount(pages.size());
            chapter.setStatus(ComicChapter.STATUS_DOWNLOADING);
            //sendUpdateChapter(chapter);
            verifyDownloadedPages(chapter);
        } else {
            SimpleAppLog.error("No chapter page found from database");
            chapter.setStatus(ComicChapter.STATUS_DOWNLOAD_FAILED);
            sendUpdateChapter(chapter);
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

    private void verifyDownloadedPages(ComicChapter chapter) {
        try {
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
                            bookDBAdapter.update(comicBook);
                            broadcastHelper.sendComicUpdate(comicBook);
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
                    }
                } else {
                    sendUpdateChapter(chapter);
                }
            } else {
                SimpleAppLog.error("No pending download pages found!");
                chapter.setStatus(ComicChapter.STATUS_DOWNLOAD_FAILED);
                sendUpdateChapter(chapter);
            }
        } catch (Exception e) {
            SimpleAppLog.error("Could not verify comic chapter: "+ chapter.getName() + ". URL: " + chapter.getUrl(), e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        broadcastHelper = new BroadcastHelper(getApplicationContext());
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

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
                SimpleAppLog.debug("Start download chaper page: " + page.getUrl());
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
                handlerCheckDownloading.removeCallbacks(runnableCheckDownloading);
                handlerCheckDownloading.post(runnableCheckDownloading);
            }

            @Override
            public void onError(ComicChapterPage page, Throwable e) {
                page.setStatus(ComicChapterPage.STATUS_DOWNLOAD_FAILED);
                updateChapterPage(page);
                ComicChapter chapter = chapterDBAdapter.getByChapterId(page.getChapterId());
                if (chapter != null) {
                    if (chapter.getStatus() == ComicChapter.STATUS_DOWNLOADING) {
                        SimpleAppLog.debug("Mask download chapter as failed. So user can resume it." +
                                ". Chapter URL: " + chapter.getUrl()
                                +". Page URL: " + page.getUrl());
                        chapter.setStatus(ComicChapter.STATUS_DOWNLOAD_FAILED);
                        sendUpdateChapter(chapter);
                    } else {
                        SimpleAppLog.debug("Status: " + chapter.getStatus() +". Skip by default. "
                                +". Chapter URL: " + chapter.getUrl()
                                +". Page URL: " + page.getUrl());
                    }
                } else {
                    SimpleAppLog.error("Could not found chapter with Page URL: " + page.getUrl());
                }
                handlerCheckDownloading.removeCallbacks(runnableCheckDownloading);
                handlerCheckDownloading.post(runnableCheckDownloading);
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SimpleAppLog.debug("Receiver new download request");
        if (intent == null) return START_STICKY;
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            int action = bundle.getInt(Action.class.getName());
            SimpleAppLog.info("DownloadService start command action:  " + action);
            final ComicChapter comicChapter = gson.fromJson(bundle.getString(ComicChapter.class.getName()), ComicChapter.class);
            if (comicChapter == null) return START_STICKY;
            switch (action) {
                case Action.DOWNLOAD:
                    try {
                        Message msg = mServiceHandler.obtainMessage();
                        msg.arg1 = startId;
                        msg.setData(bundle);
                        mServiceHandler.sendMessage(msg);
                        Cursor cursorChapters = chapterDBAdapter.listByStatus(new Integer[] {
                                ComicChapter.STATUS_INIT_DOWNLOADING,
                                ComicChapter.STATUS_DOWNLOADING,
                                ComicChapter.STATUS_DOWNLOAD_JOINING
                        });
                        int downloadCount = cursorChapters.getCount();
                        cursorChapters.close();
                        showForegroundNotification("Đang tải " + downloadCount + " tập truyện",
                                "Vui lòng chờ trong giây lát");

                    } catch (Exception e) {
                        SimpleAppLog.error("Could not start download",e);
                    }
                    break;
                case Action.STOP:
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            stopDownloadChapter(comicChapter);
                            return null;
                        }
                    }.execute();
                    break;
                case Action.RECHECK:
                    break;
            }

        }
        return START_STICKY;
    }

    private void stopDownloadChapter(ComicChapter chapter) {
        try {
            chapter.setStatus(ComicChapter.STATUS_READED);
            chapterDBAdapter.update(chapter);
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
            handlerCheckDownloading.removeCallbacks(runnableCheckDownloading);
            handlerCheckDownloading.post(runnableCheckDownloading);
            if (whatMessages.containsKey(chapter.getChapterId())) {
                mServiceHandler.removeMessages(whatMessages.get(chapter.getChapterId()));
                whatMessages.remove(chapter.getChapterId());
            }
        } catch (Exception e) {
            SimpleAppLog.error("Could not stop download chapter " + chapter.getUrl(),e);
        }
    }

    private Handler handlerCheckDownloading = new Handler();


    private Runnable runnableCheckDownloading = new Runnable() {
        @Override
        public void run() {
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
                if (downloadManager.getDownloadingCount() == 0 && isChapterPageDownloading) {
                    SimpleAppLog.error("Look like not all page is submit to download thread");
                    while (!cursorChapterPages.isAfterLast()) {
                        downloadChapterPage(chapterPageDBAdapter.toObject(cursorChapterPages), true);
                        cursorChapterPages.moveToNext();
                    }
                }
            } catch (Exception e) {
                SimpleAppLog.error("Could not check downloading",e);
            } finally {
                if (cursorChapters != null)
                    cursorChapters.close();
                if (cursorChapterPages != null)
                    cursorChapterPages.close();
            }
            if (stopService) {
                stopForeground(true);
                isForeGround = false;
            } else {
                showForegroundNotification("Đang tải " + downloadCount + " tập truyện",
                        "Vui lòng chờ trong giây lát");
                handlerCheckDownloading.postDelayed(runnableCheckDownloading, CHECK_DOWNLOADING_TIME);
            }
        }
    };

    private void showForegroundNotification(String title, String description) {
        SimpleAppLog.debug("Send foreground notification: " + title + ". Description: " + description);
        Notification notification = new Notification(R.drawable.app_icon, getText(R.string.app_name),
                System.currentTimeMillis());
        Intent notificationIntent = new Intent(ComicDownloaderService.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ComicDownloaderService.this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(ComicDownloaderService.this, title,
                description, pendingIntent);
        if (!isForeGround) {
            isForeGround = true;
            startForeground(ONGOING_NOTIFICATION_ID, notification);
        } else {
            final NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                    .getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
            notificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
        }
    }
}
