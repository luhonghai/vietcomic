package com.halosolutions.mangaworld.comic;

import android.content.Context;

/**
 * Created by cmg on 25/08/15.
 */
public class DefaultComicService extends ComicService {

    protected DefaultComicService(Context context) {
        super(context);
    }

    @Override
    public String getRootUrl() {
        return null;
    }

    @Override
    public void fetchChapterPage(ComicChapter chapter, FetchChapterPageListener listener) throws Exception {

    }

    @Override
    public void fetchChapter(ComicBook comicBook, FetchChapterListener listener) throws Exception {

    }

    @Override
    public void fetchHotAndNewComic(FetchHotAndNewListener listener) throws Exception {

    }
}
