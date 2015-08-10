package com.halosolutions.itranslator.sqlite.ext;

import android.content.Context;
import android.database.Cursor;

import com.halosolutions.itranslator.sqlite.DBAdapter;
import com.halosolutions.itranslator.sqlite.History;
import com.halosolutions.itranslator.utilities.DateHelper;

import java.util.Date;
import java.util.List;

/**
 * Created by luhonghai on 25/02/2015.
 */
public class HistoryDBAdapter extends DBAdapter<History> {

    private static final String QUERY_SELECT_HISTORY_BY_PHASE = "select h." + KEY_ROW_ID
            + ", h." + KEY_PHASE
            + ", h." + KEY_CREATED_DATE
            + " from " +
            " ((" + TABLE_HISTORY + " as h " +
            " where h." + KEY_PHASE + "=?";

    public HistoryDBAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public Cursor getAll() throws Exception {
        return getAll(KEY_CREATED_DATE + " DESC");
    }

    @Override
    public String getTableName() {
        return TABLE_HISTORY;
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {
                KEY_ROW_ID,
                KEY_PHASE,
                KEY_CREATED_DATE
        };
    }

    public History findByPhase(String phase) {
        Cursor cursor = getDB().rawQuery(QUERY_SELECT_HISTORY_BY_PHASE,
                new String[] {
                        phase
                });
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            return toObject(cursor);
        }
        return null;
    }

    @Override
    public History toObject(Cursor cursor) {
        History history = new History();
        history.setId(cursor.getInt(cursor.getColumnIndex(KEY_ROW_ID)));
        history.setPhase(cursor.getString(cursor.getColumnIndex(KEY_PHASE)));
        history.setCreatedDate(DateHelper.convertStringToDate(cursor.getString(cursor.getColumnIndex(KEY_CREATED_DATE))));
        return history;
    }

    @Override
    public List<History> search(String s) throws Exception {
        if (s == null || s.length() == 0) return findAll();
        return toCollection(getDB().query(getTableName(), getAllColumns(),
                KEY_PHASE + " like ?",
                new String[] {
                        "%" + s + "%"
                },
                null,
                null,
                KEY_CREATED_DATE + " DESC"));
    }

    @Override
    public long insert(History obj) throws Exception {
        Cursor cursor = getDB().query(getTableName(), getAllColumns(),
                KEY_PHASE + "=?",
                new String[]{
                        obj.getPhase()
                },
                null,
                null,
                null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            long oldId =cursor.getLong(cursor.getColumnIndex(KEY_ROW_ID));
            obj.setId(oldId);
            obj.setCreatedDate(new Date(System.currentTimeMillis()));
            update(obj);
            return oldId;
        }
        return super.insert(obj);
    }
}
