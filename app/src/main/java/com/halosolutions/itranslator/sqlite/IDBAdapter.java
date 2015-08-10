package com.halosolutions.itranslator.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Created by luhonghai on 25/02/2015.
 */
public interface IDBAdapter<T> {
    /**
     *  FIELD
     */
    public static final String KEY_ROW_ID = "_id";

    public static final String KEY_CREATED_DATE = "created_date";

    public static final String KEY_PHASE = "phase";

    /**
     *  TABLE
     */

    public static final String TABLE_HISTORY = "history";

    public SQLiteDatabase open() throws SQLException;

    public void close();

    public Cursor getAll() throws Exception;

    public String getTableName();

    public String[] getAllColumns();

    public long insert(T obj) throws Exception;

    public boolean update(T obj) throws Exception;

    public Cursor getAll(String orderBy) throws Exception;

    public Cursor get(long rowId) throws Exception;

    public boolean delete(long rowId) throws Exception;

    public boolean delete(T obj) throws Exception;

    public List<T> toCollection(Cursor cursor);

    public abstract T toObject(Cursor cursor);

    public T find(long rowId) throws Exception;

    public List<T> findAll() throws Exception;

    public List<T> search(String s) throws Exception;
}
