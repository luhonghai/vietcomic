package com.halosolutions.mangaworld.sqlite;

import com.luhonghai.litedb.annotation.LiteColumn;

import java.util.Date;

/**
 * Created by luhonghai on 25/02/2015.
 */
public abstract class AbstractData {

    @LiteColumn(name = "_id", isAutoincrement = true, isPrimaryKey = true)
    private long id;

    @LiteColumn
    private Date createdDate;

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
