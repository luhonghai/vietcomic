package com.halosolutions.mangaworld.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by luhonghai on 25/02/2015.
 */
public abstract class DBAdapter<T> implements IDBAdapter<T> {

    private static final String TAG = "DBAdapter";

    private static final String DATABASE_NAME = "main_db";

    private static final int DATABASE_VERSION = 1;

    private static final String[] DATABASE_TABLE_CREATE = new String[] {
            "create table " + AbstractData.TABLE_COMIC_BOOK
                    + " ("
                    + AbstractData.KEY_ROW_ID +" integer primary key autoincrement, "
                    + AbstractData.KEY_SEARCH + " text, "
                    + AbstractData.KEY_AUTHOR + " text, "
                    + AbstractData.KEY_BOOK_ID + " text, "
                    + AbstractData.KEY_DESCRIPTION + " text, "
                    + AbstractData.KEY_NAME + " text, "
                    + AbstractData.KEY_OTHER_NAME + " text, "
                    + AbstractData.KEY_STATUS + " text, "
                    + AbstractData.KEY_THUMBNAIL + " text, "
                    + AbstractData.KEY_URL + " text, "
                    + AbstractData.KEY_SOURCE + " text, "
                    + AbstractData.KEY_SERVICE + " text, "
                    + AbstractData.KEY_CATEGORIES + " text, "
                    + AbstractData.KEY_RATE + " integer, "
                    + AbstractData.KEY_DELETED + " integer, "
                    + AbstractData.KEY_NEW + " integer, "
                    + AbstractData.KEY_HOT + " integer, "
                    + AbstractData.KEY_FAVORITE + " integer, "
                    + AbstractData.KEY_DOWNLOADED + " integer, "
                    + AbstractData.KEY_WATCHED + " integer, "
                    + AbstractData.KEY_TIMESTAMP + " date,"
                    + AbstractData.KEY_CREATED_DATE + " date not null"
                    + ");",
            "create table " + AbstractData.TABLE_COMIC_CHAPTER
                    + " ("
                    + AbstractData.KEY_ROW_ID +" integer primary key autoincrement, "
                    + AbstractData.KEY_BOOK_ID + " text, "
                    + AbstractData.KEY_NAME + " text, "
                    + AbstractData.KEY_STATUS + " integer, "
                    + AbstractData.KEY_URL + " text, "
                    + AbstractData.KEY_CHAPTER_ID + " text, "
                    + AbstractData.KEY_FILE_PATH + " text, "
                    + AbstractData.KEY_PUBLISH_DATE + " date, "
                    + AbstractData.KEY_IMAGE_COUNT + " integer, "
                    + AbstractData.KEY_COMPLETED_COUNT + " integer, "
                    + AbstractData.KEY_INDEX + " integer, "
                    + AbstractData.KEY_TIMESTAMP + " date,"
                    + AbstractData.KEY_CREATED_DATE + " date not null"
                    + ");",
            "create table " + AbstractData.TABLE_COMIC_CHAPTER_PAGE
                    + " ("
                    + AbstractData.KEY_ROW_ID +" integer primary key autoincrement, "
                    + AbstractData.KEY_BOOK_ID + " text, "
                    + AbstractData.KEY_STATUS + " integer, "
                    + AbstractData.KEY_URL + " text, "
                    + AbstractData.KEY_CHAPTER_ID + " text, "
                    + AbstractData.KEY_FILE_PATH + " text, "
                    + AbstractData.KEY_INDEX + " integer, "
                    + AbstractData.KEY_TASK_ID + " integer, "
                    + AbstractData.KEY_PAGE_ID + " text, "
                    + AbstractData.KEY_CREATED_DATE + " date not null"
                    + ");",
    };

    private final Context context;

    private DatabaseHelper DBHelper;

    private SQLiteDatabase db;


    public DBAdapter(Context ctx)
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(getContext());
    }

    public Context getContext() {
        return context;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        private Context currentContext;

        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.currentContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            for (String query : DATABASE_TABLE_CREATE) {
                db.execSQL(query);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion)
        {
            //Log.w(TAG, "Upgrading database from version " + oldVersion
            //        + " to "
            //        + newVersion + ", which will destroy all old data");
            //db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            //onCreate(db);
        }
    }

    protected SQLiteDatabase getDB() {
        return db;
    }

    //---opens the database---
    public SQLiteDatabase open() throws SQLException
    {
        db = DBHelper.getWritableDatabase();
        while(db.isDbLockedByCurrentThread() || db.isDbLockedByOtherThreads()) {
            //db is locked, keep looping
        }
        return db;
    }

    //---closes the database---
    public void close()
    {
        try {
            DBHelper.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public long insert(T obj) throws Exception {
        if (obj instanceof AbstractData) {
            AbstractData data = (AbstractData) obj;
            data.setCreatedDate(new Date(System.currentTimeMillis()));
            return getDB().insert(getTableName(), null, data.toContentValues());
        } else {
            return -1;
        }
    }

    public void insert(List<T> objs) throws Exception {
        if (objs != null && objs.size() > 0) {
            for (T obj : objs) {
                insert(obj);
            }
        }
    }

    public boolean update(T obj) throws Exception {
        if (obj instanceof AbstractData) {
            AbstractData data = (AbstractData) obj;
            return getDB().update(getTableName(), data.toContentValues(),
                    AbstractData.KEY_ROW_ID + "=" + data.getId(), null) > 0;
        } else {
            return false;
        }
    }

    public Cursor getAll(String orderBy) throws Exception {
        return getDB().query(getTableName(), getAllColumns(),
                null,
                null,
                null,
                null,
                orderBy);
    }

    public Cursor get(long rowId) throws Exception {
        Cursor mCursor =
                getDB().query(true, getTableName(), getAllColumns(),
                        AbstractData.KEY_ROW_ID + "=" + rowId,
                        null,
                        null,
                        null,
                        null,
                        null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean delete(T obj) throws Exception {
        if (obj instanceof AbstractData) {
            return delete(((AbstractData) obj).getId());
        }
        return false;
    }

    public boolean delete(long rowId) throws Exception {
        return getDB().delete(getTableName(), AbstractData.KEY_ROW_ID + "=" + rowId, null) > 0;
    }

    public List<T> toCollection(Cursor cursor) {
        List<T> list = new ArrayList<T>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            list.add(toObject(cursor));
            cursor.moveToNext();
        }
        return list;
    }

    public T find(long rowId) throws Exception {
        return toObject(get(rowId));
    }

    public List<T> findAll() throws Exception {
        return toCollection(getAll());
    }

    public int count() throws Exception {
        Cursor mCount= db.rawQuery("select count(*) from " + getTableName(), null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();
        return count;
    }
}
