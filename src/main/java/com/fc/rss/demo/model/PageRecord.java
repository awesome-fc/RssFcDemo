package com.fc.rss.demo.model;

public class PageRecord {

    private String url;

    private String filename;

    public PageRecord() {

    }

    public PageRecord(String url, String filename) {
        this.url = url;
        this.filename = filename;
    }

    public String getUrl() {
        return url;
    }

    public String getFilename() {
        return filename;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "PageRecord{" +
                "url='" + url + '\'' +
                ", filename='" + filename + '\'' +
                '}';
    }
}
