package com.ecos.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ecos.SimplePodcastReader.model.database.PodcastFromDataBase;
import com.ecos.SimplePodcastReader.model.database.PodcastItemFromDataBase;
import com.ecos.SimplePodcastReader.model.generic.Enclosure;
import com.ecos.SimplePodcastReader.model.generic.Podcast;
import com.ecos.SimplePodcastReader.model.generic.PodcastItem;
import com.ecos.helpers.PodcastHelper.PodcastType;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DBManagerHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SimplePodcastReader";
    private static final int DATABASE_VERSION = 4;

    private static final String CHANNEL_TABLE_NAME = "Channel";
    private static final String CHANNELITEM_TABLE_NAME = "ChannelItem";

    private static final String TITLE = "title";
    private static final String URL = "url";
    private static final String GUID = "guid";
    private static final String DESCRIPTION = "description";
    private static final String PUBDATE = "pubDate";
    private static final String TYPE = "type";
    private static final String LENGTH = "length";
    private static final String ID = "id";
    private static final String LINK = "link";

    public DBManagerHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(getSchemaCreationDDLChannel());
        database.execSQL(getSchemaCreationDDLChannelItem());
    }

    private String getSchemaCreationDDLChannel() {
        StringBuilder sb = new StringBuilder();

        sb.append(" CREATE TABLE").append("\n");
        sb.append(" 	").append(CHANNEL_TABLE_NAME).append("").append("\n");
        sb.append(" (").append("\n");
        sb.append(" 	").append(getIdChannelFieldName()).append(" integer primary key autoincrement ").append(",").append("\n");
        sb.append(" 	").append(URL).append(" text ").append(",").append("\n");
        sb.append(" 	").append(TITLE).append(" text ").append(",").append("\n");
        sb.append(" 	").append(LINK).append(" text ").append(",").append("\n");
        sb.append(" 	").append(DESCRIPTION).append(" text ").append("\n");
        sb.append(" );").append("\n");

        return sb.toString();
    }

    private String getSchemaCreationDDLChannelItem() {
        StringBuilder sb = new StringBuilder();

        sb.append(" CREATE TABLE").append("\n");
        sb.append(" 	").append(CHANNELITEM_TABLE_NAME).append("").append("\n");
        sb.append(" (").append("\n");
        sb.append(" 	").append(ID).append(CHANNELITEM_TABLE_NAME).append(" integer primary key autoincrement ").append(",").append("\n");
        sb.append(" 	").append(getIdChannelFieldName()).append(" integer REFERENCES ").append(CHANNEL_TABLE_NAME).append("(").append(getIdChannelFieldName()).append(") ON DELETE CASCADE").append(",").append("\n");
        sb.append(" 	").append(GUID).append(" text ").append(",").append("\n");
        sb.append(" 	").append(TITLE).append(" text ").append(",").append("\n");
        sb.append(" 	").append(DESCRIPTION).append(" text ").append(",").append("\n");
        sb.append(" 	").append(PUBDATE).append(" text ").append(",").append("\n");
        sb.append(" 	").append(URL).append(" text ").append(",").append("\n");
        sb.append(" 	").append(LENGTH).append(" integer ").append(",").append("\n");
        sb.append(" 	").append(TYPE).append(" text ").append("\n");
        sb.append(" );").append("\n");

        return sb.toString();
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL(getSchemaErasingDDLChannel());
        database.execSQL(getSchemaErasingDDLChannelItem());

        onCreate(database);
    }


    private String getSchemaErasingDDLChannel() {
        StringBuilder sb = new StringBuilder();

        sb.append(" DROP TABLE IF EXISTS").append("\n");
        sb.append(" 	").append(CHANNEL_TABLE_NAME).append("").append(";").append("\n");

        return sb.toString();
    }

    private String getSchemaErasingDDLChannelItem() {
        StringBuilder sb = new StringBuilder();

        sb.append(" DROP TABLE IF EXISTS").append("\n");
        sb.append(" 	").append(CHANNELITEM_TABLE_NAME).append("").append(";").append("\n");
        return sb.toString();
    }

    public int addChannel(SQLiteDatabase database, String title, String url, String link, String descripcion) {
        ContentValues initialValues = createChannelContentValues(url, title, link, descripcion);

        return (int) database.insert(CHANNEL_TABLE_NAME, null, initialValues);

    }

    private ContentValues createChannelContentValues(String url, String title, String link, String description) {
        ContentValues values = new ContentValues();
        values.put(TITLE, title);
        values.put(URL, url);
        values.put(LINK, link);
        values.put(DESCRIPTION, description);
        return values;
    }

    public int addChannelItem
            (
                    SQLiteDatabase database,
                    int idPodcast,
                    String guid,
                    String title,
                    String description,
                    String pubDate,
                    String url,
                    String type,
                    int length
            ) {
        ContentValues initialValues =
                createChannelItemContentValues
                        (
                                idPodcast,
                                guid,
                                title,
                                description,
                                pubDate,
                                url,
                                type,
                                length
                        );

        return (int) database.insert(CHANNELITEM_TABLE_NAME, null, initialValues);
    }

    private ContentValues createChannelItemContentValues
            (
                    int idPodcast,
                    String guid,
                    String title,
                    String description,
                    String pubDate,
                    String url,
                    String type,
                    int length
            ) {
        ContentValues values = new ContentValues();

        values.put(getIdChannelFieldName(), idPodcast);
        values.put(GUID, guid);
        values.put(TITLE, title);
        values.put(DESCRIPTION, description);
        values.put(PUBDATE, pubDate);
        values.put(URL, url);
        values.put(TYPE, type);
        values.put(LENGTH, length);

        return values;
    }

    public Podcast getChannelBy(SQLiteDatabase database, String channelURL) throws MalformedURLException, IllegalArgumentException {
        PodcastFromDataBase resultado = null;


        Cursor podcastCursor = fetchChannelByUrl(database, channelURL);
        boolean puedeContinuar = podcastCursor.moveToFirst();

        boolean podcastEncontrado = puedeContinuar;

        int podcastId = 0;
        while (puedeContinuar) {
            resultado = new PodcastFromDataBase();
            podcastId = podcastCursor.getInt(podcastCursor.getColumnIndexOrThrow(getIdChannelFieldName()));
            resultado.setTitle(podcastCursor.getString(podcastCursor.getColumnIndexOrThrow(TITLE)));
            resultado.setLink(podcastCursor.getString(podcastCursor.getColumnIndexOrThrow(LINK)));
            resultado.setDescription(podcastCursor.getString(podcastCursor.getColumnIndexOrThrow(DESCRIPTION)));

            puedeContinuar = podcastCursor.moveToNext();
        }
        podcastCursor.close();

        if (podcastEncontrado) {
            Cursor podcastItemCursor = fetchChannelItemsByPodcastId(database, podcastId);

            puedeContinuar = podcastItemCursor.moveToFirst();

            while (puedeContinuar) {
                PodcastItemFromDataBase item = new PodcastItemFromDataBase();
                SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
                Date fecha = new Date();
                try {
                    fecha = formatter.parse(podcastItemCursor.getString(podcastItemCursor.getColumnIndexOrThrow(PUBDATE)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                item.setPubDate(fecha);
                item.setTitle(podcastItemCursor.getString(podcastItemCursor.getColumnIndexOrThrow(TITLE)));
                item.setDescription(podcastItemCursor.getString(podcastItemCursor.getColumnIndexOrThrow(DESCRIPTION)));

                Enclosure enclosure = new Enclosure();

                URL url = null;
                String stringURL = podcastItemCursor.getString(podcastItemCursor.getColumnIndexOrThrow(URL));
                if (stringURL != null)
                    url = new URL(stringURL);
                enclosure.setUrl(url);
                enclosure.setType(podcastItemCursor.getString(podcastItemCursor.getColumnIndexOrThrow(TYPE)));
                enclosure.setLength(podcastItemCursor.getInt(podcastItemCursor.getColumnIndexOrThrow(LENGTH)));

                item.setEnclosure(enclosure);

                String guid = podcastItemCursor.getString(podcastItemCursor.getColumnIndexOrThrow(GUID));

                resultado.addItem(guid, item);

                puedeContinuar = podcastItemCursor.moveToNext();
            }
            podcastItemCursor.close();
        }

        return resultado;
    }

    private Cursor fetchChannelItemsByPodcastId(SQLiteDatabase database,
                                                int podcastId) {
        return database.query
                (
                        CHANNELITEM_TABLE_NAME,
                        new String[]
                                {
                                        getIdChannelItemFieldName(),
                                        GUID,
                                        PUBDATE,
                                        TITLE,
                                        DESCRIPTION,
                                        URL,
                                        TYPE,
                                        LENGTH
                                },
                        getIdChannelFieldName() + "=" + "" + podcastId + "",
                        null,
                        null,
                        null,
                        null
                );
    }

    private String getIdChannelItemFieldName() {
        StringBuilder sb = new StringBuilder();
        sb.append(ID).append(CHANNELITEM_TABLE_NAME);
        return sb.toString();
    }

    private String getIdChannelFieldName() {
        StringBuilder sb = new StringBuilder();
        sb.append(ID).append(CHANNEL_TABLE_NAME);
        return sb.toString();
    }

    private Cursor fetchChannelByUrl(SQLiteDatabase database, String address) {
        return database.query
                (
                        CHANNEL_TABLE_NAME,
                        new String[]
                                {
                                        getIdChannelFieldName(),
                                        TITLE, //¿Para qué?
                                        URL,
                                        LINK,
                                        DESCRIPTION
                                },
                        URL + "=" + "'" + address + "'",
                        null,
                        null,
                        null,
                        null
                );
    }


    public void createNewPodcast(String podcastAddress, Podcast podcast, SQLiteDatabase database) {
        int idPodcast = addChannel(database, podcast.getTitle(), podcastAddress, podcast.getLink(), podcast.getDescription());
        for (java.util.Map.Entry<String, PodcastItem> item : podcast.getItems().entrySet()) {
            PodcastItem podcastItem = item.getValue();
            Enclosure enclosure = podcastItem.getEnclosure();
            String url = null;
            String type = null;
            int length = 0;

            if (enclosure != null) {
                url = enclosure.getUrl().toString();
                type = enclosure.getType();
                length = enclosure.getLength();

            }

            addChannelItem
                    (
                            database,
                            idPodcast,
                            item.getKey(),
                            podcastItem.getTitle(),
                            podcastItem.getDescription(),
                            podcastItem.getPubDate().toString(),
                            url,
                            type,
                            length
                    );
        }
    }

    public boolean deleteChannelBy(SQLiteDatabase database, String address) {
        return database.delete(CHANNEL_TABLE_NAME, URL + "=" + "'" + address + "'", null) > 0;
    }

    public List<Podcast> fetchAllChannels(SQLiteDatabase database) {
        List<Podcast> result = new ArrayList<Podcast>();

        Cursor channelCursor = fetchAll(database);

        boolean puedeContinuar = channelCursor.moveToFirst();

        while (puedeContinuar) {
            PodcastFromDataBase aux = new PodcastFromDataBase();
            aux.setTitle(channelCursor.getString(channelCursor.getColumnIndexOrThrow(TITLE)));
            aux.setLink(channelCursor.getString(channelCursor.getColumnIndexOrThrow(LINK)));
            aux.setDescription(channelCursor.getString(channelCursor.getColumnIndexOrThrow(DESCRIPTION)));
            aux.setUrl(channelCursor.getString(channelCursor.getColumnIndexOrThrow(URL)));


            result.add(aux);
            puedeContinuar = channelCursor.moveToNext();
        }
        channelCursor.close();

        return result;
    }

    private Cursor fetchAll(SQLiteDatabase database) {
        return database.query
                (
                        CHANNEL_TABLE_NAME,
                        new String[]
                                {
                                        getIdChannelFieldName(),
                                        TITLE,
                                        URL,
                                        LINK,
                                        DESCRIPTION
                                },
                        null,
                        null,
                        null,
                        null,
                        null
                );
    }

    public boolean isChannelSaved(SQLiteDatabase database, String address) {
        Cursor channelCursor = fetchChannelByUrl(database, address);
        boolean result = channelCursor.moveToFirst();
        channelCursor.close();
        return result;
    }

    public Map<PodcastType, Integer> getPodcastTypeCounterByUrl
            (
                    SQLiteDatabase database,
                    String url
            ) {
        Map<PodcastType, Integer> result = new HashMap<PodcastType, Integer>();

        Cursor channelCursor = fetchChannelByUrl(database, url);
        channelCursor.moveToFirst();

        int channelId = channelCursor.getInt(channelCursor.getColumnIndexOrThrow(getIdChannelFieldName()));

        Cursor counterCursor = null;


        counterCursor = fetchPodcastCounterByEnclosureTypeAndChannelId(database, channelId, "audio");
        int audioCounter = 0;
        if (counterCursor.moveToFirst()) {
            audioCounter = counterCursor.getInt(0);
        }
        result.put(PodcastType.Audio, audioCounter);
        counterCursor.close();

        counterCursor = fetchPodcastCounterByEnclosureTypeAndChannelId(database, channelId, "video");
        int videoCounter = 0;
        if (counterCursor.moveToFirst()) {
            videoCounter = counterCursor.getInt(0);
        }
        result.put(PodcastType.Video, videoCounter);
        counterCursor.close();

        counterCursor = fetchPodcastCounterByChannelId(database, channelId);
        int allPodcastCounter = 0;
        if (counterCursor.moveToFirst()) {
            allPodcastCounter = counterCursor.getInt(0);
        }
        result.put(PodcastType.Other, allPodcastCounter - audioCounter - videoCounter);
        channelCursor.close();
        counterCursor.close();
        return result;
    }

    private Cursor fetchPodcastCounterByChannelId(SQLiteDatabase database, int channelId) {
        return database.query
                (
                        CHANNELITEM_TABLE_NAME,
                        new String[]
                                {
                                        "COUNT(*)"
                                },
                        (
                                getIdChannelFieldName() + "=" + "" + channelId + ""
                        ),
                        null,
                        getIdChannelFieldName(),
                        null,
                        null
                );
    }

    private Cursor fetchPodcastCounterByEnclosureTypeAndChannelId(SQLiteDatabase database, int channelId, String enclosureType) {
        return database.query
                (
                        CHANNELITEM_TABLE_NAME,
                        new String[]
                                {
                                        "COUNT(*)"
                                },
                        (
                                getIdChannelFieldName() + "=" + "" + channelId + "" + " AND " +
                                        TYPE + " LIKE " + "'" + enclosureType + "%'" + " "
                        ),
                        null,
                        TYPE,
                        null,
                        null
                );
    }

    public void updatePodcastItemLength(SQLiteDatabase database, PodcastItem item) {
        ContentValues values = new ContentValues();
        values.put(LENGTH, item.getEnclosure().getLength());
        database.update
                (
                        CHANNELITEM_TABLE_NAME,
                        values,
                        MessageFormat.format("{0} = ''{1}''", TITLE, item.getTitle()),
                        null
                );

    }


}
