package com.ecos.SimplePodcastReader;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ecos.SimplePodcastReader.adapters.PodcastAdapter;
import com.ecos.SimplePodcastReader.model.generic.Podcast;
import com.ecos.SimplePodcastReader.model.generic.PodcastItem;
import com.ecos.SimplePodcastReader.util.PodcastReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PodcastsActivity extends ListActivity {
    private String address = "http://feeds.feedburner.com/eslpod?format=rss";
    private List<PodcastItem> feeds;
    private ProgressDialog progressDialog;
    private PodcastAdapter feedsAdapter;

    public static final String ADDRESS = "address";
    private static final int MENU_ITEM_UPDATE_CHANNEL_ID = 0;

    /**
     * Called when the activity is first created.
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        setContentView(R.layout.podcast_list);

        setAddressFromPreviousActivity();

        feeds = new ArrayList<PodcastItem>();
        feedsAdapter = new PodcastAdapter(this, R.layout.podcast_list_item,
                feeds);
        this.setListAdapter(feedsAdapter);


        Thread thread = new Thread() {
            @Override
            public void run() {
                PodcastsActivity.this.getFeeds(false);
            }

        };
        thread.start();

        progressDialog = ProgressDialog.show(PodcastsActivity.this,
                getString(R.string.progress_dialog_please_wait),
                getString(R.string.progress_list_retrieving_data), true);

    }


    private void setAddressFromPreviousActivity() {
        Bundle bundle = getBundle();

        String aux = bundle.getString(PodcastsActivity.ADDRESS);
        if (aux != null)
            this.address = aux;

        Log.d(PodcastsActivity.class.toString(), "Address = " + this.address);
    }

    private Bundle getBundle() {
        return getIntent().getExtras();
    }

    private void getFeeds(boolean updateFeeds) {
        PodcastReader podcastReader = new PodcastReader();
        Podcast podcastFeed;
        try {
            Log.d(PodcastsActivity.class.toString(), "Address = "
                    + this.address);
            podcastFeed = podcastReader.getPodcastFromDatabase(address,
                    this.getBaseContext(), updateFeeds);
            if (podcastFeed != null && podcastFeed.getItems() != null
                    && !podcastFeed.getItems().isEmpty()) {
                ArrayList<PodcastItem> values = new ArrayList<PodcastItem>(
                        podcastFeed.getItems().values());
                Collections.sort(values);
                for (PodcastItem feed : values) {
                    feeds.add(feed);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            runOnUiThread(returnRes);
        }
    }

    private Runnable returnRes = new Runnable() {

        @Override
        public void run() {
            progressDialog.dismiss();
            feedsAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        int menuItemNewChannelOrder = 0;
        menu.add(Menu.NONE, MENU_ITEM_UPDATE_CHANNEL_ID,
                menuItemNewChannelOrder, R.string.update_channel).setIcon(
                R.drawable.ic_menu_refresh);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        switch (item.getItemId()) {
            case MENU_ITEM_UPDATE_CHANNEL_ID:
                updateChannel();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateChannel() {
        feeds = new ArrayList<PodcastItem>();
        Thread thread = new Thread() {
            @Override
            public void run() {
                PodcastsActivity.this.getFeeds(true);
            }

        };
        thread.start();
        progressDialog = ProgressDialog.show(PodcastsActivity.this,
                getString(R.string.progress_dialog_please_wait),
                getString(R.string.progress_list_retrieving_data), true);
    }

}