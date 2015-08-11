/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.pension.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.cmg.android.preference.Preference;
import com.cmg.mobile.shared.data.Newsletter;
import com.cmg.mobile.shared.data.NewsletterCategory;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    private static Logger log = Logger.getLogger(DatabaseHandler.class);
    private static final int DATABASE_VERSION = 4;
    // private static final String DB_PATH = "/data/data/databases/";
    private static final String DATABASE_NAME = "newsletter_database";
    private static final String TABLE_NEWSLETTER = "newsletter";
    private static final String TABLE_PREFERENCE = "preference";
    private static final String TABLE_CATEGORY = "category";
    private static final String TABLE_BOOKMARK = "bookmark";

    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DATE = "date";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_FILE = "file";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_SIZE = "size";
    private static final String KEY_DOWNLOAD = "isDownloaded";
    private static final String KEY_CATEGORY = "cat_id";
    private static final String KEY_PAGE = "page";
    private static final String KEY_NEW = "isNew";
    private static final String KEY_FAVOR = "isFavor";

    private static final String KEY_NEWSLETTER_ID = "newsletter_id";

    private static final String CATEGORY_ID = "cat_id";
    private static final String CATEGORY_NAME = "name";
    private static final String PREFERENCE_ID = "pre_id";
    private static final String PREFERENCE_VIEW_TYPE = "view_type";
    private static final String PREFERENCE_SORT_TYPE = "sort_type";

    private static final String CREATE_NEWSLETTER_TABLE = "CREATE TABLE "
            + TABLE_NEWSLETTER + "(" + KEY_ID + " TEXT," + KEY_TITLE + " TEXT,"
            + KEY_DATE + " TEXT," + KEY_DESCRIPTION + " TEXT," + KEY_FILE
            + " TEXT," + KEY_IMAGE + " TEXT," + KEY_SIZE + " INTEGER,"
            + KEY_DOWNLOAD + " INTEGER," + KEY_CATEGORY + " INTEGER,"
            + KEY_PAGE + " INTEGER," + KEY_NEW + " INTEGER," + KEY_FAVOR + " INTEGER" + ")";

    private static final String CREATE_CATEGORY_TABLE = "CREATE TABLE "
            + TABLE_CATEGORY + "(" + CATEGORY_ID + " INTEGER," + CATEGORY_NAME
            + " TEXT" + ")";

    private static final String CREATE_PREFERENCE_TABLE = "CREATE TABLE "
            + TABLE_PREFERENCE + "(" + PREFERENCE_ID + " INTEGER, "
            + PREFERENCE_VIEW_TYPE + " TEXT," + PREFERENCE_SORT_TYPE + " TEXT"
            + ")";

    private static final String CREATE_BOOKMARK_TABLE = "CREATE TABLE " + TABLE_BOOKMARK + "(" + KEY_NEWSLETTER_ID + " TEXT, "
            + KEY_PAGE + " INTEGER" + ")";

    /**
     * Constructor
     *
     * @param context
     */
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Constructor
     *
     * @param context
     * @param name
     * @param factory
     * @param version
     */
    public DatabaseHandler(Context context, String name, CursorFactory factory,
                           int version) {
        super(context, name, factory, version);
    }

    /*
     * 8 (non-Javadoc)
     *
     * @see
     * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
     * .SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOOKMARK_TABLE);
        db.execSQL(CREATE_NEWSLETTER_TABLE);
        db.execSQL(CREATE_CATEGORY_TABLE);
        db.execSQL(CREATE_PREFERENCE_TABLE);
    }

    /*
     * 8 (non-Javadoc)
     *
     * @see
     * android.database.sqlite.SQLiteOpenHelper#onDowngrade(android.database
     * .sqlite.SQLiteDatabase, int, int)
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 1 && oldVersion == 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_NEWSLETTER + " DROP COLUMN "
                        + KEY_PAGE);
            } catch (Exception ex) {
                log.error("Cannot drop column", ex);
            }
        }

        if (newVersion == 2 && oldVersion == 3) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_NEWSLETTER + " DROP COLUMN "
                        + KEY_NEW);
            } catch (Exception ex) {
                log.error("Cannot drop column", ex);
            }
        }

        if (newVersion == 1 && oldVersion == 3) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_NEWSLETTER + " DROP COLUMN "
                        + KEY_PAGE);
                db.execSQL("ALTER TABLE " + TABLE_NEWSLETTER + " DROP COLUMN "
                        + KEY_NEW);
            } catch (Exception ex) {
                log.error("Cannot drop column", ex);
            }
        }

        if (oldVersion == 4) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_NEWSLETTER + " DROP COLUMN "
                        + KEY_FAVOR);
                db.execSQL("DROP TABLE " + TABLE_BOOKMARK);
            } catch (Exception ex) {
                log.error("Cannot drop column", ex);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
     * .SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 2 && oldVersion == 1) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_NEWSLETTER + " ADD "
                        + KEY_PAGE + " INTEGER");
            } catch (Exception ex) {
                log.error("Cannot add column", ex);
            }
        }

        if (newVersion == 3 && oldVersion == 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_NEWSLETTER + " ADD "
                        + KEY_NEW + " INTEGER");
            } catch (Exception ex) {
                log.error("Cannot add column", ex);
            }
        }

        if (newVersion == 3 && oldVersion == 1) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_NEWSLETTER + " ADD "
                        + KEY_PAGE + " INTEGER");
                db.execSQL("ALTER TABLE " + TABLE_NEWSLETTER + " ADD "
                        + KEY_NEW + " INTEGER");
            } catch (Exception ex) {
                log.error("Cannot add column", ex);
            }
        }

        if (newVersion == 4) {
            try {
                db.execSQL(CREATE_BOOKMARK_TABLE);
                db.execSQL("ALTER TABLE " + TABLE_NEWSLETTER + " ADD " + KEY_FAVOR + " INTEGER");
            } catch (Exception e) {
                log.error("Cannot update database", e);
            }
        }
    }

    /**
     * add newsletter
     *
     * @param newsletter
     */
    public void addNewsletter(Newsletter newsletter) {
        SQLiteDatabase db = null;
        Cursor rowData = null;
        try {
            db = this.getWritableDatabase();
            rowData = db.rawQuery("select * from " + TABLE_NEWSLETTER
                    + " where " + KEY_ID + "=?",
                    new String[]{newsletter.getId()});
            boolean isUpdate = (rowData != null && rowData.moveToFirst());
            ContentValues values = new ContentValues();
            values.put(KEY_ID, newsletter.getId());
            values.put(KEY_TITLE, newsletter.getTitle());
            values.put(KEY_DATE, newsletter.getDate());
            values.put(KEY_DESCRIPTION, newsletter.getSummary());
            values.put(KEY_FILE, newsletter.getFileUrl());
            values.put(KEY_IMAGE, newsletter.getImageUrl());
            values.put(KEY_SIZE, newsletter.getSize());
            values.put(KEY_CATEGORY, newsletter.getCategoryId());
            values.put(KEY_PAGE, newsletter.getPage());
            if (isUpdate) {
                db.update(TABLE_NEWSLETTER, values, KEY_ID + "=?",
                        new String[]{newsletter.getId()});
            } else {
                values.put(KEY_DOWNLOAD, newsletter.checkDownloaded());
                values.put(KEY_NEW, newsletter.checkNew());
                values.put(KEY_FAVOR, newsletter.checkFavor());
                db.insert(TABLE_NEWSLETTER, null, values);
            }
        } catch (Exception ex) {
            log.error("Can not create newsletter", ex);
        } finally {
            try {
                if (rowData != null) {
                    rowData.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Exception ex) {
                log.error("Error when close database transaction", ex);
            }
        }
    }

    /**
     * add new category
     *
     * @param id
     * @param name
     */
    public void addCategory(int id, String name) {
        SQLiteDatabase db = null;
        Cursor rowData = null;
        try {
            db = this.getWritableDatabase();
            rowData = db
                    .rawQuery("select * from " + TABLE_CATEGORY + " where "
                            + CATEGORY_ID + "=?",
                            new String[]{Integer.toString(id)});

            if (rowData != null) {
                if (rowData.moveToFirst()) {
                    return;
                }
            }

            ContentValues values = new ContentValues();
            values.put(CATEGORY_ID, id);
            values.put(CATEGORY_NAME, name);

            db.insert(TABLE_CATEGORY, null, values);
        } catch (Exception ex) {
            log.error("Can't add category", ex);
        } finally {
            try {
                if (rowData != null) {
                    rowData.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Exception ex) {
                log.error("Error when close database transaction", ex);
            }
        }
    }

    /**
     * get newsletter by id
     *
     * @param id
     * @return
     */
    public Newsletter getById(String id) {
        SQLiteDatabase db = null;
        Cursor rowData = null;
        log.info("Get newsletter id: " + id);
        try {
            db = this.getReadableDatabase();
            rowData = db.query(TABLE_NEWSLETTER, new String[]{KEY_ID,
                    KEY_TITLE, KEY_DATE, KEY_DESCRIPTION, KEY_FILE, KEY_IMAGE,
                    KEY_SIZE, KEY_DOWNLOAD, KEY_CATEGORY, KEY_PAGE, KEY_NEW, KEY_FAVOR},
                    KEY_ID + "=?", new String[]{id}, null, null, null, null);

            if (rowData != null) {
                rowData.moveToFirst();
            }
            String isNew = rowData.getString(10);
            int newValue = Newsletter.NOT_NEW;
            if (isNew != null && isNew.length() > 0) {
                newValue = Integer.parseInt(isNew);
            }
            int favorValue = Newsletter.NOT_FAVOR;
            String isFavor = rowData.getString(11);
            if (isFavor != null && isFavor.length() > 0) {
                favorValue = Integer.parseInt(isFavor);
            }
            Newsletter newsletter = new Newsletter(rowData.getString(0),
                    rowData.getString(1), rowData.getString(2),
                    rowData.getString(3), rowData.getString(5),
                    rowData.getString(4),
                    Integer.parseInt(rowData.getString(6)),
                    Integer.parseInt(rowData.getString(7)),
                    Integer.parseInt(rowData.getString(8)),
                    Integer.parseInt(rowData.getString(9)), newValue, favorValue);
            return newsletter;
        } catch (Exception ex) {
            log.error("Can not get newsletter by id", ex);
        } finally {
            try {
                if (rowData != null) {
                    rowData.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Exception ex) {
                log.error("Error when close database transaction", ex);
            }
        }
        return null;
    }

    /**
     * get all newsletters
     *
     * @return
     */
    public List<Newsletter> getAllNewsletters() {
        SQLiteDatabase db = null;
        Cursor rowData = null;
        try {
            List<Newsletter> list = new ArrayList<Newsletter>();

            String selectQuery = "SELECT * FROM " + TABLE_NEWSLETTER;

            db = this.getWritableDatabase();
            rowData = db.rawQuery(selectQuery, null);

            if (rowData.moveToFirst()) {
                do {
                    Newsletter newsletter = new Newsletter();
                    newsletter.setId(rowData.getString(0));
                    newsletter.setTitle(rowData.getString(1));
                    newsletter.setDate(rowData.getString(2));
                    newsletter.setSummary(rowData.getString(3));
                    newsletter.setFileUrl(rowData.getString(4));
                    newsletter.setImageUrl(rowData.getString(5));
                    newsletter.setSize(Integer.parseInt(rowData.getString(6)));
                    newsletter.setDownloaded(Integer.parseInt(rowData
                            .getString(7)));
                    newsletter.setCategoryId(Integer.parseInt(rowData
                            .getString(8)));
                    newsletter.setPage(Integer.parseInt(rowData.getString(9)));
                    String isNew = rowData.getString(10);
                    if (isNew != null && isNew.length() > 0) {
                        newsletter.setNew(Integer.parseInt(isNew));
                    } else {
                        newsletter.setNew(Newsletter.NOT_NEW);
                    }

                    String isFavor = rowData.getString(11);
                    if (isFavor != null && isFavor.length() > 0) {
                        newsletter.setIsFavor(Integer.parseInt(isFavor));
                    } else {
                        newsletter.setIsFavor(Newsletter.NOT_FAVOR);
                    }

                    list.add(newsletter);
                } while (rowData.moveToNext());
            }
            return list;
        } catch (Exception ex) {
            log.error("Can not list newsletter", ex);
        } finally {
            try {
                if (rowData != null) {
                    rowData.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Exception ex) {
                log.error("Error when close database transaction", ex);
            }
        }
        return null;
    }

    /**
     * get newsletter by title
     *
     * @param title
     * @return
     */
    public Newsletter getByTitle(String title) {
        return null;
    }

    /**
     * update status isdownload or not
     *
     * @param newsletter
     * @return
     */
    public int updateStatusById(Newsletter newsletter) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_DOWNLOAD,
                    newsletter.checkDownloaded() ? Newsletter.IS_DOWNLOAD : 0);
            int isUpdated = db.update(TABLE_NEWSLETTER, values, KEY_ID + "=?",
                    new String[]{newsletter.getId()});
            return isUpdated;
        } catch (Exception ex) {
            log.error("Can update status newsletter", ex);
        } finally {
            try {
                if (db != null) {
                    db.close();
                }
            } catch (Exception ex) {
                log.error("Error when close database transaction", ex);
            }
        }
        return 0;
    }

    /**
     * update status "NEW" for item
     *
     * @param newsletter
     * @return
     */
    public int updateNewStatusById(Newsletter newsletter) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_NEW, newsletter.getIsNew());
            int isUpdated = db.update(TABLE_NEWSLETTER, values, KEY_ID + "=?",
                    new String[]{newsletter.getId()});

            return isUpdated;
        } catch (Exception ex) {
            log.error("Can update new status newsletter", ex);
        } finally {
            try {
                if (db != null) {
                    db.close();
                }
            } catch (Exception ex) {
                log.error("Error when close database transaction", ex);
            }
        }
        return 0;
    }


    /**
     * update status "FAVOR" for item
     *
     * @param newsletter
     * @return
     */
    public int updateFavorStatus(Newsletter newsletter) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_FAVOR, newsletter.checkFavor());
            int isUpdated = db.update(TABLE_NEWSLETTER, values, KEY_ID + "=?",
                    new String[]{newsletter.getId()});

            return isUpdated;
        } catch (Exception ex) {
            log.error("Can update new status newsletter", ex);
        } finally {
            try {
                if (db != null) {
                    db.close();
                }
            } catch (Exception ex) {
                log.error("Error when close database transaction", ex);
            }
        }
        return 0;
    }

    /**
     * update newsletter
     *
     * @param newsletter
     * @param id
     * @return
     */
    public int updateDatabase(Newsletter newsletter, String id) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_TITLE, newsletter.getTitle());
            values.put(KEY_DATE, newsletter.getDate());
            values.put(KEY_DESCRIPTION, newsletter.getSummary());
            values.put(KEY_FILE, newsletter.getFileUrl());
            values.put(KEY_IMAGE, newsletter.getImageUrl());
            values.put(KEY_SIZE, newsletter.getSize());
            values.put(KEY_CATEGORY, newsletter.getCategoryId());
            values.put(KEY_PAGE, newsletter.getPage());
            int isUpdated = db.update(TABLE_NEWSLETTER, values, KEY_ID + "=?",
                    new String[]{id});
            db.close();
            return isUpdated;
        } catch (Exception ex) {
            log.error("Can not update newsletter", ex);
        } finally {
            try {
                if (db != null) {
                    db.close();
                }
            } catch (Exception ex) {
                log.error("Error when close database transaction", ex);
            }
        }
        return 0;
    }

    /**
     * check newsletter if it's download by id
     *
     * @param id
     * @return
     */
    public boolean checkIsDownloaded(String id) {
        try {
            boolean b = false;
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor rowData = db.query(TABLE_NEWSLETTER,
                    new String[]{KEY_DOWNLOAD}, KEY_ID + "=?",
                    new String[]{id}, null, null, null, null);

            if (rowData != null) {
                rowData.moveToFirst();
                if (rowData.getInt(0) == 1) {
                    b = true;
                }
            }
            rowData.close();
            db.close();
            return b;
        } catch (Exception ex) {
            log.error("Can check isupload newsletter", ex);
        }
        return false;
    }

    /**
     * check newsletter if it's added new by id
     *
     * @param id
     * @return
     */
    public boolean checkIsNew(String id) {
        try {
            boolean b = false;
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor rowData = db.query(TABLE_NEWSLETTER,
                    new String[]{KEY_NEW}, KEY_ID + "=?",
                    new String[]{id}, null, null, null, null);

            if (rowData != null) {
                rowData.moveToFirst();
                int newValue = Newsletter.NOT_NEW;
                try {
                    newValue = rowData.getInt(0);
                } catch (Exception ex) {
                    log.debug("Cannot cast new value", ex);
                }
                if (newValue == 1) {
                    b = true;
                }
            }
            rowData.close();
            db.close();
            return b;
        } catch (Exception e) {
            log.error("Can check isNew newsletter", e);
        }
        return false;
    }

    /**
     * check newsletter if it's added new by id
     *
     * @param id
     * @return
     */
    public boolean checkIsFavor(String id) {
        try {
            boolean b = false;
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor rowData = db.query(TABLE_NEWSLETTER,
                    new String[]{KEY_FAVOR}, KEY_ID + "=?",
                    new String[]{id}, null, null, null, null);

            if (rowData != null) {
                rowData.moveToFirst();
                int value = Newsletter.IS_FAVOR;
                try {
                    value = rowData.getInt(0);
                } catch (Exception ex) {
                    log.debug("Cannot cast new value", ex);
                }
                b = value == Newsletter.IS_FAVOR;
            }
            rowData.close();
            db.close();
            return b;
        } catch (Exception e) {
            log.error("Can check isNew newsletter", e);
        }
        return false;
    }


    /**
     * Remove bookmark
     *
     * @param newsletterId
     * @param page
     */
    public boolean removeBookmark(String newsletterId, int page) {
        SQLiteDatabase db = null;
        Cursor rowData = null;
        try {
            db = this.getWritableDatabase();
            rowData = db.rawQuery("select * from " + TABLE_BOOKMARK
                    + " where " + KEY_NEWSLETTER_ID + "=? and " + KEY_PAGE + "=?",
                    new String[]{newsletterId, String.valueOf(page)});
            boolean isExisted = (rowData != null && rowData.moveToFirst());
            if (isExisted) {
                log.info("bookmark is existed. try to remove bookmark");
                db.delete(TABLE_BOOKMARK, KEY_NEWSLETTER_ID + "=? and " + KEY_PAGE + "=?", new String[]{newsletterId, String.valueOf(page)});
                return true;
            }
        } catch (Exception ex) {
            log.error("Can not create newsletter", ex);
        } finally {
            try {
                if (rowData != null) {
                    rowData.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Exception ex) {
                log.error("Error when close database transaction", ex);
            }
        }
        return false;
    }

    /**
     * Add bookmark
     *
     * @param newsletterId
     * @param page
     */
    public boolean addBookmark(String newsletterId, int page) {
        SQLiteDatabase db = null;
        Cursor rowData = null;
        try {
            db = this.getWritableDatabase();
            rowData = db.rawQuery("select * from " + TABLE_BOOKMARK
                    + " where " + KEY_NEWSLETTER_ID + "=? and " + KEY_PAGE + "=?",
                    new String[]{newsletterId});
            boolean isUpdate = (rowData != null && rowData.moveToFirst());
            ContentValues values = new ContentValues();
            values.put(KEY_NEWSLETTER_ID, newsletterId);
            values.put(KEY_PAGE, page);
            if (!isUpdate) {
                db.insert(TABLE_BOOKMARK, null, values);
                return true;
            }
        } catch (Exception ex) {
            log.error("Can not create newsletter", ex);
        } finally {
            try {
                if (rowData != null) {
                    rowData.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Exception ex) {
                log.error("Error when close database transaction", ex);
            }
        }
        return false;
    }

    public Newsletter getBookmark(Newsletter newsletter) {
        String query = "SELECT " + KEY_PAGE + " FROM " + TABLE_BOOKMARK + " WHERE " + KEY_NEWSLETTER_ID + "=?";

        SQLiteDatabase db = null;
        Cursor rowData = null;
        try {
            db = this.getWritableDatabase();
            rowData = db.rawQuery(query,
                    new String[]{newsletter.getId()});

            if (rowData.moveToFirst()) {
                List<Integer> bookmarkPages = new ArrayList<Integer>();
                do {
                    bookmarkPages.add(new Integer(rowData.getInt(0)));
                } while (rowData.moveToNext());
                newsletter.setBookmarkPages(bookmarkPages);
            }
        } catch (Exception ex) {
            log.error("Can not getBookmark of newsletter", ex);
        } finally {
            try {
                if (rowData != null) {
                    rowData.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Exception ex) {
                log.error("Error when close database transaction", ex);
            }
        }

        return newsletter;
    }

    /**
     * get all newsletter by category
     *
     * @param categoryId
     * @param search
     * @return
     */
    public List<Newsletter> getAllNewslettersByCategory(int categoryId,
                                                        String search) {
        try {
            List<Newsletter> list = new ArrayList<Newsletter>();
            String selectQuery = "";
            boolean isSearch = false;
            String[] args = null;
            if (search != null && search.trim().length() > 0) {
                isSearch = true;
                if (categoryId == NewsletterCategory.FAVORITES) {
                    selectQuery = "SELECT * FROM " + TABLE_NEWSLETTER + " WHERE "
                            + KEY_FAVOR + "=? and (" + KEY_TITLE + " like ? OR "
                            + KEY_DESCRIPTION + " like ?)";
                    args = new String[]{String.valueOf(Newsletter.IS_FAVOR),
                            "%" + search + "%", "%" + search + "%"};
                } else {
                    selectQuery = "SELECT * FROM " + TABLE_NEWSLETTER + " WHERE "
                            + KEY_CATEGORY + "=? and (" + KEY_TITLE + " like ? OR "
                            + KEY_DESCRIPTION + " like ?)";
                    args = new String[]{String.valueOf(categoryId),
                            "%" + search + "%", "%" + search + "%"};
                }
            } else {
                if (categoryId == NewsletterCategory.FAVORITES) {
                    selectQuery = "SELECT * FROM " + TABLE_NEWSLETTER + " WHERE "
                            + KEY_FAVOR + "=?";
                    args = new String[]{String.valueOf(Newsletter.IS_FAVOR)};
                } else {
                    selectQuery = "SELECT * FROM " + TABLE_NEWSLETTER + " WHERE "
                            + KEY_CATEGORY + "=?";
                    args = new String[]{String.valueOf(categoryId)};
                }
            }

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor rowData = db.rawQuery(selectQuery, args);

            if (rowData.moveToFirst()) {
                do {
                    Newsletter newsletter = new Newsletter();
                    newsletter.setId(rowData.getString(0));
                    newsletter.setTitle(rowData.getString(1));
                    newsletter.setDate(rowData.getString(2));
                    newsletter.setSummary(rowData.getString(3));
                    newsletter.setFileUrl(rowData.getString(4));
                    newsletter.setImageUrl(rowData.getString(5));
                    newsletter.setSize(Integer.parseInt(rowData.getString(6)));
                    newsletter.setDownloaded(Integer.parseInt(rowData
                            .getString(7)));
                    newsletter.setCategoryId(Integer.parseInt(rowData
                            .getString(8)));
                    newsletter.setPage(Integer.parseInt(rowData.getString(9)));
                    String isNew = rowData.getString(10);
                    if (isNew != null && isNew.length() > 0) {
                        newsletter.setNew(Integer.parseInt(rowData
                                .getString(10)));
                    } else {
                        newsletter.setNew(Newsletter.NOT_NEW);
                    }
                    String isFavor = rowData.getString(11);
                    if (isFavor != null && isFavor.length() > 0) {
                        newsletter.setIsFavor(Integer.parseInt(isFavor));
                    } else {
                        newsletter.setIsFavor(Newsletter.NOT_FAVOR);
                    }

                    list.add(newsletter);
                } while (rowData.moveToNext());
            }
            rowData.close();
            db.close();
            return list;
        } catch (Exception ex) {
            log.error("Can not list newsletters", ex);
        }
        return null;
    }

    /**
     * update preference
     */
    @Deprecated
    public boolean updatePreference(Preference pre) {
        // try {
        // SQLiteDatabase db = this.getWritableDatabase();
        // boolean isUpdate = false;
        // Cursor rowData = db.rawQuery("select * from " + TABLE_PREFERENCE,
        // null);
        //
        // if (rowData != null && rowData.moveToFirst()) {
        // isUpdate = true;
        // }
        // ContentValues values = new ContentValues();
        // values.put(PREFERENCE_ID, pre.getId());
        // values.put(PREFERENCE_SORT_TYPE, pre.getSortType());
        // values.put(PREFERENCE_VIEW_TYPE, pre.getViewType());
        // if (isUpdate) {
        // db.update(TABLE_PREFERENCE, values, PREFERENCE_ID + "=?",
        // new String[] { Integer.toString(pre.getId()) });
        // } else {
        // db.insert(TABLE_PREFERENCE, null, values);
        // }
        // rowData.close();
        // db.close();
        // return true;
        // } catch (Exception ex) {
        // log.error("Can not update preferences", ex);
        // }
        return false;
    }

    /**
     * get preference
     *
     * @return
     */
    @Deprecated
    public Preference getPreference() {
        /*
         * try { SQLiteDatabase db = this.getWritableDatabase(); Cursor rowData
		 * = db.rawQuery("select * from " + TABLE_PREFERENCE, null);
		 * 
		 * if (rowData != null && rowData.moveToFirst()) { Preference pre = new
		 * Preference(); pre.setId(rowData.getInt(0));
		 * pre.setViewType(rowData.getString(1));
		 * pre.setSortType(rowData.getString(2)); return pre; } rowData.close();
		 * db.close(); } catch (Exception ex) {
		 * log.error("Can not update preferences", ex); }
		 */
        return null;
    }

    /**
     * delete newsletter
     *
     * @param id
     */
    public void deleteNewsletter(String id) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            db.delete(TABLE_NEWSLETTER, KEY_ID + "=?", new String[]{id});
        } catch (Exception ex) {
            log.error("Can not delete newsletter by id", ex);
        } finally {
            try {
                if (db != null) {
                    db.close();
                }
            } catch (Exception ex) {
                log.error("Error when close database transaction", ex);
            }
        }
    }

    /**
     * close database connection
     */
    public void recycle() {
        try {
            if (this.getWritableDatabase().isOpen()) {
                this.getWritableDatabase().close();
            }
            if (this.getReadableDatabase().isOpen()) {
                this.getReadableDatabase().close();
            }
            this.close();
        } catch (Exception ex) {
            log.error("Error when recycle database object", ex);
        }
    }

}
