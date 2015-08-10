package com.halosolutions.itranslator.sqlite;

import android.content.ContentValues;
import android.content.Context;

import com.halosolutions.itranslator.utilities.DateHelper;

/**
 * Created by cmg on 28/07/15.
 */
public class History extends AbstractData<History> {

    private String phase;

    @Override
    public String toPrettyString(Context context) {
        return phase;
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(DBAdapter.KEY_PHASE, getPhase());
        if (getCreatedDate() != null)
            cv.put(DBAdapter.KEY_CREATED_DATE, DateHelper.convertDateToString(getCreatedDate()));
        return cv;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }
}
