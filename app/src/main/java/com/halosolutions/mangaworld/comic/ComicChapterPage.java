package com.halosolutions.mangaworld.comic;

import com.halosolutions.mangaworld.sqlite.AbstractData;
import com.luhonghai.litedb.annotation.LiteColumn;
import com.luhonghai.litedb.annotation.LiteTable;

/**
 * Created by cmg on 11/08/15.
 */
@LiteTable(allowedParent = AbstractData.class)
public class ComicChapterPage extends AbstractData {

    public static final int STATUS_DEFAULT = 0;

    public static final int STATUS_DOWNLOADING = 1;

    public static final int STATUS_DOWNLOADED = 2;

    public static final int STATUS_DOWNLOAD_FAILED = 3;

    @LiteColumn
    private String chapterId;

    @LiteColumn
    private String bookId;

    @LiteColumn
    private String pageId;

    @LiteColumn
    private String url;

    @LiteColumn
    private String filePath;

    @LiteColumn
    private int index;

    @LiteColumn
    private int taskId = -1;

    @LiteColumn
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
