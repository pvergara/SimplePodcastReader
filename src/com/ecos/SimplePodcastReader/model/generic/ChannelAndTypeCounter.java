package com.ecos.SimplePodcastReader.model.generic;

import com.ecos.helpers.PodcastHelper;

import java.util.Map;

public class ChannelAndTypeCounter {
    public Podcast channel;
    public Map<PodcastHelper.PodcastType, Integer> podcastTypeCounter;
}
