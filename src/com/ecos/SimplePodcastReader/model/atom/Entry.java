package com.ecos.SimplePodcastReader.model.atom;

import com.ecos.SimplePodcastReader.model.generic.Enclosure;
import com.ecos.SimplePodcastReader.model.generic.PodcastItem;

import java.util.Date;

public class Entry implements PodcastItem {
    private String id;
    private String title;
    private Date updated;
    private String content;
    private Enclosure enclosure;

    public String getId() {
        return this.id;
    }

    public void setId(String entryId) {
        this.id = entryId;

    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getPublished() {
        return this.updated;
    }


    public void setUpdated(Date updated) {
        this.updated = updated;

    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Enclosure getEnclosure() {
        return this.enclosure;
    }

    public void setEnclosure(Enclosure enclosure) {
        this.enclosure = enclosure;

    }

    @Override
    public Date getPubDate() {
        return getPublished();
    }

    @Override
    public String getDescription() {
        return getContent();
    }

    @Override
    public int compareTo(PodcastItem another) {
        return getPubDate().compareTo(another.getPubDate()) * -1;
    }

}
