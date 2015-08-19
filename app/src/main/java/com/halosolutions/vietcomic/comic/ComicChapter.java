package com.halosolutions.vietcomic.comic;

import android.content.ContentValues;
import android.content.Context;

import com.halosolutions.vietcomic.sqlite.AbstractData;
import com.halosolutions.vietcomic.util.DateHelper;

import java.util.Date;

/**
 * Created by cmg on 11/08/15.
 */
public class ComicChapter extends AbstractData<ComicChapter> {

    private String chapterId;

    private String bookId;

    private String name;

    private String url;

    private String filePath;

    private Date publishDate;

    private int index;

    private int imageCount;

    private int status;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

    public String getChapterId() {
        if (chapterId == null) return chapterId;
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    @Override
    public String toPrettyString(Context context) {
        return name;
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(KEY_NAME, getName());
        cv.put(KEY_BOOK_ID, getBookId());
        cv.put(KEY_STATUS, getStatus());
        cv.put(KEY_URL, getUrl());
        cv.put(KEY_CHAPTER_ID, getChapterId());
        cv.put(KEY_INDEX, getIndex());
        cv.put(KEY_IMAGE_COUNT, getImageCount());
        cv.put(KEY_FILE_PATH, getFilePath());
        if (getPublishDate() != null)
            cv.put(KEY_PUBLISH_DATE, DateHelper.convertDateToString(getPublishDate()));
        if (getCreatedDate() != null)
            cv.put(KEY_CREATED_DATE, DateHelper.convertDateToString(getCreatedDate()));
        return cv;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof ComicChapter) {
            return this.getChapterId().equalsIgnoreCase(((ComicChapter) o).getChapterId());
        }
        return super.equals(o);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
