package com.halosolutions.mangaworld.sqlite.ext;

import android.content.Context;
import android.database.Cursor;

import com.halosolutions.mangaworld.comic.ComicBook;
import com.halosolutions.mangaworld.sqlite.ComicDatabaseHelper;
import com.halosolutions.mangaworld.util.StringHelper;
import com.luhonghai.litedb.LiteBaseDao;
import com.luhonghai.litedb.exception.AnnotationNotFound;
import com.luhonghai.litedb.exception.InvalidAnnotationData;
import com.luhonghai.litedb.exception.LiteDatabaseException;

import java.util.Date;
import java.util.List;

/**
 * Created by luhonghai on 25/02/2015.
 */
public class ComicBookDBAdapter extends LiteBaseDao<ComicBook> {

    public ComicBookDBAdapter(Context ctx) throws AnnotationNotFound, InvalidAnnotationData {
        super(new ComicDatabaseHelper(ctx), ComicBook.class);
    }

    public Cursor cursorSearch(String s) throws Exception {
        if (s != null) s = StringHelper.removeAccent(s).toLowerCase();
        return query(
                "[search] like ? and [isDeleted] = 0",
                new String[]{
                        "%" + s + "%"
                },
                null,
                null,
                "[name] ASC");
    }

    public List<ComicBook> search(String s) throws Exception {
        return toList(cursorSearch(s));
    }

    public List<ComicBook> listAllFavorites() throws Exception {
        return toList(cursorAllFavorites());
    }

    public Cursor cursorAllFavorites() throws Exception {
        return query(
                "[isFavorite] = 1 and [isDeleted] = 0",
                null,
                null,
                null,
                "[name] ASC");
    }

    public Cursor cursorAllNew() throws Exception {
        return query(
                "[isNew] = 1 and [isDeleted] = 0",
                null,
                null,
                null,
                "[name] ASC");
    }

    public List<ComicBook> listAllNew() throws Exception {
        return toList(cursorAllNew());
    }

    public Cursor cursorAllHot() throws Exception {
        return query(
                "[isHot] = 1 and [isDeleted] = 0",
                null,
                null,
                null,
                "[name] ASC");
    }

    public List<ComicBook> listAllHot() throws Exception {
        return toList(cursorAllHot());
    }

    public Cursor cursorByCategory(String category) throws Exception {
        return query(
                "[strCategories] like ? and [isDeleted] = 0",
                new String[]{
                        "%|" + category + "|%"
                },
                null,
                null,
                "[name] ASC");
    }

    public Cursor cursorByAuthor(String author) throws Exception {
        return query(
                "[author] = ? and [isDeleted] = 0",
                new String[]{
                        author
                },
                null,
                null,
                "[name] ASC");
    }

    public Cursor cursorDownloaded() throws Exception {
        return query(
                "[isDeleted] = 1 and [isDeleted] = 0",
                null,
                null,
                null,
                "[timestamp] DESC");
    }

    public Cursor cursorWatched() throws Exception {
        return query(
                "[isWatched] = 1 and [isDeleted] = 0",
                null,
                null,
                null,
                "[timestamp] DESC");
    }

    public ComicBook getComicByBookId(String bookId) throws Exception {
        Cursor cursor = query(
                "[bookId] = ? and [isDeleted] = 0",
                new String[]{
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
            selection += "[strCategories] like ?";
            args[i] =  "%|" + categories.get(i) + "|%";
            if (i != categories.size() - 1) {
                selection += " or ";
            }
        }
        selection += ")";
        selection +=  " and [isDeleted] = 0";

        return query(
                selection,
                args,
                null,
                null,
                "[name] ASC");
    }

    public List<ComicBook> listByCategory(String category) throws Exception {
        return toList(cursorByCategory(category));
    }

    @Override
    public long insert(ComicBook obj) throws LiteDatabaseException {
        Cursor cursor = query(
                "[bookId] = ?",
                new String[]{
                        obj.getBookId()
                },
                null,
                null,
                null);
        try{
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                ComicBook oldObj = toObject(cursor);
                long oldId = oldObj.getId();
                obj.setId(oldId);
                obj.setIsFavorite(oldObj.isFavorite());
                obj.setIsDownloaded(oldObj.isDownloaded());
                obj.setIsWatched(oldObj.isWatched());
                obj.setCreatedDate(new Date(System.currentTimeMillis()));
                obj.setTimestamp(oldObj.getTimestamp());
                update(obj);
                return oldId;
            }
        }finally {
            cursor.close();
        }
        return super.insert(obj);
    }

    public void bulkInsert(List<ComicBook> comicBooks, boolean useTransaction) throws Exception {
        insert(comicBooks, useTransaction);
    }
}
