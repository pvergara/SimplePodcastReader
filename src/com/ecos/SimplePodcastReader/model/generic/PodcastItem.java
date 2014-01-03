package com.ecos.SimplePodcastReader.model.generic;

import java.util.Date;


public interface PodcastItem extends Comparable<PodcastItem> {
    Date getPubDate();

    String getTitle();

    String getDescription();

    Enclosure getEnclosure();
}
