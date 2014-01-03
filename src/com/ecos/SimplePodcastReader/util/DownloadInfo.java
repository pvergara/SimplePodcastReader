package com.ecos.SimplePodcastReader.util;

import com.ecos.SimplePodcastReader.model.generic.PodcastItem;

import java.net.URL;

public class DownloadInfo {

    private URL url;
    private String filePath;
    private int length;
    private PodcastItem item;

    public DownloadInfo(URL url, String filePath, int length, PodcastItem previousItem) {
        this.setUrl(url);
        this.setFilePath(filePath);
        this.setLength(length);
        this.setItem(previousItem);
    }

    private void setUrl(URL url) {
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }

    private void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    private void setLength(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }


    private void setItem(PodcastItem item) {
        this.item = item;

    }


}
