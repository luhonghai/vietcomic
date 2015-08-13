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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cmg on 12/08/15.
 */
public class BookManager {

    private static Map<String, ComicBook> BOOK_DATA;

    private static String BOOK_DATA_JSON;

    private static final String KEY_BOOK_DATA = "book_data";

    private static final String KEY_VERSION = "version";

    public static int DATA_VERSION = -1;

    private static final Object lock = new Object();

    public static void load(boolean clearCache) {
        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        int latestVersion = 0;
        Key vKey = KeyFactory.createKey(KEY_VERSION, KEY_VERSION);
        if (clearCache) {
            if (DATA_VERSION > 0) {
                latestVersion = DATA_VERSION;
            } else {
                if (syncCache.contains(KEY_VERSION)) {
                    latestVersion = Integer.parseInt(syncCache.get(KEY_VERSION).toString());
                } else {
                    try {
                        Entity eVersion = datastore.get(vKey);
                        latestVersion = Integer.parseInt(eVersion.getProperty(KEY_VERSION).toString());
                    } catch (EntityNotFoundException e) {
                    }
                }
            }
            System.out.println("Latest version: " + latestVersion);
            BOOK_DATA = null;
            syncCache.clearAll();
        }
        if (BOOK_DATA == null || BOOK_DATA.size() == 0) {
            synchronized (lock) {
                BOOK_DATA = new HashMap<String, ComicBook>();
                Gson gson = new Gson();
                for (int i = 0; i <= 90; i++) {
                    String key;
                    if (i <= 9) {
                        key = Integer.toString(i);
                    } else if (i >= 65) {
                        key = Character.toString((char) i);
                    } else {
                        continue;
                    }
                    List<ComicBook> comicBooks;
                    if (syncCache.contains(key)) {
                        comicBooks = gson.fromJson(syncCache.get(key).toString(), new TypeToken<List<ComicBook>>() {
                        }.getType());
                    } else {
                        comicBooks = new ArrayList<ComicBook>();
                        int index = 1;
                        while (getComic(comicBooks, key, index)) {
                            index++;
                        }
                        syncCache.put(key, gson.toJson(comicBooks));
                    }
                    if (comicBooks != null && comicBooks.size() > 0) {
                        for (ComicBook b : comicBooks) {
                            BOOK_DATA.put(b.getId(), b);
                        }
                    }
                }
                System.out.println("Comic book size: " + BOOK_DATA.size());
            }
        }
        if (clearCache) {
            latestVersion++;
            DATA_VERSION = latestVersion;
            syncCache.put(KEY_VERSION, latestVersion);
            Entity eVersion = new Entity(vKey);
            eVersion.setProperty(KEY_VERSION, latestVersion);
            datastore.put(eVersion);
            BOOK_DATA_JSON = null;
        }

    }

    public static String getBookDataJson() {
        if (BOOK_DATA_JSON == null || BOOK_DATA_JSON.length() == 0) {
            load(false);
            if (BOOK_DATA != null && BOOK_DATA.size() > 0) {
                Gson gson = new Gson();
                BOOK_DATA_JSON = gson.toJson(BOOK_DATA.values());
            }
        }
        return BOOK_DATA_JSON;
    }

    private static boolean getComic(final List<ComicBook> comicBooks, final String c, final int index) {
        try {
            System.out.println("Char: " + c + ". Index: " + index);
            Document doc = Jsoup.connect("http://vechai.info/danh-sach.tall.p" + index + ".json?fc=" + c).get();
            boolean noComic = false;
            Elements eleText = doc.select("#comic-list li");
            System.out.println("Comic size: " + eleText.size());
            if (eleText.size() > 0) {
                String text = eleText.get(0).text().trim();
                System.out.println("First text: " + text);
                noComic = text.equalsIgnoreCase("Chưa có truyện !");
            }
            if (!noComic && eleText.size() > 0) {
                for (int i = 0; i < eleText.size(); i++) {
                    Element ele = eleText.get(i);
                    Element firstA = ele.child(0);
                    if (firstA.tagName().equalsIgnoreCase("a")) {
                        String href = firstA.attr("href");
                        if (!(href.startsWith("http") || href.startsWith("https"))) {
                            href = "http://vechai.info/" + href;
                        }
                        ComicBook comicBook = new ComicBook();
                        comicBook.setId(Hash.md5(href));
                        comicBook.setUrl(href);
                        if (!BOOK_DATA.containsKey(comicBook.getId()) && !comicBooks.contains(comicBook)) {
                            if (loadComicBookInfo(comicBook)) {
                                System.out.println("Found comic: " + comicBook.getName() + ". URL: " + comicBook.getUrl());
                                comicBooks.add(comicBook);
                            }
                        }
                    }
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean loadComicBookInfo(final ComicBook comicBook) {
        try {
            Document doc = Jsoup.connect(comicBook.getUrl()).get();
            comicBook.setName(getText(doc, ".BoxContent .IntroText .TitleH2",0,""));
            comicBook.setRate(Float.parseFloat(getText(doc, "#mainStory .VoteScore", 0, "")));
            comicBook.setOtherName(getText(doc, ".MoreInfo dl dd", 0, ""));
            String rawCategories =getText(doc, ".MoreInfo dl dd", 1, "");
            if (rawCategories.length() > 0) {
                String[] categories = rawCategories.split(",");
                if (categories.length > 0) {
                    List<String> listCategories = new ArrayList<String>();
                    for (String c : categories) {
                        if (!listCategories.contains(c.trim().toLowerCase())) {
                            listCategories.add(c.trim().toLowerCase());
                        }
                    }
                    comicBook.setCategories(listCategories);
                }
            }
            comicBook.setAuthor(getText(doc, ".MoreInfo dl dd", 2, ""));
            comicBook.setStatus(getText(doc, ".MoreInfo dl dd", 3, ""));
            comicBook.setThumbnail(getText(doc, ".BoxContent img.Thumb", 0, "src"));
            //removeElements(doc, ".BoxContent .IntroText .TitleH2");
            //comicBook.setDescription(getText(doc, ".BoxContent .IntroText", 0, ""));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String getText(final Document doc, final String selector, final int index, final String attr) {
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

    private static void removeElements(final Document doc, String selector) {
        Elements elements = doc.select(selector);
        if (!elements.isEmpty()) {
            for (Element ele : elements) {
                ele.remove();
            }
        }
    }
}
