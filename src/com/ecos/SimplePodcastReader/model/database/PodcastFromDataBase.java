
package com.ecos.SimplePodcastReader.model.database;

import com.ecos.SimplePodcastReader.model.generic.Podcast;
import com.ecos.SimplePodcastReader.model.generic.PodcastItem;

import java.util.HashMap;


public class PodcastFromDataBase implements Podcast {
    private String title;
    private String description;
    private String link;
    private HashMap<String, PodcastItem> items = new HashMap<String, PodcastItem>();
    private String url;

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String getLink() {
        return link;
    }


    @Override
    public String getUrl() {
        return url;
    }


    public void setDescription(String description) {
        this.description = description;

    }

    @Override
    public HashMap<String, PodcastItem> getItems() {
        return items;
    }

    public void addItem(String guid, PodcastItem item) {
        items.put(guid, item);
    }

    public void setUrl(String url) {
        this.url = url;
    }
}