package com.halosolutions.mangaworld.sqlite.ext;

import android.content.Context;
import android.database.Cursor;

import com.halosolutions.mangaworld.comic.ComicBook;
import com.halosolutions.mangaworld.comic.ComicChapter;
import com.halosolutions.mangaworld.sqlite.ComicDatabaseHelper;
import com.halosolutions.mangaworld.util.SimpleAppLog;
import com.luhonghai.litedb.LiteBaseDao;
import com.luhonghai.litedb.exception.AnnotationNotFound;
import com.luhonghai.litedb.exception.InvalidAnnotationData;
import com.luhonghai.litedb.exception.LiteDatabaseException;

import java.util.Set;

/**
 * Created by cmg on 19/08/15.
 */
public class ComicChapterDBAdapter extends LiteBaseDao<ComicChapter> {

    public ComicChapterDBAdapter(Context ctx) throws AnnotationNotFound, InvalidAnnotationData {
        super(new ComicDatabaseHelper(ctx), ComicChapter.class);
    }

    public Cursor listByComic(ComicBook book) {
        try {
            return query("[bookId] = ? ",
                    new String[]{
                            book.getBookId()
                    },
                    null,
                    null,
                    "[index] ASC",
                    null
            );
        } catch (LiteDatabaseException e) {
            SimpleAppLog.error("Could not list by comic book",e);
            return null;
        }
    }

    public Cursor listByStatus(Integer[] status, Set<String> chapterIds, String limit) {
        String selection = "(";
        int statusLength = status.length;
        String[] params = new String[statusLength + ((chapterIds != null) ? chapterIds.size() : 0)];
        for (int i = 0; i < statusLength; i++) {
            params[i] = Integer.toString(status[i]);
            selection += ("[status] = ?");
            if (i != statusLength - 1) {
                selection += " or ";
            }
        }
        selection += ")";
        if (chapterIds != null && chapterIds.size() > 0) {
            selection += " and [chapterId] not in (";
            int count = 0;
            for (String chapterId : chapterIds) {
                params[statusLength + count++] = chapterId;
                selection += "?";
                if (count != params.length - 1) {
                    selection += ",";
                }
            }
            selection += ")";
        }

        try {
            return query(
                    selection,
                    params,
                    null,
                    null,
                    "[status] ASC, [timestamp] ASC",
                    limit
            );
        } catch (LiteDatabaseException e) {
            SimpleAppLog.error("Could not list by status", e);
            return null;
        }
    }

    public Cursor listByStatus(Integer[] status) {
        return listByStatus(status,null, null);
    }

    public ComicChapter getByChapterId(String chapterId) {
        Cursor cursor = null;
        try {
            cursor = query(
                    "[chapterId] = ? ",
                    new String[] {
                            chapterId
                    },
                    null,
                    null,
                    null,
                    null
            );
        } catch (LiteDatabaseException e) {
            SimpleAppLog.error("Could not get chapter by id", e);
        }
        if (cursor != null && cursor.moveToFirst()) {
            ComicChapter chapter = null;
            try {
                chapter = toObject(cursor);
            } catch (LiteDatabaseException e) {
                SimpleAppLog.error("Could not get chapter by id", e);
            } finally {
                cursor.close();
            }
            return chapter;
        }
        return null;
    }
}
