package com.halosolutions.vietcomic.server.service;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.halosolutions.vietcomic.server.data.ComicBook;
import org.apache.commons.codec.digest.DigestUtils;
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

    private static final Object lock = new Object();

    public static void load(boolean clearCache) {
        synchronized (lock) {
            MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
            if (clearCache) {
                BOOK_DATA = null;
                syncCache.clearAll();
            }
            if (BOOK_DATA == null || BOOK_DATA.size() == 0) {
                BOOK_DATA = new HashMap<String, ComicBook>();
                Gson gson = new Gson();
                for(int i = 0; i <= 91; i++) {
                    String key;
                    if (i<=9) {
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
                        while(getComic(comicBooks,key, index)) {
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
            if (clearCache) {
                BOOK_DATA_JSON = null;
            }
        }
    }

    public static String getBookDataJson() {
        load(false);
        if (BOOK_DATA_JSON == null || BOOK_DATA_JSON.length() == 0) {
            synchronized (lock) {
                if (BOOK_DATA != null && BOOK_DATA.size() > 0) {
                    Gson gson = new Gson();
                    BOOK_DATA_JSON = gson.toJson(BOOK_DATA.values());
                }
            }
        }
        return BOOK_DATA_JSON;
    }

    private static boolean getComic(final List<ComicBook> comicBooks, final String c, final int index) {
        try {
            Document doc = Jsoup.connect("http://vechai.info/danh-sach.tall.p" + index + ".json?fc=" + c).get();
            boolean noComic = false;
            Elements eleText = doc.select("#mainStory .NewsList li");
            if (eleText != null && eleText.size() > 0) {
                String text = eleText.get(0).text().trim();
                noComic = text.equalsIgnoreCase("Chưa có truyện !");
            }
            if (!noComic && eleText != null && eleText.size() > 0) {
                for (int i = 0; i < eleText.size(); i++) {
                    Element ele = eleText.get(0);
                    Element firstA = ele.child(0);
                    if (firstA.tagName().equalsIgnoreCase("a")) {
                        String href = firstA.attr("href");
                        if (!(href.startsWith("http") || href.startsWith("https"))) {
                            href = "http://vechai.info/" + href;
                        }
                        ComicBook comicBook = new ComicBook();
                        comicBook.setId(DigestUtils.md5Hex(href));
                        comicBook.setUrl(href);
                        if (!comicBooks.contains(comicBook)) {
                            if (loadComicBookInfo(comicBook)) {
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
            comicBook.setName(doc.select("#mainStory .TitleH2").get(0).text());
            comicBook.setRate(Float.parseFloat(doc.select("#mainStory .VoteScore").get(0).text()));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
