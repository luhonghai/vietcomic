package com.halosolutions;

import com.cmg.android.cmgpdf.AsyncTask;
import com.cmg.android.util.SimpleAppLog;
import com.cmg.mobile.shared.util.StringUtils;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

/**
 * Created by cmg on 11/08/15.
 */
public class BookService {

    public interface DownloadListener {
        public void onError(String message, Throwable e);
        public void onComplete(ComicBook book);
    }

    private static final String VECHAI_ROOT_URL = "http://vechai.info/";

    public static final String DEFAULT_CSS_SELECTOR = "#contentChapter img";

    private String cssSelector;


    public BookService() {
        cssSelector = DEFAULT_CSS_SELECTOR;
    }

    public BookService(String cssSelector) {
        this.cssSelector = cssSelector;
    }

    public void downloadAsync(final ComicBook book, final DownloadListener listener) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    listener.onComplete(downloadBook(book));
                } catch (Exception e) {
                    listener.onError("Could not download comic", e);
                }
                return null;
            }
        }.execute();
    }

    public ComicBook downloadBook(final ComicBook book) throws Exception {
        SimpleAppLog.info("Start download comic book at " + book.getUrl());
        Document doc = Jsoup.connect(book.getUrl()).get();
        Elements images = doc.select(cssSelector);
        if (images != null) {
            SimpleAppLog.info("Found images");
            com.itextpdf.text.Document pdfDoc = new com.itextpdf.text.Document();
            pdfDoc.setMargins(0, 0, 0, 0);
            float documentWidth = pdfDoc.getPageSize().getWidth() - pdfDoc.leftMargin() - pdfDoc.rightMargin();
            float documentHeight = pdfDoc.getPageSize().getHeight() - pdfDoc.topMargin() - pdfDoc.bottomMargin();

            FileOutputStream fos = new FileOutputStream(book.getPdfFile());
            PdfWriter.getInstance(pdfDoc, fos);
            try {
                SimpleAppLog.info("Open PDF document");
                pdfDoc.open();
                int count = 0;
                for (int i = 0; i < images.size(); i++) {
                    Element img = images.get(i);
                    String url = img.attr("src");
                    if (!url.contains("http://adx.kul.vn")) {
                        if (!(url.startsWith("http") || url.startsWith("https"))) {
                            url = VECHAI_ROOT_URL + url;
                        }
                        SimpleAppLog.info("Download img: " + url);
                        File tmpImg = new File(org.apache.commons.io.FileUtils.getTempDirectory(), StringUtils.md5(url));
                        if (!tmpImg.exists()) {
                            org.apache.commons.io.FileUtils.copyURLToFile(new URL(url), tmpImg);
                        }
                        Image pdfImg = Image.getInstance(tmpImg.getAbsolutePath());
                        pdfImg.scaleToFit(documentWidth, documentHeight);
                        pdfImg.setAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);
                        SimpleAppLog.info("Add img to PDF doc. " + tmpImg);
                        pdfDoc.add(pdfImg);
                        if (i != images.size() - 1) {
                            pdfDoc.newPage();
                        }
                        try {
                            FileUtils.forceDelete(tmpImg);
                        } catch (Exception e) {

                        }
                        count ++;
                    }
                }
                book.setImageCount(count);
            } finally {
                try {
                    pdfDoc.close();
                    fos.close();
                } catch (Exception e) {}

            }
        }
        return book;
    }
}
