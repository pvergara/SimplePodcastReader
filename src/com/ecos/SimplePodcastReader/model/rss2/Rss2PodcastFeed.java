package com.ecos.SimplePodcastReader.model.rss2;

import com.ecos.SimplePodcastReader.model.generic.Podcast;
import com.ecos.SimplePodcastReader.model.generic.PodcastItem;

import java.util.HashMap;

public class Rss2PodcastFeed implements Podcast {
    private String title = null;
    private String link = null;
    private String description = null;

    public HashMap<String, PodcastItem> Items = new HashMap<String, PodcastItem>();
    private String url;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String toString() {
        return
                "Title: " + this.getTitle() + "\n" +
                        "Link: " + this.getLink() + "\n" +
                        "Descrition: " + this.getDescription();
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public HashMap<String, PodcastItem> getItems() {
        return Items;
    }


    @Override
    public String getUrl() {
        return url;
    }

}
