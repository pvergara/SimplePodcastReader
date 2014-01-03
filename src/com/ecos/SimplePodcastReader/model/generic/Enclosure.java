package com.ecos.SimplePodcastReader.model.generic;

import java.net.URL;

public class Enclosure {

    private URL url = null;
    private int length = 0;
    private String type = null;

    public void setUrl(URL url) {
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }


}
