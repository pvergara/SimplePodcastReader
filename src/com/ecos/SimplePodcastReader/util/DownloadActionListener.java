package com.ecos.SimplePodcastReader.util;

public interface DownloadActionListener {

    void OnDownloadActionPerformed();

    void OnDownloadActionCanceled(DownloadInfo downloadInfo);

}
