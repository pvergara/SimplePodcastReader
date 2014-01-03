package com.ecos.helpers;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;

import com.ecos.util.MediaPlayerActionListener;

import java.io.IOException;

public class MediaPlayerHelper {
    private static MediaPlayerHelper mediaPlayerHelper;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying;
    private MediaPlayerActionListener mediaPlayerActionListener;
    private Integer songId;
    private Object songPath;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            fireEvents();
        }
    };
    private Handler handler;
    private boolean isUser;

    private MediaPlayerHelper() {
        isPlaying = false;
    }

    public static MediaPlayerHelper getMediaPlayerHelper() {
        if (mediaPlayerHelper == null)
            mediaPlayerHelper = new MediaPlayerHelper();

        return mediaPlayerHelper;
    }

    public void play(Context context, int id) throws InterruptedException {
        preparePlayerByUsingResourceId(context, id);
        startPlaying();
    }

    public void play(String filePath) throws IllegalArgumentException, IllegalStateException, IOException, InterruptedException {
        preparePlayerByUsingFilePath(filePath);

        startPlaying();
    }

    public void stop() {
        isUser = true;
        mediaPlayer.stop();
        isPlaying = false;
        setNoSongToPlay();
//		if(isPlaying())
//			fireStoppedEvent(true);
    }

    private void setNoSongToPlay() {
        songId = null;
        songPath = null;
    }

    private void preparePlayerByUsingResourceId(Context context, int id) {
        mediaPlayer = MediaPlayer.create(context, id);
        setSongToPlayById(id);
    }

    private void setSongToPlayById(int id) {
        songId = id;
        songPath = null;
    }

    private void preparePlayerByUsingFilePath(String path) throws IOException {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(path);
        mediaPlayer.prepare();
        setSongToPlayByPath(path);
    }

    private void setSongToPlayByPath(String path) {
        songId = null;
        songPath = path;
    }

    private void startPlaying() throws InterruptedException {
        mediaPlayer.start();
        isPlaying = true;
        isUser = false; //Solo se pone a true con un stop
        if (mediaPlayerActionListener != null) {
            handler = new Handler();
            fireEvents();
        }
    }

    protected void fireEvents() {
        if (mediaPlayer.isPlaying()) {
            handler.removeCallbacks(runnable);
            fireCurrentPositionChangedEvent();
            handler.postDelayed(runnable, 500);
        } else {
            handler.removeCallbacks(runnable);
            fireStoppedEvent();
        }

    }

    protected void fireStoppedEvent() {
        if (mediaPlayerActionListener != null)
            mediaPlayerActionListener.OnStopped(isUser);
        isUser = false;

    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public void setMediaPlayerActionListener(MediaPlayerActionListener mediaPlayerActionListener) {
        this.mediaPlayerActionListener = mediaPlayerActionListener;
    }

    private void fireCurrentPositionChangedEvent() {
        if (mediaPlayerActionListener != null)
            mediaPlayerActionListener.OnCurrentPositionChanged();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void setPosition(int position) {
        mediaPlayer.seekTo(position);
    }


}
