package com.halosolutions.mangaworld.fragment.detail;

import com.google.gson.Gson;
import com.halosolutions.mangaworld.comic.ComicBook;
import com.halosolutions.mangaworld.fragment.ComicFragment;

/**
 * Created by cmg on 19/08/15.
 */
public abstract class DetailComicFragment extends ComicFragment {

    protected ComicBook getComicBook() {
        Gson gson = new Gson();
        return gson.fromJson(getArguments().getString(ComicBook.class.getName()), ComicBook.class);
    }
}
