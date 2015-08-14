package com.halosolutions.vietcomic.sqlite.ext;

import android.content.Context;
import android.database.Cursor;

import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.sqlite.DBAdapter;
import com.halosolutions.vietcomic.util.DateHelper;

import java.util.Date;
import java.util.List;

/**
 * Created by luhonghai on 25/02/2015.
 */
public class ComicBookDBAdapter extends DBAdapter<ComicBook> {

    public ComicBookDBAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public Cursor getAll() throws Exception {
        return getAll(ComicBook.KEY_NAME + " ASC");
    }

    @Override
    public String getTableName() {
        return ComicBook.TABLE_COMIC_BOOK;
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {
                ComicBook.KEY_ROW_ID,
                ComicBook.KEY_NAME,
                ComicBook.KEY_DESCRIPTION,
                ComicBook.KEY_AUTHOR,
                ComicBook.KEY_RATE,
                ComicBook.KEY_URL,
                ComicBook.KEY_STATUS,
                ComicBook.KEY_OTHER_NAME,
                ComicBook.KEY_BOOK_ID,
                ComicBook.KEY_THUMBNAIL,
                ComicBook.KEY_SOURCE,
                ComicBook.KEY_DELETED,
                ComicBook.KEY_NEW,
                ComicBook.KEY_HOT,
                ComicBook.KEY_FAVORITE,
                ComicBook.KEY_CREATED_DATE,
        };
    }

    @Override
    public ComicBook toObject(Cursor cursor) {
        ComicBook comicBook = new ComicBook();
        comicBook.setId(cursor.getInt(cursor.getColumnIndex(ComicBook.KEY_ROW_ID)));
        comicBook.setName(cursor.getString(cursor.getColumnIndex(ComicBook.KEY_NAME)));
        comicBook.setAuthor(cursor.getString(cursor.getColumnIndex(ComicBook.KEY_AUTHOR)));
        comicBook.setBookId(cursor.getString(cursor.getColumnIndex(ComicBook.KEY_BOOK_ID)));
        comicBook.setDescription(cursor.getString(cursor.getColumnIndex(ComicBook.KEY_DESCRIPTION)));
        comicBook.setOtherName(cursor.getString(cursor.getColumnIndex(ComicBook.KEY_OTHER_NAME)));
        comicBook.setRate(cursor.getFloat(cursor.getColumnIndex(ComicBook.KEY_RATE)));
        comicBook.setStatus(cursor.getString(cursor.getColumnIndex(ComicBook.KEY_STATUS)));
        comicBook.setThumbnail(cursor.getString(cursor.getColumnIndex(ComicBook.KEY_THUMBNAIL)));
        comicBook.setUrl(cursor.getString(cursor.getColumnIndex(ComicBook.KEY_URL)));
        comicBook.setSource(cursor.getString(cursor.getColumnIndex(ComicBook.KEY_SOURCE)));
        comicBook.setIsDeleted(cursor.getInt(cursor.getColumnIndex(ComicBook.KEY_DELETED)) == 1);
        comicBook.setIsNew(cursor.getInt(cursor.getColumnIndex(ComicBook.KEY_NEW)) == 1);
        comicBook.setIsHot(cursor.getInt(cursor.getColumnIndex(ComicBook.KEY_HOT)) == 1);
        comicBook.setIsFavorite(cursor.getInt(cursor.getColumnIndex(ComicBook.KEY_FAVORITE)) == 1);
        comicBook.setCreatedDate(DateHelper.convertStringToDate(cursor.getString(cursor.getColumnIndex(ComicBook.KEY_CREATED_DATE))));
        return comicBook;
    }

    @Override
    public List<ComicBook> search(String s) throws Exception {
        if (s == null || s.length() == 0) return findAll();
        return toCollection(getDB().query(getTableName(), getAllColumns(),
                ComicBook.KEY_NAME + " like ?",
                new String[] {
                        s + "%"
                },
                null,
                null,
                ComicBook.KEY_NAME + " ASC"));
    }

    @Override
    public long insert(ComicBook obj) throws Exception {
        Cursor cursor = getDB().query(getTableName(), getAllColumns(),
                ComicBook.KEY_BOOK_ID + "=?",
                new String[]{
                        obj.getBookId()
                },
                null,
                null,
                null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            long oldId =cursor.getLong(cursor.getColumnIndex(ComicBook.KEY_ROW_ID));
            boolean isFavorite = cursor.getInt(cursor.getColumnIndex(ComicBook.KEY_FAVORITE)) == 1;
            obj.setId(oldId);
            obj.setIsFavorite(isFavorite);
            obj.setCreatedDate(new Date(System.currentTimeMillis()));
            update(obj);
            return oldId;
        }
        return super.insert(obj);
    }
}
