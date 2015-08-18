package com.halosolutions.vietcomic.comic;

import android.content.ContentValues;
import android.content.Context;

import com.halosolutions.vietcomic.sqlite.AbstractData;

/**
 * Created by cmg on 11/08/15.
 */
public class ComicChapter extends AbstractData<ComicChapter> {

    private String chapterId;

    private String bookId;

    private String name;

    private String url;

    private String pdfFile;

    private int index;

    private int imageCount;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(String pdfFile) {
        this.pdfFile = pdfFile;
    }

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

    public String getChapterId() {
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
        return null;
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
}
