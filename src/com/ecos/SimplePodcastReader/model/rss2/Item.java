package com.ecos.SimplePodcastReader.model.rss2;

import com.ecos.SimplePodcastReader.model.generic.Enclosure;
import com.ecos.SimplePodcastReader.model.generic.PodcastItem;

import java.util.Date;

public class Item implements PodcastItem {

    private String guid = null;
    private Date pubDate = null;
    private String title = null;
    private String description = null;
    private Enclosure enclosure = null;

    public void setGUID(String guid) {
        this.guid = guid;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    @Override
    public Date getPubDate() {
        return pubDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setEnclosure(Enclosure enclosure) {
        this.enclosure = enclosure;
    }

    @Override
    public Enclosure getEnclosure() {
        return enclosure;
    }

    @Override
    public int compareTo(PodcastItem another) {
        return getPubDate().compareTo(another.getPubDate()) * -1;
    }

}
