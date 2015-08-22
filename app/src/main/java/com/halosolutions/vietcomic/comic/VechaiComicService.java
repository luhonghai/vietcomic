package com.halosolutions.vietcomic.comic;

import android.content.Context;

import com.halosolutions.vietcomic.util.Hash;
import com.halosolutions.vietcomic.util.SimpleAppLog;
import com.halosolutions.vietcomic.util.StringHelper;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by cmg on 18/08/15.
 */
public class VechaiComicService extends ComicService {

    private static final String VECHAI_ROOT_URL = "http://vechai.info/";

    private static final String SELECTOR_BOOK_CHAPTER_PAGE_IMAGE = "#contentChapter img";

    private static final String SELECTOR_BOOK_TITLE = ".BoxContent .IntroText .TitleH2";

    private static final String SELECTOR_BOOK_INFO_TEXT = ".BoxContent .IntroText";

    private SimpleDateFormat sdfPublishDate = new SimpleDateFormat("dd/MM/yyyy");

    protected VechaiComicService(Context context) {
        super(context);
    }

    @Override
    public void fetchChapterPage(ComicChapter chapter, FetchChapterPageListener listener) throws Exception {
        long start =System.currentTimeMillis();
        SimpleAppLog.debug("Start fetch comic book chapter pages at " + chapter.getUrl());
        Document doc = Jsoup.connect(chapter.getUrl())
                .userAgent(USER_AGENT)
                .timeout(REQUEST_TIMEOUT)
                .get();
        removeElements(doc, "#advInPage");
        Elements images = doc.select(SELECTOR_BOOK_CHAPTER_PAGE_IMAGE);
        if (images != null) {
            SimpleAppLog.debug("Found images");
            try {
                int count = 0;
                for (int i = 0; i < images.size(); i++) {
                    Element img = images.get(i);
                    String url = img.attr("src");
                    if (!url.contains("http://adx.kul.vn")) {
                        if (!(url.startsWith("http") || url.startsWith("https"))) {
                            url = VECHAI_ROOT_URL + url;
                        }
                        ComicChapterPage page = new ComicChapterPage();
                        page.setBookId(chapter.getBookId());
                        page.setChapterId(chapter.getChapterId());
                        page.setPageId(Hash.md5(url));
                        page.setUrl(url);
                        page.setIndex(count);
                        if (listener != null)
                            listener.onChapterPageFound(page);
                        count ++;
                    }
                }
            } finally {

            }
        }
        long end = System.currentTimeMillis();
        SimpleAppLog.debug("Fetch time: " + (end - start) + "ms");
    }

    @Override
    public void fetchChapter(final ComicBook comicBook, FetchChapterListener listener) throws Exception {
        long start =System.currentTimeMillis();
        SimpleAppLog.info("Start fetch comic book chapter at " + comicBook.getUrl());
        Document doc = Jsoup
                .connect(comicBook.getUrl())
                .userAgent(USER_AGENT)
                .timeout(REQUEST_TIMEOUT).get();
        removeElements(doc, SELECTOR_BOOK_TITLE);
        //comicBook.setDescription(getText(doc, SELECTOR_BOOK_INFO_TEXT, 0, ""));
        if (listener != null)
            listener.onDescriptionFound(getText(doc, SELECTOR_BOOK_INFO_TEXT, 0, ""));
        SimpleAppLog.info("Found description: " + comicBook.getDescription());
        Elements elements = doc.select("#chapterList ul.accordion > li ul > li");
        List<String> keys = new ArrayList<String>();
        if (elements != null) {
            try {
                int count = 0;
                for (int i = 0; i < elements.size(); i++) {
                    Element element = elements.get(i);
                    String url = getText(element, "a", 0, "href");
                    if (!(url.startsWith("http") || url.startsWith("https"))) {
                        url = VECHAI_ROOT_URL + url;
                    }
                    String name = getText(element, "a", 0, "");
                    name = name.trim();
                    do  {
                        while (name.length() > 2) {
                            if (StringUtils.isAlphanumeric(Character.toString(name.toCharArray()[0]))) {
                                break;
                            } else {
                                name = name.substring(1, name.length()).trim();
                            }
                        }
                        if (StringHelper.removeAccent(name.toLowerCase())
                                .startsWith(StringHelper.removeAccent(comicBook.getOtherName().trim().toLowerCase()))) {
                            name = name.substring(comicBook.getOtherName().trim().length(), name.length()).trim();
                        }
                        while (name.length() > 2) {
                            if (StringUtils.isAlphanumeric(Character.toString(name.toCharArray()[0]))) {
                                break;
                            } else {
                                name = name.substring(1, name.length()).trim();
                            }
                        }
                        if (StringHelper.removeAccent(name.toLowerCase())
                                .startsWith(StringHelper.removeAccent(comicBook.getName().trim().toLowerCase()))) {
                            name = name.substring(comicBook.getName().trim().length(), name.length()).trim();
                        }
                    } while (StringHelper.removeAccent(name.toLowerCase())
                            .startsWith(StringHelper.removeAccent(comicBook.getOtherName().trim().toLowerCase()))
                            || StringHelper.removeAccent(name.toLowerCase())
                            .startsWith(StringHelper.removeAccent(comicBook.getName().trim().toLowerCase())));
                    if (name.toLowerCase().contains("chap")) {
                        name = name.substring(name.toLowerCase().lastIndexOf("chap"), name.length()).trim();
                    }

                    String date = getText(element, "span.Date", 0, "");
                    Date pDate = null;
                    try {
                        pDate = sdfPublishDate.parse(date);
                    } catch (Exception e) {
                        SimpleAppLog.error("Could not parse date: " + date,e);
                    }
                    ComicChapter chapter = new ComicChapter();
                    chapter.setUrl(url);
                    chapter.setIndex(count);
                    chapter.setBookId(comicBook.getBookId());
                    chapter.setChapterId(Hash.md5(url));
                    chapter.setPublishDate(pDate);
                    chapter.setName(name);
                    if (!keys.contains(chapter.getChapterId())) {
                        keys.add(chapter.getChapterId());
                        if (listener != null)
                            listener.onChapterFound(chapter);
                        SimpleAppLog.debug("Found chapter: " + chapter.getName() + ". URL: " + chapter.getUrl() + ". Index: " + count);
                        count++;
                    }
                }
            } finally {

            }
        }
        long end = System.currentTimeMillis();
        SimpleAppLog.info("Fetch time: " + (end - start) + "ms");
    }
}
