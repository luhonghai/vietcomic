package com.halosolutions.itranslator.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.Date;

/**
 * Created by luhonghai on 25/02/2015.
 */
public abstract class AbstractData<T> {

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
