package com.halosolutions.vietcomic.fragment.detail;

import com.google.gson.Gson;
import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.fragment.ComicFragment;

/**
 * Created by cmg on 19/08/15.
 */
public abstract class DetailComicFragment extends ComicFragment {

    protected ComicBook getComicBook() {
        Gson gson = new Gson();
        return gson.fromJson(getArguments().getString(ComicBook.class.getName()), ComicBook.class);
    }
}
