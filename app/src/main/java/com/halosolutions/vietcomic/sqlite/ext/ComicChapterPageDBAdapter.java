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
                AbstractData.KEY_CREATED_DATE
        };
    }

    @Override
    public ComicChapterPage toObject(Cursor cursor) {
        ComicChapterPage page = new ComicChapterPage();
        page.setId(cursor.getLong(cursor.getColumnIndex(AbstractData.KEY_ROW_ID)));
        page.setBookId(cursor.getString(cursor.getColumnIndex(AbstractData.KEY_BOOK_ID)));
        page.setChapterId(cursor.getString(cursor.getColumnIndex(AbstractData.KEY_CHAPTER_ID)));
        page.setStatus(cursor.getInt(cursor.getColumnIndex(AbstractData.KEY_STATUS)));
        page.setIndex(cursor.getInt(cursor.getColumnIndex(AbstractData.KEY_INDEX)));
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
}
