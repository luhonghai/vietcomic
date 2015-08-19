package com.halosolutions.vietcomic.comic;

import android.content.ContentValues;
import android.content.Context;

import com.halosolutions.vietcomic.sqlite.AbstractData;
import com.halosolutions.vietcomic.util.DateHelper;

/**
 * Created by cmg on 11/08/15.
 */
public class ComicChapterPage extends AbstractData<ComicChapterPage> {

    private String chapterId;

    private String bookId;

    private String url;

    private String filePath;

    private int index;

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
        return url;
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
}
