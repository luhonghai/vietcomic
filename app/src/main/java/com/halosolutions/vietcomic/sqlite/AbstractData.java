package com.halosolutions.vietcomic.sqlite;

import android.content.ContentValues;
import android.content.Context;

import java.util.Date;

/**
 * Created by luhonghai on 25/02/2015.
 */
public abstract class AbstractData<T> {

    /**
     *  FIELD
     */
    public static final String KEY_ROW_ID = "_id";

    public static final String KEY_CREATED_DATE = "created_date";

    public static final String TABLE_COMIC_BOOK = "comic_book";

    public static final String TABLE_COMIC_CHAPTER = "comic_chapter";

    public static final String TABLE_COMIC_CHAPTER_PAGE = "comic_chapter_page";

    /**
     *  Common field
     */

    public static final String KEY_OTHER_NAME = "other_name";

    public static final String KEY_STATUS = "status";

    public static final String KEY_URL = "url";

    public static final String KEY_THUMBNAIL = "thumbnail";

    public static final String KEY_AUTHOR = "author";

    public static final String KEY_RATE = "rate";

    public static final String KEY_DESCRIPTION = "description";

    public static final String KEY_SOURCE = "source";

    public static final String KEY_DELETED = "is_deleted";

    public static final String KEY_NEW = "is_new";

    public static final String KEY_HOT = "is_hot";

    public static final String KEY_FAVORITE = "is_favorite";

    public static final String KEY_DOWNLOADED = "is_downloaded";

    public static final String KEY_WATCHED = "is_watched";

    public static final String KEY_BOOK_ID = "book_id";

    public static final String KEY_NAME = "name";

    public static final String KEY_CATEGORIES = "categories";

    public static final String KEY_SEARCH = "search";

    public static final String KEY_CHAPTER_ID = "chapter_id";

    public static final String KEY_FILE_PATH = "file_path";

    public static final String KEY_INDEX = "c_index";

    public static final String KEY_PUBLISH_DATE = "publish_date";

    public static final String KEY_IMAGE_COUNT = "image_count";

    public static final String KEY_TASK_ID = "task_id";

    public static final String KEY_PAGE_ID = "page_id";

    public static final String KEY_COMPLETED_COUNT = "completed_count";

    public static final String KEY_TIMESTAMP = "c_timestamp";

    public static final String KEY_SERVICE = "service";

    private long id;

    private Date createdDate;

    public abstract String toPrettyString(Context context);

    public abstract ContentValues toContentValues();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
