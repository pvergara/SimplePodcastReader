package com.ecos.helpers;

import android.net.Uri;
import android.util.Log;

import com.ecos.SimplePodcastReader.adapters.PodcastAdapter;
import com.ecos.SimplePodcastReader.model.generic.PodcastItem;

import java.io.File;
import java.net.URL;
import java.util.List;

public class PodcastHelper {
    public enum PodcastType {
        Audio,
        Video,
        Other
    }

    public static PodcastType getPodcastType(PodcastItem item) {
        PodcastType type = PodcastType.Other;

        if
                (
                item != null &&
                        item.getEnclosure() != null &&
                        item.getEnclosure().getType() != null
                ) {
            if (item.getEnclosure().getType().startsWith("audio")) {
                type = PodcastType.Audio;
            } else if (item.getEnclosure().getType().startsWith("video")) {
                type = PodcastType.Video;
            }
        }

        return type;
    }

    public static PodcastItem getNextAudioItem(List<PodcastItem> items2, PodcastItem item) {
        PodcastItem result = null;
        int index = items2.indexOf(item);

        if (index != -1) {
            boolean isAudioPodcast = false;
            PodcastItem aux = null;
            while (index < items2.size() - 1 && !isAudioPodcast) {
                index++;
                aux = items2.get(index);
                if
                        (
                        aux.getEnclosure() != null &&
                                aux.getEnclosure().getType() != null &&
                                aux.getEnclosure().getType().startsWith("audio")
                        )
                    isAudioPodcast = true;
            }
            if (isAudioPodcast)
                result = aux;
        }
        return result;
    }

    public static PodcastItem getPreviousAudioItem(List<PodcastItem> items2, PodcastItem item) {
        PodcastItem result = null;
        int index = items2.indexOf(item);
        Log.d(PodcastAdapter.class.toString(), "Index = " + index);

        if (index != -1) {
            boolean isAudioPodcast = false;
            PodcastItem aux = null;
            while (index > 0 && !isAudioPodcast) {
                index--;
                aux = items2.get(index);
                if
                        (
                        aux.getEnclosure() != null &&
                                aux.getEnclosure().getType() != null &&
                                aux.getEnclosure().getType().startsWith("audio")
                        )
                    isAudioPodcast = true;
            }
            if (isAudioPodcast)
                result = aux;
        }
        return result;
    }

    public static boolean isPodcastInLocal(PodcastItem item) {
        File file = null;
        String filePath = PodcastHelper.getPodcastFilePath(item);
        if (filePath != null)
            file = new File(filePath);

        return (file != null) && (file.isFile());
    }

    public static boolean isPodcastDownloading(PodcastItem item) {
        File file = null;
        String filePath = PodcastHelper.getPodcastFilePath(item);
        if (filePath != null)
            file = new File(filePath);

        return (file != null) && (file.isFile())
                && (file.length() < item.getEnclosure().getLength());
    }

    public static String getPodcastFilePath(PodcastItem previousItem) {
        String result = null;

        if (previousItem.getEnclosure() != null
                && previousItem.getEnclosure().getUrl() != null)
            result = PodcastAdapter.podcastFolder
                    + File.separator
                    + PodcastHelper.getPodcastFileNameFromURL(previousItem.getEnclosure()
                    .getUrl());

        return result;
    }

    public static String getPodcastFileNameFromURL(URL url) {
        Uri aux = Uri.parse(url.toString());
        List<String> segments = aux.getPathSegments();
        String fileName;
        fileName = segments.get(segments.size() - 1);
        return fileName;
    }


}
