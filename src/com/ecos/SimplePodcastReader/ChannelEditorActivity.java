package com.ecos.SimplePodcastReader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ecos.SimplePodcastReader.util.PodcastReader;
import com.ecos.helpers.DBManagerHelper;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.MessageFormat;

public class ChannelEditorActivity extends Activity {
    private ProgressDialog progressDialog;
    private String conditionalMessage;
    protected int conditionalDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_editor);

        addressFromIntentFilter = "";
        if
                (
                this.getIntent() != null &&
                        this.getIntent().getDataString() != null
                ) {
            addressFromIntentFilter = this.getIntent().getDataString();
        }

        txtChannelAddress = (EditText) findViewById(R.id.channel_address);
        if (!addressFromIntentFilter.equals(""))
            txtChannelAddress.setText(addressFromIntentFilter);


        Button btnAddNewChannel = (Button) findViewById(R.id.btnFeedList);
        btnAddNewChannel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String address;
                address = txtChannelAddress.getText().toString();
                if (address != null) {
                    try {
                        addNewChannelByChannelUrl(address);
                    } catch (Exception e) {
                        Toast.makeText(ChannelEditorActivity.this, R.string.error_maybe_the_address_is_not_correct_, Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    protected void addNewChannelByChannelUrl(final String address) throws IOException, SAXException {
        DBManagerHelper dbManagerHelper = new DBManagerHelper(this);
        SQLiteDatabase database = dbManagerHelper.getWritableDatabase();


        boolean isChannelSaved = dbManagerHelper.isChannelSaved(database, address);
        assert database != null;
        database.close();

        if (isChannelSaved) {
            Toast.makeText(this, R.string.existing_channel, Toast.LENGTH_LONG).show();
        } else {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    ChannelEditorActivity.this.addChannel(address);
                }

            };
            thread.start();

            progressDialog =
                    ProgressDialog.show
                            (
                                    ChannelEditorActivity.this,
                                    getString(R.string.progress_dialog_please_wait),
                                    getString(R.string.progress_list_retrieving_data), true
                            );


        }
    }

    protected void addChannel(String address) {
        DBManagerHelper dbManagerHelper = new DBManagerHelper(this);
        SQLiteDatabase database = dbManagerHelper.getWritableDatabase();

        PodcastReader podcastReader = new PodcastReader();
        try {
            dbManagerHelper.createNewPodcast(address, podcastReader.getPodcast(address), database);
            conditionalMessage = MessageFormat.format("{0}{1}", getResources().getString(R.string.app_name), getResources().getString(R.string.channel_added));
            conditionalDuration = 10;
        } catch (Exception e) {
            conditionalMessage = "Couldn't add a channel, probably there're not a real podcast address.";
            conditionalDuration = 100;
            e.printStackTrace();
        } finally {
            assert database != null;
            database.close();
            runOnUiThread(returnRes);
        }
    }

    private Runnable returnRes = new Runnable() {

        @Override
        public void run() {
            progressDialog.dismiss();
            Intent mIntent = new Intent();
            setResult(RESULT_OK, mIntent);
            Toast.makeText
                    (
                            ChannelEditorActivity.this,
                            conditionalMessage,
                            conditionalDuration
                    ).show();
            finish();

        }
    };
    private String addressFromIntentFilter;
    private EditText txtChannelAddress;

}
