package com.halosolutions.mangaworld.sqlite.ext;

import android.content.Context;
import android.database.Cursor;

import com.halosolutions.mangaworld.comic.ComicBook;
import com.halosolutions.mangaworld.comic.ComicChapter;
import com.halosolutions.mangaworld.sqlite.AbstractData;
import com.halosolutions.mangaworld.sqlite.DBAdapter;
import com.halosolutions.mangaworld.util.DateHelper;

import java.util.List;
import java.util.Set;

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
                AbstractData.KEY_TIMESTAMP,
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
        chapter.setTimestamp(DateHelper.convertStringToDate(cursor.getString(cursor.getColumnIndex(AbstractData.KEY_TIMESTAMP))));
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

    public Cursor listByStatus(Integer[] status, Set<String> chapterIds, String limit) {
        String selection = "(";
        int statusLength = status.length;
        String[] params = new String[statusLength + ((chapterIds != null) ? chapterIds.size() : 0)];
        for (int i = 0; i < statusLength; i++) {
            params[i] = Integer.toString(status[i]);
            selection += (AbstractData.KEY_STATUS + " = ?");
            if (i != statusLength - 1) {
                selection += " or ";
            }
        }
        selection += ")";
        if (chapterIds != null && chapterIds.size() > 0) {
            selection += " and " + AbstractData.KEY_CHAPTER_ID + " not in (";
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

        return getDB().query(getTableName(),
                getAllColumns(),
                selection,
                params,
                null,
                null,
                AbstractData.KEY_STATUS + " ASC, " + AbstractData.KEY_TIMESTAMP + " ASC",
                limit
        );
    }

    public Cursor listByStatus(Integer[] status) {
        return listByStatus(status,null, null);
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
