package com.halosolutions.mangaworld.comic;

import com.halosolutions.mangaworld.sqlite.AbstractData;
import com.halosolutions.mangaworld.util.StringHelper;
import com.luhonghai.litedb.annotation.LiteColumn;
import com.luhonghai.litedb.annotation.LiteTable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by cmg on 12/08/15.
 */
@LiteTable(allowedParent = AbstractData.class)
public class ComicBook extends AbstractData {

    @LiteColumn
    private String bookId;

    @LiteColumn
    private String name;

    @LiteColumn
    private String otherName;

    @LiteColumn
    private String status;

    @LiteColumn
    private String source;

    @LiteColumn
    private String service;

    @LiteColumn
    private String url;

    @LiteColumn
    private String thumbnail;

    @LiteColumn
    private String author;

    @LiteColumn
    private float rate;

    @LiteColumn
    private String description;

    @LiteColumn
    private boolean isDeleted;

    @LiteColumn
    private boolean isNew;

    @LiteColumn
    private boolean isHot;

    @LiteColumn
    private boolean isFavorite;

    @LiteColumn
    private boolean isDownloaded;

    @LiteColumn
    private boolean isWatched;

    private List<String> categories;

    @LiteColumn
    private String strCategories;

    @LiteColumn
    private Date timestamp;

    @LiteColumn
    private String search;

    public ComicBook() {

    }

    public ComicBook(String source) {
        this.source = source;
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getService() {
        if (service == null || service.length() == 0) return getSource();
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getSearch() {
        if (search == null || search.length() == 0) {
            search = StringHelper.removeAccent(getName()).toLowerCase();
        }
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}