package com.halosolutions.vietcomic.sqlite.ext;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.widget.CursorAdapter;

import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.comic.ComicService;
import com.halosolutions.vietcomic.sqlite.AbstractData;
import com.halosolutions.vietcomic.sqlite.DBAdapter;
import com.halosolutions.vietcomic.util.DateHelper;
import com.halosolutions.vietcomic.util.SimpleAppLog;
import com.halosolutions.vietcomic.util.StringHelper;

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
                ComicBook.KEY_DOWNLOADED,
                ComicBook.KEY_WATCHED,
                ComicBook.KEY_CATEGORIES,
                ComicBook.KEY_TIMESTAMP,
                ComicBook.KEY_CREATED_DATE,
                ComicBook.KEY_SERVICE
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
        comicBook.setService(cursor.getString(cursor.getColumnIndex(ComicBook.KEY_SERVICE)));
        comicBook.setIsDeleted(cursor.getInt(cursor.getColumnIndex(ComicBook.KEY_DELETED)) == 1);
        comicBook.setIsNew(cursor.getInt(cursor.getColumnIndex(ComicBook.KEY_NEW)) == 1);
        comicBook.setIsHot(cursor.getInt(cursor.getColumnIndex(ComicBook.KEY_HOT)) == 1);
        comicBook.setIsFavorite(cursor.getInt(cursor.getColumnIndex(ComicBook.KEY_FAVORITE)) == 1);
        comicBook.setIsDownloaded(cursor.getInt(cursor.getColumnIndex(ComicBook.KEY_DOWNLOADED)) == 1);
        comicBook.setIsWatched(cursor.getInt(cursor.getColumnIndex(ComicBook.KEY_WATCHED)) == 1);
        comicBook.setCreatedDate(DateHelper.convertStringToDate(cursor.getString(cursor.getColumnIndex(ComicBook.KEY_CREATED_DATE))));
        comicBook.setTimestamp(DateHelper.convertStringToDate(cursor.getString(cursor.getColumnIndex(ComicBook.KEY_TIMESTAMP))));
        comicBook.setStrCategories(cursor.getString(cursor.getColumnIndex(ComicBook.KEY_CATEGORIES)));
        return comicBook;
    }

    public Cursor cursorSearch(String s) throws Exception {
        if (s != null) s = StringHelper.removeAccent(s).toLowerCase();
        return getDB().query(getTableName(), getAllColumns(),
                ComicBook.KEY_SEARCH + " like ? and " + ComicBook.KEY_DELETED + " = 0",
                new String[]{
                        "%" + s + "%"
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

    public Cursor cursorByAuthor(String author) throws Exception {
        return getDB().query(getTableName(), getAllColumns(),
                ComicBook.KEY_AUTHOR + " = ? and " + ComicBook.KEY_DELETED + " = 0",
                new String[]{
                        author
                },
                null,
                null,
                ComicBook.KEY_NAME + " ASC");
    }

    public Cursor cursorDownloaded() throws Exception {
        return getDB().query(getTableName(), getAllColumns(),
                ComicBook.KEY_DOWNLOADED + " = 1 and " + ComicBook.KEY_DELETED + " = 0",
                null,
                null,
                null,
                ComicBook.KEY_TIMESTAMP + " DESC");
    }

    public Cursor cursorWatched() throws Exception {
        return getDB().query(getTableName(), getAllColumns(),
                ComicBook.KEY_WATCHED + " = 1 and " + ComicBook.KEY_DELETED + " = 0",
                null,
                null,
                null,
                ComicBook.KEY_TIMESTAMP + " DESC");
    }

    public ComicBook getComicByBookId(String bookId) throws Exception {
        Cursor cursor = getDB().query(getTableName(), getAllColumns(),
                ComicBook.KEY_BOOK_ID+ " = ? and " + ComicBook.KEY_DELETED + " = 0",
                new String[] {
                        bookId
                },
                null,
                null,
                null);
        if (cursor.moveToFirst()) {
            ComicBook comicBook = toObject(cursor);
            cursor.close();
            return comicBook;
        }
        return null;
    }

    public Cursor cursorByCategories(List<String> categories) throws Exception {
        if (categories == null || categories.size() == 0) return null;
        String selection = "(";
        String[] args = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            selection += ComicBook.KEY_CATEGORIES + " like ?";
            args[i] =  "%|" + categories.get(i) + "|%";
            if (i != categories.size() - 1) {
                selection += " or ";
            }
        }
        selection += ")";
        selection +=  " and " + ComicBook.KEY_DELETED + " = 0";
        return getDB().query(getTableName(), getAllColumns(),
                selection,
                args,
                null,
                null,
                ComicBook.KEY_NAME + " ASC");
    }

    public List<ComicBook> listByCategory(String category) throws Exception {
        return toCollection(cursorByCategory(category));
    }

    public void cleanHotComic() throws Exception {
        ContentValues cv = new ContentValues();
        cv.put(AbstractData.KEY_HOT, 0);
        getDB().update(getTableName(), cv, AbstractData.KEY_HOT + " = 1", null);
    }

    public void cleanNewComic() throws Exception {
        ContentValues cv = new ContentValues();
        cv.put(AbstractData.KEY_NEW, 0);
        getDB().update(getTableName(), cv, AbstractData.KEY_NEW + " = 1", null);
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
                obj.setId(oldId);
                obj.setIsFavorite(cursor.getInt(cursor.getColumnIndex(ComicBook.KEY_FAVORITE)) == 1);
                obj.setIsDownloaded(cursor.getInt(cursor.getColumnIndex(ComicBook.KEY_DOWNLOADED)) == 1);
                obj.setIsWatched(cursor.getInt(cursor.getColumnIndex(ComicBook.KEY_WATCHED)) == 1);
                obj.setCreatedDate(new Date(System.currentTimeMillis()));
                obj.setTimestamp(DateHelper.convertStringToDate(cursor.getString(cursor.getColumnIndex(ComicBook.KEY_TIMESTAMP))));
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
                if (!book.getService().equalsIgnoreCase(ComicService.SEVICE_VIETCOMIC_V2))
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
