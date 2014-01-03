package com.ecos.SimplePodcastReader.util;

import android.util.Log;

import com.ecos.helpers.FileHelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class Downloader {

    private static final int _8K = 8094;


    public Downloader(URL urlSource, String filePathTarget, int length) {
        this.url = urlSource;
        this.filePath = filePathTarget;
        this.setLength(length);
    }

    private URL url;
    private String filePath;
    private int length;

    private void setLength(int length) {
        this.length = length;
    }

    private long bytesDownloaded;
    private DownloadProgressListener dowloadProgressListener;
    private boolean stop;
    private DownloadFinishedListener dowloadFinishedListener;

    public void setDowloadProgressListener(DownloadProgressListener dowloadProgressListener) {
        this.dowloadProgressListener = dowloadProgressListener;
    }

    public void start() throws IOException {
        Log.d(Downloader.class.toString(), "download begining");
        Log.d(Downloader.class.toString(), "download url:" + url);
        Log.d(Downloader.class.toString(), "downloaded file name:" + filePath);
        long startTime = System.currentTimeMillis();


        URLConnection urlConnection = url.openConnection();
        this.setLength(FileHelper.getLengthByUrl(url));

        InputStream inputStream = urlConnection.getInputStream();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

        File file = new File(filePath);

        if (file.isFile())
            this.setBytesDownloaded(file.length());

        FileOutputStream fos = new FileOutputStream(file);
        byte[] bytes = new byte[_8K];
        int current;
        int offset = 0;

        this.stop = false;
        while ((current = bufferedInputStream.read(bytes)) != -1 && !this.stop) {
            fos.write(bytes, 0, current);
            this.setBytesDownloaded(this.getBytesDownloaded() + current);

            offset += current;

            fireOnDownloadingProgressEvent(offset);
        }
        fos.close();
        fireOnDownloadFinishedEvent();

        long endTime = System.currentTimeMillis();
        Log.d
                (
                        Downloader.class.toString(),
                        "download ready in" + ((endTime - startTime) / 1000) + " sec"
                );


    }

    private void fireOnDownloadFinishedEvent() {
        if (dowloadFinishedListener != null)
            dowloadFinishedListener.OnDowloadFinish();

    }

    private void fireOnDownloadingProgressEvent(int offset) {
        if (this.dowloadProgressListener != null)
            this.dowloadProgressListener.OnDownloaingProgress(offset, length);
    }

    private void setBytesDownloaded(long bytesDownloaded) {
        this.bytesDownloaded = bytesDownloaded;
    }

    public long getBytesDownloaded() {
        return bytesDownloaded;
    }


}
