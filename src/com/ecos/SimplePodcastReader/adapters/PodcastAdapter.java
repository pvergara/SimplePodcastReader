package com.ecos.SimplePodcastReader.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ecos.SimplePodcastReader.PodcastDescriptionActivity;
import com.ecos.SimplePodcastReader.R;
import com.ecos.SimplePodcastReader.model.generic.PodcastItem;
import com.ecos.SimplePodcastReader.util.Downloader;
import com.ecos.helpers.DBManagerHelper;
import com.ecos.helpers.DirectoryHelper;
import com.ecos.helpers.FileHelper;
import com.ecos.helpers.MediaPlayerHelper;
import com.ecos.helpers.PodcastHelper;
import com.ecos.helpers.PodcastHelper.PodcastType;
import com.ecos.util.MediaPlayerActionListener;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PodcastAdapter extends ArrayAdapter<PodcastItem> {
    private class DownloaderRunnableImplementation implements Runnable {
        private boolean isDownloaded;
        private int retryingCounter;
        private Downloader downloader;

        public DownloaderRunnableImplementation(Downloader downloader) {
            this.downloader = downloader;

        }

        @Override
        public void run() {
            isDownloaded = false;
            retryingCounter = 0;
            while (!isDownloaded && retryingCounter < MAX_RETRY_COUNT) {
                try {
                    downloader.start();
                    isDownloaded = true;
                } catch (IOException e) {
                    retryingCounter++;
                    e.printStackTrace();
                }
            }
        }
    }

    private class RunnableImplementation implements Runnable {
        PodcastItem item;
        View view;

        public RunnableImplementation(View view, PodcastItem item) {
            this.item = item;
            this.view = view;
        }

        @Override
        public void run() {
            fireDownloadingProgressLoop(this.item, this.view);
        }
    }

    private List<PodcastItem> Items;
    private Activity context;
    public final static String podcastFolder = DirectoryHelper
            .getPodcastFolder();
    protected static final int MAX_RETRY_COUNT = 5;
    private MediaPlayerHelper mediaPlayerHelper;
    private SeekBar sbMediaPlayer;
    private Button btMediaPlayerStop;
    private PlayMode playMode;
    protected static PodcastItem podcastActualPlaying;
    private ArrayList<CharSequence> dialogItems;

    private static Map<String, DownloaderRunnableImplementation> downloaderRunnableImplementatioMap;
    private static Map<String, Handler> handlerMap;
    private static Map<String, RunnableImplementation> runnableImplementationMap;

    public PodcastAdapter(Activity context, int textViewResourceId, List<PodcastItem> feeds) {
        super(context, textViewResourceId, feeds);
        this.Items = feeds;
        this.context = context;
        configureCustomMediaPlayer();

        if (downloaderRunnableImplementatioMap == null)
            downloaderRunnableImplementatioMap = new HashMap<String, PodcastAdapter.DownloaderRunnableImplementation>();

        if (handlerMap == null)
            handlerMap = new HashMap<String, Handler>();

        if (runnableImplementationMap == null)
            runnableImplementationMap = new HashMap<String, RunnableImplementation>();

    }


    protected void chooseAndInvokePodcastAction(int selectedIndex, PodcastItem item) {
        String selectedOption = (String) dialogItems.get(selectedIndex);
        if (selectedOption.equals(context.getResources().getString(R.string.podcast_list_delete))) {
            conditionalDeletePodcast(item);
        }

        if (selectedOption.equals(context.getResources().getString(R.string.podcast_list_details))) {
            showPodcastDescription(item);
        }

        if (selectedOption.equals(context.getResources().getString(R.string.podcast_list_play_and_next))) {
            continousPlayNext(item);
        }

        if (selectedOption.equals(context.getResources().getString(R.string.podcast_list_play_and_prev))) {
            continousPlayPrevious(item);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // View view = convertView;
        View view;

        LayoutInflater layoutInflater = (LayoutInflater) this.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.podcast_list_item, null);

        PodcastItem item = this.Items.get(position);
        if (item != null) {
            setPodcastTitle(view, item);

            setFeedPublishDate(view, item);

            setTypeOfPodcast(view, item);

            View llItem = view.findViewById(R.id.podcastItem);
            llItem.setTag(item);
            llItem.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    PodcastItem item = (PodcastItem) v.getTag();
                    downloadOrPlayPodcast(item);
                }
            });

            Button btDelete = (Button) view.findViewById(R.id.podcastDelete);
            btDelete.setTag(item);
            btDelete.setOnClickListener
                    (
                            new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    PodcastItem item = (PodcastItem) v.getTag();


                                    AlertDialog.Builder builder = getAlertDialogBuilder(item);


                                    builder.create().show();

                                }

                                private AlertDialog.Builder getAlertDialogBuilder(final PodcastItem item) {
                                    dialogItems = new ArrayList<CharSequence>();

                                    dialogItems.add(context.getResources().getString(R.string.podcast_list_details));

                                    try {
                                        if (PodcastHelper.isPodcastInLocal(item)) {
                                            dialogItems.add(context.getResources().getString(R.string.podcast_list_delete));
                                            if (PodcastHelper.getPodcastType(item) == PodcastType.Audio) {
                                                dialogItems.add(context.getResources().getString(R.string.podcast_list_play_and_next));
                                                dialogItems.add(context.getResources().getString(R.string.podcast_list_play_and_prev));
                                            }
                                        }
                                    } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }


                                    CharSequence[] items = new String[dialogItems.size()];
                                    dialogItems.toArray(items);


                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle(R.string.podcast_list_more_actions);
                                    builder.setItems
                                            (
                                                    items,
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int selectedIndex) {
                                                            chooseAndInvokePodcastAction(selectedIndex, item);
                                                        }
                                                    }
                                            );
                                    return builder;
                                }
                            }
                    );

            try {
                //PodcastHelper.correctPodcastFileLength(item);
                if (!PodcastHelper.isPodcastInLocal(item)) {
                    showDownloadPodcastElements(view);
                } else {

                    //if (downloaderRunnableImplementatioMap.get(item.getTitle())!=null)
                    if (PodcastHelper.isPodcastDownloading(item)) {
                        showDownloadingPodcastElements(view, item);
                    } else {
                        showPlayPodcastElements(view, item);
                    }

                }
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        return view;
    }

    private void showDownloadingPodcastElements(View view, PodcastItem item) throws URISyntaxException {
        View pgDownloadingItem = (View) view.findViewById(R.id.feed_downloading_progress);
        pgDownloadingItem.setVisibility(View.VISIBLE);
        fireDownloadingProgressLoop(item, view);

        View podcastItem = view.findViewById(R.id.podcastItem);
        podcastItem.setBackgroundDrawable(view.getResources().getDrawable(R.drawable.gradient_box_orange));
    }

    private void fireDownloadingProgressLoop(PodcastItem item, View view) {
        Handler handler = handlerMap.get(item.getTitle());
        RunnableImplementation runnable = runnableImplementationMap.get(item.getTitle());

        if (PodcastHelper.isPodcastInLocal(item)) {
            if (handler == null) {
                handlerMap.put(item.getTitle(), new Handler());
            }
            handler = handlerMap.get(item.getTitle());
            if (runnable == null) {
                runnableImplementationMap.put(item.getTitle(), new RunnableImplementation(view, item));
            }
            runnable = runnableImplementationMap.get(item.getTitle());

            if (PodcastHelper.isPodcastDownloading(item)) {

                handler.removeCallbacks(runnable);
                ProgressBar pbDownloading = (ProgressBar) view.findViewById(R.id.feed_downloading_progress);

                float currentSize = new File(PodcastHelper.getPodcastFilePath(item)).length();
                float totalSize = item.getEnclosure().getLength();

                int percentage = (int) ((float) currentSize * 100 / totalSize);

                pbDownloading.setProgress(percentage);
                handler.postDelayed(runnable, 100);

            } else {
                conditionalRemoveHandlerAndRunnableFromMap(item, handler, runnable);

                notifyDataSetChanged();
                handler.removeCallbacks(runnable);
            }
        } else {
            conditionalRemoveHandlerAndRunnableFromMap(item, handler, runnable);
        }

    }


    private void conditionalRemoveHandlerAndRunnableFromMap(PodcastItem item, Handler handler, RunnableImplementation runnable) {
        handlerMap.remove(item.getTitle());
        runnableImplementationMap.remove(item.getTitle());
        downloaderRunnableImplementatioMap.remove(item.getTitle());
    }


    private void showPlayPodcastElements(View view, PodcastItem item) {
        View pgDownloadingItem = view.findViewById(R.id.feed_downloading_progress);
        pgDownloadingItem.setVisibility(View.GONE);

        View podcastItem = view.findViewById(R.id.podcastItem);
        podcastItem.setBackgroundDrawable(view.getResources().getDrawable(R.drawable.gradient_box_green));
    }

    private void showDownloadPodcastElements(View view) {
        View pgDownloadingItem = view.findViewById(R.id.feed_downloading_progress);
        pgDownloadingItem.setVisibility(View.GONE);

        View podcastItem = view.findViewById(R.id.podcastItem);
        podcastItem.setBackgroundDrawable(view.getResources().getDrawable(R.drawable.gradient_box_red));
    }

    private void setTypeOfPodcast(View view, PodcastItem item) {
        TextView typeOfPodcast = (TextView) view.findViewById(R.id.typeOfPodcast);
        typeOfPodcast.setText(PodcastHelper.getPodcastType(item).toString());
    }

    private void downloadOrPlayPodcast(PodcastItem item) {
        if (!PodcastHelper.isPodcastInLocal(item)) {
            downloadPodcast(item);
        } else {
            if (!PodcastHelper.isPodcastDownloading(item)) {
                playPodcast(item);
            }
        }
    }

    protected void conditionalDeletePodcast(final PodcastItem item) {
        new AlertDialog.Builder(this.context).
                setMessage(R.string.deletePodcastQuestion).
                setPositiveButton
                        (
                                android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == DialogInterface.BUTTON_POSITIVE)
                                            deletePodcast(item);
                                    }
                                }
                        ).
                setNegativeButton(android.R.string.cancel, null).
                show();
    }

    private void deletePodcast(PodcastItem item) {
        String filePath = PodcastHelper.getPodcastFilePath(item);
        if (filePath != null) {
            File file = new File(filePath);
            file.delete();
            notifyDataSetChanged();
        }
    }

    private void downloadPreviousFeed(PodcastItem item) {
        PodcastItem previousItem = PodcastHelper.getPreviousAudioItem(Items, item);
        Log.d(PodcastAdapter.class.toString(), "PreviousItem = "
                + previousItem);

        if (previousItem != null && !PodcastHelper.isPodcastInLocal(previousItem)) {
            downloadPodcast(previousItem);
        }

    }

    public void setFeedPublishDate(View view, PodcastItem item) {
        TextView tvPubDate = (TextView) view.findViewById(R.id.feedPudDate);
        if (tvPubDate != null) {
            java.text.DateFormat df = android.text.format.DateFormat
                    .getDateFormat(this.getContext().getApplicationContext());
            tvPubDate.setText(df.format(item.getPubDate()));
        }
    }

    public void setPodcastTitle(View view, PodcastItem item) {
        TextView tvTitle = (TextView) view.findViewById(R.id.podcastTitle);
        if (tvTitle != null) {
            tvTitle.setText(item.getTitle());
        }
    }

    public void continousPlayPrevious(PodcastItem podcast) {
        playMode = PlayMode.ContiniousPlayPrev;
        PodcastAdapter.podcastActualPlaying = podcast;
        downloadPreviousFeed(podcast);
        try {
            playUsingCustomMediaPlayer(podcast);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void continousPlayNext(PodcastItem podcast) {
        playMode = PlayMode.ContiniousPlayNext;
        PodcastAdapter.podcastActualPlaying = podcast;
        downloadNextPodcast(podcast);

        try {
            playUsingCustomMediaPlayer(podcast);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void playPodcast(PodcastItem podcast) {
        try {
            if (PodcastHelper.getPodcastType(podcast) == PodcastType.Audio) {
                PodcastAdapter.podcastActualPlaying = podcast;
                playMode = PlayMode.Play;
                playUsingCustomMediaPlayer(podcast);
            } else {
                String podcastFilePath = PodcastHelper
                        .getPodcastFilePath(podcast);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(podcastFilePath)),
                        podcast.getEnclosure().getType());
                context.startActivity(intent);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void playUsingCustomMediaPlayer(PodcastItem podcast)
            throws IOException, InterruptedException, URISyntaxException {
        String podcastFilePath = PodcastHelper.getPodcastFilePath(podcast);
        if (mediaPlayerHelper.isPlaying()) {
            mediaPlayerHelper.stop();
        }
        mediaPlayerHelper.play(podcastFilePath);
        showCustomMediaPlayer();
    }

    private void configureCustomMediaPlayer() {
        sbMediaPlayer = (SeekBar) context.findViewById(R.id.sbMediaPlayer);
        sbMediaPlayer.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (fromUser)
                    changeMusicPosition(progress);
            }
        });

        btMediaPlayerStop = (Button) context.findViewById(R.id.btnPlayStop);
        btMediaPlayerStop.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mediaPlayerHelper.stop();

            }
        });

        mediaPlayerHelper = MediaPlayerHelper.getMediaPlayerHelper();
        mediaPlayerHelper
                .setMediaPlayerActionListener(new MediaPlayerActionListener() {

                    @Override
                    public void OnStopped(boolean isUser) {
                        if (isUser || playMode == PlayMode.Play) {
                            TableLayout mediaPlayerControl = (TableLayout) context
                                    .findViewById(R.id.mediaPlayerControl);
                            mediaPlayerControl.setVisibility(View.GONE);
                        } else {
                            if (!isUser
                                    && playMode == PlayMode.ContiniousPlayPrev) {
                                PodcastItem prevItem = PodcastHelper
                                        .getPreviousAudioItem(Items,
                                                podcastActualPlaying);
                                if (prevItem != null)
                                    continousPlayPrevious(prevItem);

                            }
                            if (!isUser
                                    && playMode == PlayMode.ContiniousPlayNext) {
                                PodcastItem nextItem = PodcastHelper
                                        .getNextAudioItem(Items,
                                                podcastActualPlaying);
                                if (nextItem != null)
                                    continousPlayNext(nextItem);

                            }
                        }
                    }

                    @Override
                    public void OnCurrentPositionChanged() {
                        sbMediaPlayer.setProgress(mediaPlayerHelper
                                .getCurrentPosition());
                    }
                });

        if (mediaPlayerHelper.isPlaying())
            showCustomMediaPlayer();

    }

    private void showCustomMediaPlayer() {
        TextView podcastTitle = (TextView) context
                .findViewById(R.id.podcastTitle);
        podcastTitle.setText(podcastActualPlaying.getTitle());
        TableLayout mediaPlayerControl = (TableLayout) context
                .findViewById(R.id.mediaPlayerControl);
        mediaPlayerControl.setVisibility(View.VISIBLE);
        if (mediaPlayerHelper.isPlaying())
            sbMediaPlayer.setMax(mediaPlayerHelper.getDuration());
    }

    protected void changeMusicPosition(int progress) {
        mediaPlayerHelper.setPosition(progress);
    }

    public void showPodcastDescription(PodcastItem item) {
        Intent newActivity = new Intent(context,
                PodcastDescriptionActivity.class);

        if (item != null && newActivity != null) {
            Bundle b = new Bundle();
            b.putString(PodcastDescriptionActivity.DESCRIPTION,
                    item.getDescription());

            newActivity.putExtras(b);
            context.startActivity(newActivity);
        }
    }

    public void downloadPodcast(final PodcastItem item) {
        if (downloaderRunnableImplementatioMap.get(item.getTitle()) == null) {

            try {
                String podcastFilePath = PodcastHelper.getPodcastFilePath(item);
                changeToDownloadingState(podcastFilePath);

                //Some podcasts lyes about her "filesize" on legth enclosure attrib.
                //for this reason it's mandatory to obtain the filesize from the server.
                item.getEnclosure().setLength(FileHelper.getLengthByUrl(item.getEnclosure().getUrl()));

                DBManagerHelper dbManagerHelper = new DBManagerHelper(this.context);
                SQLiteDatabase database = dbManagerHelper.getWritableDatabase();

                dbManagerHelper.updatePodcastItemLength(database, item);

                database.close();


                Downloader downloader =
                        new Downloader
                                (
                                        item.getEnclosure().getUrl(),
                                        podcastFilePath,
                                        item.getEnclosure().getLength()
                                );

                DownloaderRunnableImplementation downloaderRunnable = new DownloaderRunnableImplementation(downloader);
                downloaderRunnableImplementatioMap.put(item.getTitle(), downloaderRunnable);

                new Thread(downloaderRunnable).start();

            } catch (Exception e) {
                Toast.makeText
                        (
                                this.context,
                                R.string.podcast_list_error_downloaing_podcast,
                                android.widget.Toast.LENGTH_LONG
                        ).
                        show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText
                    (
                            this.context,
                            "The podcats is actually downloading",
                            android.widget.Toast.LENGTH_LONG
                    ).
                    show();
        }
    }

    private void changeToDownloadingState(String podcastFilePath) throws IOException {
        File file = new File(podcastFilePath);
        file.createNewFile();
        notifyDataSetChanged();
        Toast.makeText
                (
                        this.context,
                        R.string.podcast_list_downloading,
                        android.widget.Toast.LENGTH_LONG
                ).
                show();
    }

    private void downloadNextPodcast(PodcastItem item) {
        PodcastItem nextItem = PodcastHelper.getNextAudioItem(Items, item);
        if (nextItem != null && !PodcastHelper.isPodcastInLocal(nextItem)) {
            downloadPodcast(nextItem);
        }
    }


}
