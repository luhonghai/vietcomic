package com.halosolutions.vietcomic.comic;

import android.content.Context;

import com.halosolutions.vietcomic.util.SimpleAppLog;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by cmg on 11/08/15.
 */
public abstract class ComicService {

    private static final String SOURCE_VECHAI = "vechai.info";

    private final Context context;

    protected ComicService(Context context) {
        this.context = context;
    }

    protected Context getContext() {
        return this.context;
    }

    public static ComicService getService(Context context, ComicBook comicBook) {
        if (comicBook.getSource().equalsIgnoreCase(SOURCE_VECHAI)) {
            return new VechaiComicService(context);
        }
        return null;
    }

    public abstract List<ComicChapterPage> fetchChapterPage(final ComicChapter chapter) throws Exception;

    public abstract List<ComicChapter> fetchChapter(final ComicBook comicBook) throws Exception;

    public ComicChapter joinComicBook(ComicChapter chapter, List<ComicChapterPage> pages) throws Exception {
        if (pages == null) return null;
        Collections.sort(pages, new Comparator<ComicChapterPage>() {
            @Override
            public int compare(ComicChapterPage page1, ComicChapterPage page2) {
                return page1.getIndex() < page2.getIndex() ? -1 : (page1.getIndex() == page2.getIndex() ? 0 : 1);
            }
        });

        long start =System.currentTimeMillis();
        com.itextpdf.text.Document pdfDoc = new com.itextpdf.text.Document();
        pdfDoc.setMargins(0, 0, 0, 0);
        float documentWidth = pdfDoc.getPageSize().getWidth() - pdfDoc.leftMargin() - pdfDoc.rightMargin();
        float documentHeight = pdfDoc.getPageSize().getHeight() - pdfDoc.topMargin() - pdfDoc.bottomMargin();
        FileOutputStream fos = new FileOutputStream(chapter.getFilePath());
        PdfWriter.getInstance(pdfDoc, fos);
        boolean isComplete = true;
        try {
            SimpleAppLog.info("Open PDF document");
            pdfDoc.open();
            for (int i = 0; i < pages.size(); i++) {
                ComicChapterPage page = pages.get(i);
                if (page.getFilePath() == null || page.getFilePath().length() == 0) {
                    isComplete = false;
                    break;
                }
                File tmpImg = new File(page.getFilePath());
                if (!tmpImg.exists()) {
                    isComplete = false;
                    break;
                }
                Image pdfImg = Image.getInstance(tmpImg.getAbsolutePath());
                pdfImg.scaleToFit(documentWidth, documentHeight);
                pdfImg.setAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);
                SimpleAppLog.info("Add img to PDF doc. " + tmpImg);
                pdfDoc.add(pdfImg);
                if (i != pages.size() - 1) {
                    pdfDoc.newPage();
                }
            }
        } finally {
            try {
                pdfDoc.close();
                fos.close();
            } catch (Exception e) {}
            if (!isComplete) {
                File pdf = new File(chapter.getFilePath());
                if (pdf.exists()) {
                    try {
                        FileUtils.forceDelete(pdf);
                    } catch (Exception e) {}
                }
            }
        }
        long end = System.currentTimeMillis();
        SimpleAppLog.info("Join time: " + (end - start) + "ms");
        return chapter;
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

}
