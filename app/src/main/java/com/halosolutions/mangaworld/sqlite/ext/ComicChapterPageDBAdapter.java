package com.halosolutions.mangaworld.sqlite.ext;

import android.content.Context;
import android.database.Cursor;

import com.halosolutions.mangaworld.comic.ComicChapterPage;
import com.halosolutions.mangaworld.sqlite.ComicDatabaseHelper;
import com.halosolutions.mangaworld.util.SimpleAppLog;
import com.luhonghai.litedb.LiteBaseDao;
import com.luhonghai.litedb.exception.AnnotationNotFound;
import com.luhonghai.litedb.exception.InvalidAnnotationData;
import com.luhonghai.litedb.exception.LiteDatabaseException;

import java.util.List;

/**
 * Created by cmg on 19/08/15.
 */
public class ComicChapterPageDBAdapter extends LiteBaseDao<ComicChapterPage> {

    public ComicChapterPageDBAdapter(Context ctx) throws AnnotationNotFound, InvalidAnnotationData {
        super(new ComicDatabaseHelper(ctx), ComicChapterPage.class);
    }

    public void deleteByComicChapter(String chapterId) throws Exception {
        delete(
                "[chapterId] = ?",
                new String[]{
                        chapterId
                });
    }

    public List<ComicChapterPage> listByComicChapter(String chapterId) throws Exception {
        Cursor c = cursorByComicChapter(chapterId);
        List<ComicChapterPage> pages = toList(c);
        c.close();
        return pages;
    }

    public Cursor cursorByComicChapter(String chapterId) throws Exception {
        return query(
                "[chapterId] = ? ",
                new String[]{
                        chapterId
                },
                null,
                null,
                "[index] ASC",
                null
        );
    }

    @Override
    public long insert(ComicChapterPage obj) throws LiteDatabaseException {
        Cursor cursor = query(
                "[pageId] = ?",
                new String[]{
                        obj.getPageId()
                },
                null,
                null,
                null,
                null
        );
        if (cursor.moveToFirst()) {
            ComicChapterPage oldPage = toObject(cursor);
            cursor.close();
            obj.setId(oldPage.getId());
            obj.setTaskId(oldPage.getTaskId());
            obj.setStatus(oldPage.getStatus());
            obj.setFilePath(oldPage.getFilePath());
            super.update(obj);
            return oldPage.getId();
        } else {
            return super.insert(obj);
        }
    }

    public ComicChapterPage getByTaskId(long taskId) {
        Cursor cursor = null;
        try {
            cursor = query(
                    "[taskId] = ? ",
                    new String[]{
                            Long.toString(taskId)
                    },
                    null,
                    null,
                    null,
                    null
            );
        } catch (LiteDatabaseException e) {
            SimpleAppLog.error("Could not get task by Id",e);
        }
        if (cursor != null && cursor.moveToFirst()) {
            ComicChapterPage page = null;
            try {
                page = toObject(cursor);
            } catch (LiteDatabaseException e) {
                SimpleAppLog.error("Could not get task by Id",e);
            } finally {
                cursor.close();
            }

            return page;
        }
        return null;
    }

    public Cursor listByStatus(Integer[] status) {
        String selection = "";
        String[] params = new String[status.length];
        for (int i = 0; i < status.length; i++) {
            params[i] = Integer.toString(status[i]);
            selection += ("[status] = ?");
            if (i != status.length - 1) {
                selection += " or ";
            }
        }

        try {
            return query(
                    selection,
                    params,
                    null,
                    null,
                    "[status] ASC",
                    null
            );
        } catch (LiteDatabaseException e) {
            SimpleAppLog.error("Could not list by status", e);
            return null;
        }
    }
}
