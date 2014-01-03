package com.ecos.SimplePodcastReader.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.ecos.SimplePodcastReader.PodcastsActivity;
import com.ecos.SimplePodcastReader.R;
import com.ecos.SimplePodcastReader.model.generic.ChannelAndTypeCounter;
import com.ecos.SimplePodcastReader.model.generic.Podcast;
import com.ecos.helpers.DBManagerHelper;
import com.ecos.helpers.PodcastHelper.PodcastType;

import java.util.List;

public class ChannelAdapter extends ArrayAdapter<ChannelAndTypeCounter> {

    private List<ChannelAndTypeCounter> channels;

    private Context context;

    protected Podcast seletedtedChannelToDelete;

    private android.content.DialogInterface.OnClickListener deleteDialogClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    deleteChannel(seletedtedChannelToDelete);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    break;
            }
        }
    };

    public ChannelAdapter(Context context, int textViewResourceId,
                          List<ChannelAndTypeCounter> channels) {
        super(context, textViewResourceId, channels);
        this.channels = channels;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.channel_list_item, null);
        }

        ChannelAndTypeCounter channelAndTypeCounter = this.channels.get(position);
        Podcast channel = channelAndTypeCounter.channel;
        if (channel != null) {
            TextView txtChannelTitle = (TextView) view.findViewById(R.id.txtChannelTitle);
            txtChannelTitle.setText(channel.getTitle());

            View channelItem = view.findViewById(R.id.channelItem);
            channelItem.setTag(channel);
            channelItem.setOnClickListener
                    (
                            new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Podcast channel = (Podcast) v.getTag();
                                    showPostList(channel);
                                }
                            }
                    );

            Button btnDeleteChannel = (Button) view.findViewById(R.id.btnDeleteChannel);
            btnDeleteChannel.setTag(channel);
            btnDeleteChannel.setOnClickListener
                    (
                            new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    seletedtedChannelToDelete = (Podcast) v.getTag();

                                    conditionalDeleteChannel();
                                }
                            }
                    );

            TextView txtAudioCounter = (TextView) view.findViewById(R.id.txtPodcastAudioCounter);
            txtAudioCounter.setText(channelAndTypeCounter.podcastTypeCounter.get(PodcastType.Audio).toString());

            TextView txtVideoCounter = (TextView) view.findViewById(R.id.txtPodcastVideoCounter);
            txtVideoCounter.setText(channelAndTypeCounter.podcastTypeCounter.get(PodcastType.Video).toString());

            TextView txtOtherCounter = (TextView) view.findViewById(R.id.txtPodcastOtherCounter);
            txtOtherCounter.setText(channelAndTypeCounter.podcastTypeCounter.get(PodcastType.Other).toString());

        }

        return view;

    }

    protected void conditionalDeleteChannel() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setMessage("Do you really want to delete the entire channel?")
                .setPositiveButton(android.R.string.ok, deleteDialogClickListener)
                .setNegativeButton(android.R.string.cancel, deleteDialogClickListener).show();
    }

    protected void deleteChannel(Podcast channel) {
        DBManagerHelper dbManagerHelper = new DBManagerHelper(this.getContext());
        SQLiteDatabase database = dbManagerHelper.getWritableDatabase();

        dbManagerHelper.deleteChannelBy(database, channel.getUrl());
        database.close();
        channels.remove(channel);
        notifyDataSetChanged();
    }

    protected void showPostList(Podcast channel) {
        Intent newActivity = new Intent(this.context, PodcastsActivity.class);

        if (channel != null) {
            Bundle b = new Bundle();
            b.putString(PodcastsActivity.ADDRESS, channel.getUrl());

            newActivity.putExtras(b);
            this.context.startActivity(newActivity);
        }

    }

}
