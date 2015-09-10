package com.halosolutions.mangaworld.comic;

import com.halosolutions.mangaworld.sqlite.AbstractData;
import com.luhonghai.litedb.annotation.LiteColumn;
import com.luhonghai.litedb.annotation.LiteTable;

import java.util.Date;

/**
 * Created by cmg on 11/08/15.
 */
@LiteTable(allowedParent = AbstractData.class)
public class ComicChapter extends AbstractData {

    public static final int STATUS_NEW = 0;

    public static final int STATUS_SELECTED = 1;

    public static final int STATUS_INIT_DOWNLOADING = 2;

    public static final int STATUS_DOWNLOADING = 3;

    public static final int STATUS_DOWNLOAD_FAILED = 4;

    public static final int STATUS_DOWNLOAD_JOINING = 5;

    public static final int STATUS_DOWNLOADED = 6;

    public static final int STATUS_WATCHED = 7;

    @LiteColumn
    private String chapterId;

    @LiteColumn
    private String bookId;

    @LiteColumn
    private String name;

    @LiteColumn
    private String url;

    @LiteColumn
    private String filePath;

    @LiteColumn
    private Date publishDate;

    @LiteColumn
    private Date timestamp;

    @LiteColumn
    private int index;

    @LiteColumn
    private int imageCount;

    @LiteColumn
    private int completedCount;

    @LiteColumn
    private int status;

    public ComicChapter() {

    }

    public ComicChapter(String bookId) {
        this.bookId = bookId;
    }

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
        if (chapterId == null) return "";
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
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
        if (filePath == null) return "";
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

    public int getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
