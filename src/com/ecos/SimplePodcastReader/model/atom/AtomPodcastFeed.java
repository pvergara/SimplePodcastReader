package com.ecos.SimplePodcastReader.model.atom;

import com.ecos.SimplePodcastReader.model.generic.Podcast;
import com.ecos.SimplePodcastReader.model.generic.PodcastItem;

import java.util.Date;
import java.util.HashMap;

public class AtomPodcastFeed implements Podcast {

    private String title;
    private String id;
    private Date updated;
    private String logo = null;

    public HashMap<String, PodcastItem> Entries = new HashMap<String, PodcastItem>();
    private String link;
    private String subtitle;
    private String url;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public void setLogo(String logo) {
        this.logo = logo;

    }


    public String getSubtitle() {
        return subtitle;
    }

    public void setSubTitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String href) {
        link = href;

    }

    @Override
    public String getDescription() {
        return this.getSubtitle();
    }

    @Override
    public HashMap<String, PodcastItem> getItems() {
        return Entries;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }


}
