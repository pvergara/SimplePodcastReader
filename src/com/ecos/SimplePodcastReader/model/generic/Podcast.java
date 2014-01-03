package com.ecos.SimplePodcastReader.model.generic;

import java.util.HashMap;

public interface Podcast {

    String getTitle();

    String getDescription();

    String getLink();

    HashMap<String, PodcastItem> getItems();

    String getUrl();

}
