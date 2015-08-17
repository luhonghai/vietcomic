package com.halosolutions.vietcomic.sqlite.ext;

import android.content.Context;
import android.database.Cursor;

import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.sqlite.DBAdapter;
import com.halosolutions.vietcomic.util.DateHelper;
import com.halosolutions.vietcomic.util.SimpleAppLog;

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
        return
                getDB().query(getTableName(), getAllColumns(),
                        ComicBook.KEY_DELETED + " = 0",
                        null,
                        null,
                        null,
                        ComicBook.KEY_NAME + " ASC");
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
                ComicBook.KEY_CATEGORIES,
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
        comicBook.setStrCategories(cursor.getString(cursor.getColumnIndex(ComicBook.KEY_CATEGORIES)));
        return comicBook;
    }

    public Cursor cursorSearch(String s) throws Exception {
        if (s == null || s.length() == 0) return getAll();
        return getDB().query(getTableName(), getAllColumns(),
                ComicBook.KEY_NAME + " like ? and " + ComicBook.KEY_DELETED + " = 0",
                new String[]{
                        s + "%"
                },
                null,
                null,
                ComicBook.KEY_NAME + " ASC");
    }

    @Override
    public List<ComicBook> search(String s) throws Exception {
        return toCollection(cursorSearch(s));
    }

    public List<ComicBook> listAllFavorites() throws Exception {
        return toCollection(cursorAllFavorites());
    }

    public Cursor cursorAllFavorites() throws Exception {
        return getDB().query(getTableName(), getAllColumns(),
                ComicBook.KEY_FAVORITE + " = 1 and " + ComicBook.KEY_DELETED + " = 0",
                null,
                null,
                null,
                ComicBook.KEY_NAME + " ASC");
    }

    public Cursor cursorAllNew() throws Exception {
        return getDB().query(getTableName(), getAllColumns(),
                ComicBook.KEY_NEW + " = 1 and " + ComicBook.KEY_DELETED + " = 0",
                null,
                null,
                null,
                ComicBook.KEY_NAME + " ASC");
    }

    public List<ComicBook> listAllNew() throws Exception {
        return toCollection(cursorAllNew());
    }

    public Cursor cursorAllHot() throws Exception {
        return (getDB().query(getTableName(), getAllColumns(),
                ComicBook.KEY_HOT + " = 1 and " + ComicBook.KEY_DELETED + " = 0",
                null,
                null,
                null,
                ComicBook.KEY_NAME + " ASC"));
    }

    public List<ComicBook> listAllHot() throws Exception {
        return toCollection(cursorAllHot());
    }

    public Cursor cursorByCategory(String category) throws Exception {
        return getDB().query(getTableName(), getAllColumns(),
                ComicBook.KEY_CATEGORIES + " like ? and " + ComicBook.KEY_DELETED + " = 0",
                new String[]{
                        "%|" + category + "|%"
                },
                null,
                null,
                ComicBook.KEY_NAME + " ASC");
    }

    public List<ComicBook> listByCategory(String category) throws Exception {
        return toCollection(cursorByCategory(category));
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
        try{
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
        }finally {
            cursor.close();
        }
        return super.insert(obj);
    }

    public void bulkInsert(List<ComicBook> comicBooks, boolean useTransaction) throws Exception {
        try {
            if (useTransaction) {
                getDB().beginTransaction();
            }
            for (ComicBook book : comicBooks) {
                //SimpleAppLog.debug("Insert new comic book: " + book.getName() + ". URL: " + book.getUrl());
                insert(book);
            }
            if (useTransaction) {
                getDB().setTransactionSuccessful();
            }
        } finally {
            try {
                if (useTransaction && getDB().inTransaction())
                    getDB().endTransaction();
            } catch (Exception e) {
                SimpleAppLog.error("Could not end transaction", e);
            }
        }
    }
}
