package com.halosolutions.vietcomic.server.service;

import com.google.appengine.repackaged.com.google.gson.Gson;
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

/**
 * Created by cmg on 25/08/15.
 */
public class VietcomicBookService extends BookService {

    @Override
    protected String getKeyVersion() {
        return null;
    }

    @Override
    protected String getKeyBookData() {
        return null;
    }

    private List<String> pageURL = new ArrayList<String>();

    private Gson gson = new Gson();

    @Override
    public void load(boolean clearCache) {
        if (BOOK_DATA == null) {
            BOOK_DATA = new HashMap<String, ComicBook>();
            BOOK_HOT = new ArrayList<String>();
            BOOK_NEW = new ArrayList<String>();
            loadAllHot();
            loadAllNew();
            loadAllComic("http://v2.vietcomic.net/danh_sach_truyen?type=new&category=all&alpha=all&page=1&state=all&group=all");
            for (String bookId : BOOK_HOT) {
                if (BOOK_DATA.containsKey(bookId)) {
                    BOOK_DATA.get(bookId).setIsHot(true);
                }
            }
            for (String bookId : BOOK_NEW) {
                if (BOOK_DATA.containsKey(bookId)) {
                    BOOK_DATA.get(bookId).setIsNew(true);
                }
            }
        }
    }

    @Override
    protected String getRootUrl() {
        return "http://v2.vietcomic.net/";
    }

    private void loadAllHot() {

        try {
            Document document = Jsoup.
                    connect("http://v2.vietcomic.net/danh_sach_truyen?type=hot&category=all&alpha=all&page=1&state=all&group=all")
                    .timeout(FETCH_TIMEOUT).get();
            Elements comics = document.select("div.truyen-list div.list-truyen-item-wrap");
            if (comics != null && comics.size() > 0) {
                for (int i = 0; i < comics.size(); i++) {
                    Element element = comics.get(i);
                    String url = fixUrl(element.select("a").get(0).attr("href"));
                    String bookId = Hash.md5(url);
                    if (!BOOK_HOT.contains(bookId)) {
                        BOOK_HOT.add(bookId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAllNew() {

        try {
            Document document = Jsoup.
                    connect("http://v2.vietcomic.net/danh_sach_truyen?type=new&category=all&alpha=all&page=1&state=all&group=all")
                    .timeout(FETCH_TIMEOUT).get();
            Elements comics = document.select(".truyen-list .list-truyen-item-wrap");
            if (comics != null && comics.size() > 0) {
                for (int i = 0; i < comics.size(); i++) {
                    Element element = comics.get(i);
                    String url = fixUrl(element.select("a").get(0).attr("href"));
                    String bookId = Hash.md5(url);
                    if (!BOOK_NEW.contains(bookId)) {
                        BOOK_NEW.add(bookId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAllComic(String url) {
        if (url == null || url.length() == 0) return;
        url = fixUrl(url);
        if (!pageURL.contains(Hash.md5(url))) {
            log("Scan page url: " + url);
            pageURL.add(Hash.md5(url));
            try {
                Document document = Jsoup.
                        connect(url)
                        .timeout(FETCH_TIMEOUT).get();
                Elements comics = document.select("div.truyen-list div.list-truyen-item-wrap");
                if (comics != null && comics.size() > 0) {
                    log("Found " + comics.size() + " comics");
                    for (int i = 0; i < comics.size(); i++) {
                        Element element = comics.get(i);
                        Element aTitle = element.select("a").first();
                        String href = aTitle.attr("href").trim();
                        //log("Found tag "  + aTitle.tagName() + ". Href: " + href);
                        loadComicInfo(href);
                    }
                }
                // Found next comic page
                Elements pages = document.select(".phan-trang a.page");
                if (pages != null && pages.size() > 0) {
                    for (int i = 0; i < pages.size(); i++) {
                        Element element = pages.get(i);
                        loadAllComic(element.attr("href"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void loadComicInfo(String url) throws IOException {
        if (url != null && url.length() > 0) {
            url = fixUrl(url);
            log("Load comic info: " + url);
            String bookId = Hash.md5(url);
            if (!BOOK_DATA.containsKey(bookId)) {
                Document document = Jsoup.
                        connect(url)
                        .timeout(FETCH_TIMEOUT).get();
                ComicBook comicBook = new ComicBook();
                comicBook.setBookId(bookId);
                comicBook.setUrl(url);
                comicBook.setService("v2.vietcomic.net");

                comicBook.setThumbnail(getText(document, ".manga-info-top .manga-info-pic img", 0, "src"));

                Elements infoUrl = document.select(".manga-info-top ul.manga-info-text li");
                if (infoUrl != null && infoUrl.size() > 5) {
                    log("Info size: " + infoUrl.size());

                    comicBook.setName(getText(infoUrl.get(0), "h1", 0, ""));
                    comicBook.setOtherName(comicBook.getName());
                    comicBook.setAuthor(cutTextInfo(infoUrl.get(1)));
                    comicBook.setStatus(cutTextInfo(infoUrl.get(2)));
                    comicBook.setSource(cutTextInfo(infoUrl.get(4)));
                    String categories = cutTextInfo(infoUrl.get(6));
                    if (categories.contains(",")) {
                        String[] raw = categories.split(",");
                        List<String> list  =new ArrayList<String>();
                        for (String r : raw) {
                            String cat = r.toLowerCase().trim();
                            if (!list.contains(cat)) {
                                list.add(cat);
                            }
                        }
                        comicBook.setCategories(list);
                    }
                }
                comicBook.setRate(9.5f);
                log(gson.toJson(comicBook));
                if (comicBook.getName().length() > 0) {
                    BOOK_DATA.put(bookId, comicBook);
                }
            }
        }
    }

    private String cutTextInfo(Element element) {
        String text = element.text();
        if (text.length() > 0 && text.contains(":")) {
            text = text.substring(text.indexOf(":") + 1, text.length());
        }
        return text.trim();
    }
}
