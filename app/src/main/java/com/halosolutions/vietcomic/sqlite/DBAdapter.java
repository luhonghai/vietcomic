package com.halosolutions.vietcomic.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.halosolutions.vietcomic.comic.ComicBook;

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
            "create table " + ComicBook.TABLE_COMIC_BOOK
                    + " ("
                    + AbstractData.KEY_ROW_ID +" integer primary key autoincrement, "
                    + ComicBook.KEY_AUTHOR + " text, "
                    + ComicBook.KEY_BOOK_ID + " text, "
                    + ComicBook.KEY_DESCRIPTION + " text, "
                    + ComicBook.KEY_NAME + " text, "
                    + ComicBook.KEY_OTHER_NAME + " text, "
                    + ComicBook.KEY_STATUS + " text, "
                    + ComicBook.KEY_THUMBNAIL + " text, "
                    + ComicBook.KEY_URL + " text, "
                    + ComicBook.KEY_SOURCE + " text, "
                    + ComicBook.KEY_RATE + " integer, "
                    + ComicBook.KEY_DELETED + " integer, "
                    + ComicBook.KEY_NEW + " integer, "
                    + ComicBook.KEY_HOT + " integer, "
                    + ComicBook.KEY_FAVORITE + " integer, "
                    + AbstractData.KEY_CREATED_DATE + " date not null"
                    + ");"

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
}
