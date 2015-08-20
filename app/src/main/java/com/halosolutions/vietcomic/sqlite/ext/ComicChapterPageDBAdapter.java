package com.halosolutions.vietcomic.sqlite.ext;

import android.content.Context;
import android.database.Cursor;

import com.halosolutions.vietcomic.comic.ComicChapter;
import com.halosolutions.vietcomic.comic.ComicChapterPage;
import com.halosolutions.vietcomic.sqlite.AbstractData;
import com.halosolutions.vietcomic.sqlite.DBAdapter;
import com.halosolutions.vietcomic.util.DateHelper;

import java.util.List;

/**
 * Created by cmg on 19/08/15.
 */
public class ComicChapterPageDBAdapter extends DBAdapter<ComicChapterPage> {

    public ComicChapterPageDBAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public Cursor getAll() throws Exception {
        return getAll(AbstractData.KEY_INDEX + " ASC");
    }

    @Override
    public String getTableName() {
        return AbstractData.TABLE_COMIC_CHAPTER_PAGE;
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {
                AbstractData.KEY_ROW_ID,
                AbstractData.KEY_BOOK_ID,
                AbstractData.KEY_CHAPTER_ID,
                AbstractData.KEY_URL,
                AbstractData.KEY_FILE_PATH,
                AbstractData.KEY_STATUS,
                AbstractData.KEY_INDEX,
                AbstractData.KEY_TASK_ID,
                AbstractData.KEY_PAGE_ID,
                AbstractData.KEY_CREATED_DATE
        };
    }

    @Override
    public ComicChapterPage toObject(Cursor cursor) {
        ComicChapterPage page = new ComicChapterPage();
        page.setId(cursor.getLong(cursor.getColumnIndex(AbstractData.KEY_ROW_ID)));
        page.setBookId(cursor.getString(cursor.getColumnIndex(AbstractData.KEY_BOOK_ID)));
        page.setChapterId(cursor.getString(cursor.getColumnIndex(AbstractData.KEY_CHAPTER_ID)));
        page.setPageId(cursor.getString(cursor.getColumnIndex(AbstractData.KEY_PAGE_ID)));
        page.setStatus(cursor.getInt(cursor.getColumnIndex(AbstractData.KEY_STATUS)));
        page.setIndex(cursor.getInt(cursor.getColumnIndex(AbstractData.KEY_INDEX)));
        page.setTaskId(cursor.getInt(cursor.getColumnIndex(AbstractData.KEY_TASK_ID)));
        page.setFilePath(cursor.getString(cursor.getColumnIndex(AbstractData.KEY_FILE_PATH)));
        page.setUrl(cursor.getString(cursor.getColumnIndex(AbstractData.KEY_URL)));
        page.setCreatedDate(DateHelper.convertStringToDate(cursor.getString(cursor.getColumnIndex(AbstractData.KEY_CREATED_DATE))));
        return page;
    }

    @Override
    public List<ComicChapterPage> search(String s) throws Exception {
        return toCollection(getDB().query(getTableName(),
                getAllColumns(),
                AbstractData.KEY_FILE_PATH + " like ? ",
                new String[]{
                        "%" + s + "%"
                },
                null,
                null,
                AbstractData.KEY_INDEX + " ASC",
                null
        ));
    }

    public void deleteByComicChapter(String chapterId) throws Exception {
        getDB().delete(getTableName(),
                AbstractData.KEY_CHAPTER_ID + "=?",
                new String[] {
                        chapterId
                });
    }

    public List<ComicChapterPage> listByComicChapter(String chapterId) throws Exception {
        Cursor c = cursorByComicChapter(chapterId);
        List<ComicChapterPage> pages = toCollection(c);
        c.close();
        return pages;
    }

    public Cursor cursorByComicChapter(String chapterId) throws Exception {
        return getDB().query(getTableName(),
                getAllColumns(),
                AbstractData.KEY_CHAPTER_ID + " = ? ",
                new String[]{
                        chapterId
                },
                null,
                null,
                AbstractData.KEY_INDEX + " ASC",
                null
        );
    }

    @Override
    public long insert(ComicChapterPage obj) throws Exception {
        Cursor cursor = getDB().query(getTableName(),
                getAllColumns(),
                AbstractData.KEY_PAGE_ID + " = ?",
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
        Cursor cursor =  getDB().query(getTableName(),
                getAllColumns(),
                AbstractData.KEY_TASK_ID + " = ? ",
                new String[] {
                        Long.toString(taskId)
                },
                null,
                null,
                null,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            ComicChapterPage page = toObject(cursor);
            cursor.close();
            return page;
        }
        return null;
    }
}
