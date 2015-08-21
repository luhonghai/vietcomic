package com.halosolutions.vietcomic.comic;

import android.content.ContentValues;
import android.content.Context;

import com.halosolutions.vietcomic.sqlite.AbstractData;
import com.halosolutions.vietcomic.util.DateHelper;

/**
 * Created by cmg on 11/08/15.
 */
public class ComicChapterPage extends AbstractData<ComicChapterPage> {

    public static final int STATUS_DEFAULT = 0;

    public static final int STATUS_DOWNLOADING = 1;

    public static final int STATUS_DOWNLOADED = 2;

    public static final int STATUS_DOWNLOAD_FAILED = 3;

    private String chapterId;

    private String bookId;

    private String pageId;

    private String url;

    private String filePath;

    private int index;

    private int taskId = -1;

    private int status;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    @Override
    public String toPrettyString(Context context) {
        return bookId + "-" + chapterId + "-" + pageId;
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(KEY_BOOK_ID, getBookId());
        cv.put(KEY_STATUS, getStatus());
        cv.put(KEY_URL, getUrl());
        cv.put(KEY_CHAPTER_ID, getChapterId());
        cv.put(KEY_INDEX, getIndex());
        cv.put(KEY_FILE_PATH, getFilePath());
        cv.put(KEY_TASK_ID, getTaskId());
        cv.put(KEY_PAGE_ID, getPageId());
        if (getCreatedDate() != null)
            cv.put(KEY_CREATED_DATE, DateHelper.convertDateToString(getCreatedDate()));
        return cv;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }
}
