package com.halosolutions.vietcomic.server.service;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.halosolutions.vietcomic.server.Hash;
import com.halosolutions.vietcomic.server.data.ComicBook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cmg on 12/08/15.
 */
public abstract class BookService {

    protected static final int FETCH_TIMEOUT = 2 * 60 * 1000;

    protected   List<String> BOOK_HOT;

    protected  List<String> BOOK_NEW;

    protected  Map<String, ComicBook> BOOK_DATA;

    protected  String BOOK_DATA_JSON;

    protected  int DATA_VERSION = -1;

    protected final Object lock = new Object();

    protected abstract String getKeyVersion();

    protected abstract String getKeyBookData();

    public abstract void load(boolean clearCache);

    public Map<String, ComicBook> getBookData() {
        return BOOK_DATA;
    }

    protected String getText(final Document doc, final String selector, final int index, final String attr) {
        Elements eles = doc.select(selector);
        if (eles.size() > index) {
            if (attr != null && attr.length() > 0) {
                return eles.get(index).attr(attr).trim();
            } else {
                return eles.get(index).text().trim();
            }
        }
        return "";
    }

    protected String getText(final Element element, final String selector, final int index, final String attr) {
        Elements eles = element.select(selector);
        if (eles.size() > index) {
            if (attr != null && attr.length() > 0) {
                return eles.get(index).attr(attr).trim();
            } else {
                return eles.get(index).text().trim();
            }
        }
        return "";
    }

    protected void removeElements(final Document doc, String selector) {
        Elements elements = doc.select(selector);
        if (!elements.isEmpty()) {
            for (Element ele : elements) {
                ele.remove();
            }
        }
    }

    protected void log(String log) {
        System.out.println(log);
    }

    protected abstract String getRootUrl();

    protected String fixUrl(String url) {
        if (!(url.startsWith("http") || url.startsWith("https"))) {
            url = getRootUrl() + url;
        }
        return url;
    }
}
