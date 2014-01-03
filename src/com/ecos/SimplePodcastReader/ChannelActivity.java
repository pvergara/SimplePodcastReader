package com.ecos.SimplePodcastReader;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ecos.SimplePodcastReader.adapters.ChannelAdapter;
import com.ecos.SimplePodcastReader.model.generic.ChannelAndTypeCounter;
import com.ecos.SimplePodcastReader.model.generic.Podcast;
import com.ecos.SimplePodcastReader.util.PodcastReader;
import com.ecos.helpers.DBManagerHelper;

import java.util.ArrayList;
import java.util.List;

public class ChannelActivity extends ListActivity {

    private static final int MENU_ITEM_NEW_CHANNEL_ID = 1;
    private static final int ACTIVITY_CREATE = 0;
    private List<ChannelAndTypeCounter> channelsAndTypeCount;
    private ChannelAdapter channelAdapter;
    private ProgressDialog progressDialog;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_list);
        channelsAndTypeCount = new ArrayList<ChannelAndTypeCounter>();
        channelAdapter = new ChannelAdapter(this, R.layout.channel_list_item, channelsAndTypeCount);

        this.setListAdapter(channelAdapter);

        refreshChannelsThread.start();
        progressDialog = ProgressDialog.show(ChannelActivity.this,
                getString(R.string.progress_dialog_please_wait),
                getString(R.string.progress_list_retrieving_data), true);
    }

    private void refreshChannelsAndEndProgressDialog() {
        DBManagerHelper dbManagerHelper = new DBManagerHelper(this);
        SQLiteDatabase database = dbManagerHelper.getWritableDatabase();
        List<Podcast> values = dbManagerHelper.fetchAllChannels(database);
        if (values.size() == 0) {
            values = addDefaultChannels(dbManagerHelper, database, values);
        }
        channelsAndTypeCount.clear();
        for (Podcast channel : values) {
            ChannelAndTypeCounter aux = new ChannelAndTypeCounter();
            aux.channel = channel;
            aux.podcastTypeCounter = dbManagerHelper.getPodcastTypeCounterByUrl(database, channel.getUrl());
            channelsAndTypeCount.add(aux);
        }
        database.close();
        runOnUiThread(returnRes);

    }

    private List<Podcast> addDefaultChannels(DBManagerHelper dbManagerHelper,
                                             SQLiteDatabase database, List<Podcast> values) {
        PodcastReader podcastReader = new PodcastReader();
        String eslPodcastURL = "http://feeds.feedburner.com/eslpod?format=rss";
        try {
            Podcast podcast = podcastReader.getPodcast(eslPodcastURL);
            dbManagerHelper.createNewPodcast(eslPodcastURL, podcast,
                    database);
            values = dbManagerHelper.fetchAllChannels(database);

        } catch (Exception e) {
            database.close();
            runOnUiThread(returnRes);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return values;
    }

    public Thread refreshChannelsThread = new Thread() {
        @Override
        public void run() {
            refreshChannelsAndEndProgressDialog();
        }

    };

    private Runnable returnRes = new Runnable() {

        @Override
        public void run() {
            progressDialog.dismiss();
            channelAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);


        int menuItemNewChannelOrder = 0;
        menu.add
                (
                        Menu.NONE,
                        MENU_ITEM_NEW_CHANNEL_ID,
                        menuItemNewChannelOrder,
                        "Add/update new channel"
                )
                .setIcon(android.R.drawable.ic_menu_add);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        switch (item.getItemId()) {
            case MENU_ITEM_NEW_CHANNEL_ID:
                addNewChannel();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        refreshChannelsAndEndProgressDialog();
    }

    private void addNewChannel() {
        Intent channelEditorActivity = new Intent(this, ChannelEditorActivity.class);
        startActivityForResult(channelEditorActivity, ACTIVITY_CREATE);
    }

}
