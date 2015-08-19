package com.halosolutions.vietcomic.comic;

import android.content.ContentValues;
import android.content.Context;

import com.halosolutions.vietcomic.sqlite.AbstractData;
import com.halosolutions.vietcomic.util.DateHelper;
import com.halosolutions.vietcomic.util.StringHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cmg on 12/08/15.
 */
public class ComicBook extends AbstractData<ComicBook> {

    private String bookId;

    private String name;

    private String otherName;

    private String status;

    private String source;

    private String url;

    private String thumbnail;

    private String author;

    private float rate;

    private String description;

    private boolean isDeleted;

    private boolean isNew;

    private boolean isHot;

    private boolean isFavorite;

    private boolean isDownloaded;

    private boolean isWatched;

    private List<String> categories;

    private String strCategories;

    @Override
    public String toPrettyString(Context context) {
        return name;
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(KEY_SEARCH, StringHelper.removeAccent(getName()).toLowerCase());
        cv.put(KEY_NAME, getName());
        cv.put(KEY_AUTHOR, getAuthor());
        cv.put(KEY_BOOK_ID, getBookId());
        cv.put(KEY_DESCRIPTION, getDescription());
        cv.put(KEY_OTHER_NAME, getOtherName());
        cv.put(KEY_RATE, getRate());
        cv.put(KEY_STATUS, getStatus());
        cv.put(KEY_THUMBNAIL, getThumbnail());
        cv.put(KEY_URL, getUrl());
        cv.put(KEY_SOURCE, getSource());
        cv.put(KEY_DELETED, isDeleted() ? 1 : 0);
        cv.put(KEY_NEW, isNew() ?  1 : 0);
        cv.put(KEY_HOT, isHot() ? 1 : 0);
        cv.put(KEY_FAVORITE, isFavorite() ? 1 : 0);
        cv.put(KEY_DOWNLOADED, isDownloaded() ? 1 : 0);
        cv.put(KEY_WATCHED, isWatched() ? 1 : 0);
        if (getCreatedDate() != null)
            cv.put(KEY_CREATED_DATE, DateHelper.convertDateToString(getCreatedDate()));
        cv.put(KEY_CATEGORIES, getStrCategories());
        return cv;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getCategories() {
        if (categories == null && strCategories != null && strCategories.length() > 0) {
            categories = new ArrayList<>();
            String[] raw = strCategories.split("\\|");
            if (raw.length > 0) {
                for (String cat : raw) {
                    if (cat.trim().length() > 0) {
                        categories.add(cat);
                    }
                }
            }
        }
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getOtherName() {
        if (otherName == null) return "";
        return otherName;
    }

    public void setOtherName(String otherName) {
        this.otherName = otherName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof ComicBook) {
            return this.getId() == ((ComicBook) obj).getId();
        }
        return super.equals(obj);
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isHot() {
        return isHot;
    }

    public void setIsHot(boolean isHot) {
        this.isHot = isHot;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public String getStrCategories() {
        if (strCategories == null || strCategories.length() == 0 ) {
            String strCat = "|";
            if (categories != null && categories.size() > 0) {
                for (String cat : categories) {
                    strCat += (cat + "|");
                }
            }
            strCategories = strCat;
        }
        return strCategories;
    }

    public void setStrCategories(String strCategories) {
        this.strCategories = strCategories;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setIsDownloaded(boolean isDownloaded) {
        this.isDownloaded = isDownloaded;
    }

    public boolean isWatched() {
        return isWatched;
    }

    public void setIsWatched(boolean isWatched) {
        this.isWatched = isWatched;
    }
}