package com.halosolutions.vietcomic.comic;

/**
 * Created by cmg on 11/08/15.
 */
public class ComicChapter {

    private String url;

    private String pdfFile;

    private int imageCount;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(String pdfFile) {
        this.pdfFile = pdfFile;
    }

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }
}
