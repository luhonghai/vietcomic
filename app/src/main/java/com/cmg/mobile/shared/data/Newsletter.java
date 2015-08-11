/**
 * Copyright (c) CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.mobile.shared.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class Newsletter implements Serializable, Mirrorable {

    private static final long serialVersionUID = 2720116139064517583L;

    public static final String NEWSLETTER_ID = "NEWSLETTER_ID";
    public static final int IS_DOWNLOAD = 1;
    public static final int NOT_DOWNLOAD = 0;
    public static final int IS_NEW = 1;
    public static final int NOT_NEW = 0;
    public static final int IS_FAVOR = 1;
    public static final int NOT_FAVOR = 0;
    public static final String TYPE_PENSIONER = "pensioner";
    public static final String TYPE_EMPLOYEE = "employee";

    private String id;
    private String title;
    private String date;
    private String summary;
    private String imageUrl;
    private String fileUrl;
    private long size;
    private int isDownloaded = NOT_DOWNLOAD;
    private int page;
    private int categoryId;
    private String type;
    private int isNew = IS_NEW;

    private List<Integer> bookmarkPages;


    private int isFavor = NOT_FAVOR;


    /**
     * Constructor
     */
    public Newsletter() {

    }

    /**
     * Constructor
     *
     * @param id
     * @param title
     * @param date
     * @param summary
     * @param imageUrl
     * @param fileUrl
     * @param size
     * @param isDownloaded
     * @param page
     */
    public Newsletter(String id, String title, String date, String summary,
                      String imageUrl, String fileUrl, long size, int isDownloaded,
                      int catId, int page, int isNew, int isFavor) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.summary = summary;
        this.imageUrl = imageUrl;
        this.fileUrl = fileUrl;
        this.size = size;
        this.isDownloaded = isDownloaded;
        this.categoryId = catId;
        this.page = page;
        this.isNew = isNew;
        this.isFavor = isFavor;
    }

    /**
     * get ID
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * set ID
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * get Donwload status
     *
     * @return
     */
    public int getDownloaded() {
        return isDownloaded;
    }

    /**
     * check download status
     *
     * @return
     */
    public boolean checkDownloaded() {
        return isDownloaded == IS_DOWNLOAD;
    }

    /**
     * set download status
     *
     * @param isDownloaded
     */
    public void setDownloaded(int isDownloaded) {
        this.isDownloaded = isDownloaded;
    }

    /**
     * get new item
     *
     * @return
     */
    public int getIsNew() {
        return isNew;
    }

    /**
     * set new item
     *
     * @param isNew
     */
    public void setNew(int isNew) {
        this.isNew = isNew;
    }

    public boolean checkNew() {
        return isNew == IS_NEW;
    }

    /**
     * get number of pages
     *
     * @return
     */
    public int getPage() {
        return page;
    }

    /**
     * set number of pages
     *
     * @param page
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * get Category ID
     *
     * @return
     */
    public int getCategoryId() {
        return categoryId;
    }

    /**
     * set Category ID
     *
     * @param categoryId
     */
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * get Title
     *
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * set Title
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * get Date
     *
     * @return
     */
    public String getDate() {
        return date;
    }

    /**
     * set Date
     *
     * @param date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * get Summary info
     *
     * @return
     */
    public String getSummary() {
        return summary;
    }

    /**
     * set Summary info
     *
     * @param summary
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * get Image URL
     *
     * @return
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * set Image URL
     *
     * @param imageUrl
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * get File URL
     *
     * @return
     */
    public String getFileUrl() {
        return fileUrl;
    }

    /**
     * set File URL
     *
     * @param fileUrl
     */
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    /**
     * get Size of PDF
     *
     * @return
     */
    public long getSize() {
        return size;
    }

    /**
     * set Size of PDF
     *
     * @param size
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * set Tab Type
     *
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * get Tab type
     *
     * @return
     */
    public String getType() {
        type = categoryId == 1 ? Newsletter.TYPE_PENSIONER
                : Newsletter.TYPE_EMPLOYEE;
        return type;
    }

    public int getIsFavor() {
        return isFavor;
    }

    public void setIsFavor(int isFavor) {
        this.isFavor = isFavor;
    }

    public boolean checkFavor() {
        return isFavor == IS_FAVOR;
    }

    public List<Integer> getBookmarkPages() {
        if (bookmarkPages == null) {
            bookmarkPages = new ArrayList<Integer>();
        }
        Collections.sort(bookmarkPages);
        return bookmarkPages;
    }

    public void setBookmarkPages(List<Integer> bookmarkPages) {
        this.bookmarkPages = bookmarkPages;
    }

    public void addBookmark(int page) {
        removeBookmark(page);
        synchronized (bookmarkPages) {
            bookmarkPages.add(new Integer(page));
        }
    }

    public boolean isBookmarkPage(int page) {
        if (bookmarkPages == null)
            return false;
        for (Integer p : bookmarkPages) {
            if (p == page) {
                return true;
            }
        }
        return false;
    }

    public void removeBookmark(int page) {
        if (bookmarkPages == null)
            bookmarkPages = new ArrayList<Integer>();
        synchronized (bookmarkPages) {
            int index = -1;
            for (int i = 0; i < bookmarkPages.size(); i++) {
                if (bookmarkPages.get(i) == page) {
                    index = i;
                }
            }
            if (index != -1) {
                bookmarkPages.remove(index);
            }
        }
    }
}
