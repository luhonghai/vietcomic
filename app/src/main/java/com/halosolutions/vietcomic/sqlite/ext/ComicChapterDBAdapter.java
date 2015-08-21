package com.halosolutions.vietcomic.sqlite.ext;

import android.content.Context;
import android.database.Cursor;

import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.comic.ComicChapter;
import com.halosolutions.vietcomic.sqlite.AbstractData;
import com.halosolutions.vietcomic.sqlite.DBAdapter;
import com.halosolutions.vietcomic.util.DateHelper;

import java.util.List;

/**
 * Created by cmg on 19/08/15.
 */
public class ComicChapterDBAdapter extends DBAdapter<ComicChapter> {

    public ComicChapterDBAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public Cursor getAll() throws Exception {
        return getAll(AbstractData.KEY_INDEX + " ASC");
    }

    @Override
    public String getTableName() {
        return AbstractData.TABLE_COMIC_CHAPTER;
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {
                AbstractData.KEY_ROW_ID,
                AbstractData.KEY_BOOK_ID,
                AbstractData.KEY_CHAPTER_ID,
                AbstractData.KEY_URL,
                AbstractData.KEY_NAME,
                AbstractData.KEY_PUBLISH_DATE,
                AbstractData.KEY_FILE_PATH,
                AbstractData.KEY_STATUS,
                AbstractData.KEY_INDEX,
                AbstractData.KEY_IMAGE_COUNT,
                AbstractData.KEY_COMPLETED_COUNT,
                AbstractData.KEY_CREATED_DATE
        };
    }

    @Override
    public ComicChapter toObject(Cursor cursor) {
        ComicChapter chapter = new ComicChapter();
        chapter.setId(cursor.getLong(cursor.getColumnIndex(AbstractData.KEY_ROW_ID)));
        chapter.setName(cursor.getString(cursor.getColumnIndex(AbstractData.KEY_NAME)));
        chapter.setBookId(cursor.getString(cursor.getColumnIndex(AbstractData.KEY_BOOK_ID)));
        chapter.setChapterId(cursor.getString(cursor.getColumnIndex(AbstractData.KEY_CHAPTER_ID)));
        chapter.setStatus(cursor.getInt(cursor.getColumnIndex(AbstractData.KEY_STATUS)));
        chapter.setIndex(cursor.getInt(cursor.getColumnIndex(AbstractData.KEY_INDEX)));
        chapter.setFilePath(cursor.getString(cursor.getColumnIndex(AbstractData.KEY_FILE_PATH)));
        chapter.setUrl(cursor.getString(cursor.getColumnIndex(AbstractData.KEY_URL)));
        chapter.setImageCount(cursor.getInt(cursor.getColumnIndex(AbstractData.KEY_IMAGE_COUNT)));
        chapter.setCompletedCount(cursor.getInt(cursor.getColumnIndex(AbstractData.KEY_COMPLETED_COUNT)));
        chapter.setPublishDate(DateHelper.convertStringToDate(cursor.getString(cursor.getColumnIndex(AbstractData.KEY_PUBLISH_DATE))));
        chapter.setCreatedDate(DateHelper.convertStringToDate(cursor.getString(cursor.getColumnIndex(AbstractData.KEY_CREATED_DATE))));
        return chapter;
    }

    @Override
    public List<ComicChapter> search(String s) throws Exception {
        return toCollection(getDB().query(getTableName(),
                getAllColumns(),
                AbstractData.KEY_NAME + " like ? ",
                new String[]{
                        "%" + s + "%"
                },
                null,
                null,
                AbstractData.KEY_INDEX + " ASC",
                null
        ));
    }

    public Cursor listByComic(ComicBook book) {
        return getDB().query(getTableName(),
                getAllColumns(),
                AbstractData.KEY_BOOK_ID + " = ? ",
                new String[]{
                        book.getBookId()
                },
                null,
                null,
                AbstractData.KEY_INDEX + " ASC",
                null
        );
    }

    public Cursor listByStatus(Integer[] status) {
        String selection = "";
        String[] params = new String[status.length];
        for (int i = 0; i < status.length; i++) {
            params[i] = Integer.toString(status[i]);
            selection += (AbstractData.KEY_STATUS + " = ?");
            if (i != status.length - 1) {
                selection += " or ";
            }
        }

        return getDB().query(getTableName(),
                getAllColumns(),
                selection,
                params,
                null,
                null,
                AbstractData.KEY_STATUS + " ASC",
                null
        );
    }

    public ComicChapter getByChapterId(String chapterId) {
        Cursor cursor =  getDB().query(getTableName(),
                getAllColumns(),
                AbstractData.KEY_CHAPTER_ID + " = ? ",
                new String[] {
                        chapterId
                },
                null,
                null,
                null,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            ComicChapter chapter = toObject(cursor);
            cursor.close();
            return chapter;
        }
        return null;
    }
}
