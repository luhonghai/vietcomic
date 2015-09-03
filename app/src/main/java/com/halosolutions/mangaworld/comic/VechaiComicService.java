package com.halosolutions.mangaworld.comic;

import android.content.Context;

import com.halosolutions.mangaworld.util.Hash;
import com.halosolutions.mangaworld.util.SimpleAppLog;
import com.halosolutions.mangaworld.util.StringHelper;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by cmg on 18/08/15.
 */
public class VechaiComicService extends ComicService {

    private static final String VECHAI_ROOT_URL = "http://vechai.info/";

    private static final String SELECTOR_BOOK_CHAPTER_PAGE_IMAGE = "img";

    private static final String SELECTOR_BOOK_TITLE = ".BoxContent .IntroText .TitleH2";

    private static final String SELECTOR_BOOK_INFO_TEXT = ".BoxContent .IntroText";

    private SimpleDateFormat sdfPublishDate = new SimpleDateFormat("dd/MM/yyyy");

    protected VechaiComicService(Context context) {
        super(context);
    }

    @Override
    public String getRootUrl() {
        return VECHAI_ROOT_URL;
    }

    @Override
    public void fetchChapterPage(ComicChapter chapter, FetchChapterPageListener listener) throws Exception {
        SimpleAppLog.debug("Start fetch comic book chapter pages at " + chapter.getUrl());
        long start =System.currentTimeMillis();
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, REQUEST_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParameters, REQUEST_TIMEOUT);
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(chapter.getUrl());
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        InputStream in = null;
        StringBuilder result = new StringBuilder();
        try
        {
            in = new BufferedInputStream(entity.getContent());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            result.append("<div>");
            String line;
            int state = 0;
            while((line = reader.readLine()) != null) {
                if (line.contains("<div class=\"Content\">") && state == 0) {
                    state = 1;
                } else if (line.contains("</noscript>") && state == 1) {
                    state = 2;
                } else if (state == 2 && line.contains("<img")) {
                    state = 3;
                } else if (state == 3 && line.contains("<div class=\"Center ChapterNav\">")) {
                    break;
                }
                if (state == 3) {
                    result.append(line);
                }

            }
        } finally
        {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {}
            }
        }

        String html = result.toString();
        SimpleAppLog.debug("Found html source: " + html);
        Document doc = Jsoup.parse(html);
        Elements images = doc.select(SELECTOR_BOOK_CHAPTER_PAGE_IMAGE);
        if (images != null) {
            SimpleAppLog.debug("Found images");
            try {
                int count = 0;
                for (int i = 0; i < images.size(); i++) {
                    Element img = images.get(i);
                    String url = fixUrl(img.attr("src"));
                    if (!url.contains("http://adx.kul.vn")) {
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
                    String url = fixUrl(getText(element, "a", 0, "href"));
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

    @Override
    public void fetchHotAndNewComic(FetchHotAndNewListener listener) throws Exception {
        Document doc = Jsoup.connect("http://vechai.info")
                .userAgent(USER_AGENT)
                .timeout(REQUEST_TIMEOUT).get();
        Elements elements = doc.select("#hotStory .NewList li");
        if (elements != null && elements.size() > 0) {
            for (int i = 0; i < elements.size() ; i++) {
                Element element = elements.get(i);
                Element firstA = element.select("a").get(0);
                String href = firstA.attr("href");
                if (!(href.startsWith("http") || href.startsWith("https"))) {
                    href = "http://vechai.info/" + href;
                }
                String bookId = Hash.md5(href);
                listener.onHotComicFound(bookId);
            }
        }

        doc = Jsoup.connect("http://vechai.info/truyen-danh-cho-ban.html").userAgent(USER_AGENT)
                .timeout(REQUEST_TIMEOUT).get();
        elements = doc.select("#mostStory ul.NewsList li");
        if (elements != null && elements.size() > 0) {
            for (int i = 0; i < elements.size() ; i++) {
                Element element = elements.get(i);
                Element firstA = element.select("a").get(0);
                String href = firstA.attr("href");
                if (!(href.startsWith("http") || href.startsWith("https"))) {
                    href = "http://vechai.info/" + href;
                }
                String bookId = Hash.md5(href);
                listener.onHotComicFound(bookId);
            }
        }

        doc = Jsoup.connect("http://vechai.info").userAgent(USER_AGENT)
                .timeout(REQUEST_TIMEOUT).get();
        elements = doc.select("#mainStory ul.NewsList li");
        if (elements != null && elements.size() > 0) {
            for (int i = 0; i < elements.size() ; i++) {
                Element element = elements.get(i);
                Element firstA = element.select("a").get(0);
                String href = firstA.attr("href");
                if (!(href.startsWith("http") || href.startsWith("https"))) {
                    href = "http://vechai.info/" + href;
                }
                String bookId = Hash.md5(href);
                listener.onNewComicFound(bookId);
            }
        }


    }
}
