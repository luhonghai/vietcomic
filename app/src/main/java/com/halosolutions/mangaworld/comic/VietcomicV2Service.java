package com.halosolutions.mangaworld.comic;

import android.content.Context;

import com.halosolutions.mangaworld.util.Hash;
import com.halosolutions.mangaworld.util.SimpleAppLog;
import com.halosolutions.mangaworld.util.StringHelper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cmg on 18/08/15.
 */
public class VietcomicV2Service extends ComicService {


    protected VietcomicV2Service(Context context) {
        super(context);
    }

    @Override
    public String getRootUrl() {
        return "http://v2.vietcomic.net/";
    }

    @Override
    public void fetchChapterPage(ComicChapter chapter, FetchChapterPageListener listener) throws Exception {
        long start =System.currentTimeMillis();
        SimpleAppLog.debug("Start fetch comic book chapter pages at " + chapter.getUrl());
        File tmpFile = null;
        try {
            tmpFile = new File(FileUtils.getTempDirectory(), Hash.md5(chapter.getUrl()));
            FileUtils.copyURLToFile(new URL(chapter.getUrl()), tmpFile);
            if (tmpFile.exists()) {
                List<String> lines = FileUtils.readLines(tmpFile, "UTF-8");
                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("data =")) {
                        SimpleAppLog.debug("Found data line: " + line);
                        String raw = line.substring("data =".length() + 1, line.length()).trim();
                        raw = raw.substring(1, raw.length() - 1);
                        SimpleAppLog.debug("Trim data line to: "+ raw);
                        String[] images = raw.split("\\|");
                        if (images.length > 0) {
                            for (int i = 0; i < images.length; i++) {
                                String url = fixUrl(images[i]);
                                ComicChapterPage page = new ComicChapterPage();
                                page.setBookId(chapter.getBookId());
                                page.setChapterId(chapter.getChapterId());
                                page.setPageId(Hash.md5(url));
                                page.setUrl(url);
                                page.setIndex(i);
                                if (listener != null)
                                    listener.onChapterPageFound(page);
                            }
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (tmpFile != null && tmpFile.exists()) {
                try {
                    FileUtils.forceDelete(tmpFile);
                } catch (Exception e) {}
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
        removeElements(doc, ".manga-info-content .noidung");
        if (listener != null)
            listener.onDescriptionFound(getText(doc, ".manga-info-content", 0, ""));
        SimpleAppLog.info("Found description: " + comicBook.getDescription());
        Elements elements = doc.select(".manga-info-chapter .chapter-list .row");
        List<String> keys = new ArrayList<String>();
        if (elements != null) {
            try {
                int count = 0;
                for (int i = elements.size() - 1; i >= 0; i--) {
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

                    ComicChapter chapter = new ComicChapter();
                    chapter.setUrl(url);
                    chapter.setIndex(count);
                    chapter.setBookId(comicBook.getBookId());
                    chapter.setChapterId(Hash.md5(url));
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
        Document doc = Jsoup.
                connect("http://v2.vietcomic.net/danh_sach_truyen?type=hot&category=all&alpha=all&page=1&state=all&group=all")
                .userAgent(USER_AGENT)
                .timeout(REQUEST_TIMEOUT).get();
        Elements comics = doc.select("div.truyen-list div.list-truyen-item-wrap");
        if (comics != null && comics.size() > 0) {
            for (int i = 0; i < comics.size(); i++) {
                Element element = comics.get(i);
                String url = fixUrl(element.select("a").get(0).attr("href"));
                String bookId = Hash.md5(url);
                listener.onHotComicFound(bookId);
            }
        }

        doc = Jsoup.
                connect("http://v2.vietcomic.net/danh_sach_truyen?type=new&category=all&alpha=all&page=1&state=all&group=all")
                .userAgent(USER_AGENT)
                .timeout(REQUEST_TIMEOUT).get();
        comics = doc.select("div.truyen-list div.list-truyen-item-wrap");
        if (comics != null && comics.size() > 0) {
            for (int i = 0; i < comics.size(); i++) {
                Element element = comics.get(i);
                String url = fixUrl(element.select("a").get(0).attr("href"));
                String bookId = Hash.md5(url);
                listener.onNewComicFound(bookId);
            }
        }

    }
}
