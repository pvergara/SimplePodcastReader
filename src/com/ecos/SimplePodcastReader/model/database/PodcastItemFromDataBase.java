package com.ecos.SimplePodcastReader.model.database;

import com.ecos.SimplePodcastReader.model.generic.Enclosure;
import com.ecos.SimplePodcastReader.model.generic.PodcastItem;

import java.util.Date;

public class PodcastItemFromDataBase implements PodcastItem {
    private String description;
    private String title;
    private Date pubDate;
    private Enclosure enclosure;

    @Override
    public int compareTo(PodcastItem another) {
        return getPubDate().compareTo(another.getPubDate()) * -1;
    }

    @Override
    public Date getPubDate() {
        return pubDate;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Enclosure getEnclosure() {
        return this.enclosure;
    }

    public void setPubDate(Date string) {
        this.pubDate = string;

    }

    public void setTitle(String string) {
        this.title = string;

    }

    public void setDescription(String string) {
        this.description = string;

    }

    public void setEnclosure(Enclosure enclosure) {
        this.enclosure = enclosure;

    }

    @Override
    public String getDescription() {
        return description;
    }

}
