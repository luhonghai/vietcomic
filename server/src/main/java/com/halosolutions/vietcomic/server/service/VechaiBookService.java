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
public class VechaiBookService extends BookService {

    @Override
    protected String getKeyVersion() {
        return "vechai_key_version";
    }

    @Override
    protected String getKeyBookData() {
        return "vechai_key_data";
    }

    @Override
    public void load(boolean clearCache) {
        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        int latestVersion = 0;
        Key vKey = KeyFactory.createKey(getKeyVersion(), getKeyVersion());
        if (clearCache) {
            if (DATA_VERSION > 0) {
                latestVersion = DATA_VERSION;
            } else {
                if (syncCache.contains(getKeyVersion())) {
                    latestVersion = Integer.parseInt(syncCache.get(getKeyVersion()).toString());
                } else {
                    try {
                        Entity eVersion = datastore.get(vKey);
                        latestVersion = Integer.parseInt(eVersion.getProperty(getKeyVersion()).toString());
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
                loadHotAndNewComic();
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
                            if (BOOK_HOT.contains(b.getBookId())) {
                                b.setIsHot(true);
                            }
                            if (BOOK_NEW.contains(b.getBookId())) {
                                b.setIsNew(true);
                            }
                            BOOK_DATA.put(b.getBookId(), b);
                        }
                    }
                }
                System.out.println("Comic book size: " + BOOK_DATA.size());
            }
        }
        if (clearCache) {
            latestVersion++;
            DATA_VERSION = latestVersion;
            syncCache.put(getKeyVersion(), latestVersion);
            Entity eVersion = new Entity(vKey);
            eVersion.setProperty(getKeyVersion(), latestVersion);
            datastore.put(eVersion);
            BOOK_DATA_JSON = null;
        }

    }

    @Override
    protected String getRootUrl() {
        return "http://vechai.info/";
    }

    private void loadHotAndNewComic() {
        BOOK_HOT = new ArrayList<String>();
        BOOK_NEW = new ArrayList<String>();
        try {
            Document doc = Jsoup.connect("http://vechai.info").timeout(FETCH_TIMEOUT).get();
            Elements elements = doc.select("#hotStory .NewList li");
            if (elements != null && elements.size() > 0) {
                for (int i = 0; i < elements.size() ; i++) {
                    Element element = elements.get(i);
                    Element firstA = element.select("a").get(0);
                    String href = fixUrl(firstA.attr("href"));
                    String bookId = Hash.md5(href);
                    if (!BOOK_HOT.contains(bookId)) {
                        System.out.println("Found hot comic: " + href);
                        BOOK_HOT.add(bookId);
                    }
                }
            }
            doc = Jsoup.connect("http://vechai.info").timeout(FETCH_TIMEOUT).get();
            elements = doc.select("#mainStory ul.NewsList li");
            if (elements != null && elements.size() > 0) {
                for (int i = 0; i < elements.size() ; i++) {
                    Element element = elements.get(i);
                    Element firstA = element.select("a").get(0);
                    String href = fixUrl(firstA.attr("href"));
                    String bookId = Hash.md5(href);
                    if (!BOOK_NEW.contains(bookId)) {
                        System.out.println("Found new comic: " + href);
                        BOOK_NEW.add(bookId);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean getComic(final List<ComicBook> comicBooks, final String c, final int index) {
        try {
            System.out.println("Char: " + c + ". Index: " + index);
            Document doc = Jsoup.connect("http://vechai.info/danh-sach.tall.p" + index + ".json?fc=" + c).timeout(FETCH_TIMEOUT).get();
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
                        String href = fixUrl(firstA.attr("href"));
                        ComicBook comicBook = new ComicBook();
                        comicBook.setBookId(Hash.md5(href));
                        comicBook.setUrl(href);
                        if (!BOOK_DATA.containsKey(comicBook.getBookId()) && !comicBooks.contains(comicBook)) {
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

    private boolean loadComicBookInfo(final ComicBook comicBook) {
        try {
            Document doc = Jsoup.connect(comicBook.getUrl()).timeout(FETCH_TIMEOUT).get();
            comicBook.setSource("vechai.info");
            comicBook.setService("vechai.info");
            comicBook.setName(getText(doc, ".BoxContent .IntroText .TitleH2",0,""));
            if (comicBook.getName().trim().length() == 0 || comicBook.getName().toLowerCase().contains("vechai.info")) {
                return false;
            }
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
            String thumbUrl = getText(doc, ".BoxContent img.Thumb", 0, "src");
            if (!thumbUrl.equalsIgnoreCase("http://cdn.vechai.info/images/noimage.png")) {
                comicBook.setThumbnail(thumbUrl);
            }
            //removeElements(doc, ".BoxContent .IntroText .TitleH2");
            //comicBook.setDescription(getText(doc, ".BoxContent .IntroText", 0, ""));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
