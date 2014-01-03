package com.ecos.SimplePodcastReader.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ecos.SimplePodcastReader.model.atom.AtomPodcastFeed;
import com.ecos.SimplePodcastReader.model.generic.Podcast;
import com.ecos.SimplePodcastReader.model.rss2.Rss2PodcastFeed;
import com.ecos.helpers.DBManagerHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class PodcastReader {
    public static final String SIMPLE_PODCAST_READER_RSS_ATOM = "Simple Podcast Reader RSS/Atom";
    private static Rss2PodcastFeed rss2PodcastFeed;
    private static AtomPodcastFeed atomPodcastFeed;
    private XMLReader xmlReader;

    private XMLReader setupAndGetXMLReader()
            throws ParserConfigurationException, SAXException,
            FactoryConfigurationError {
        SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        return saxParser.getXMLReader();
    }

    public PodcastReader() {
        try {
            xmlReader = setupAndGetXMLReader();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Rss2PodcastFeed getRss2PodcastFeed(String address, boolean updateFeedDataSet)
            throws
            IOException,
            SAXException, URISyntaxException {
        if (xmlReader != null) {
            if (rss2PodcastFeed == null || updateFeedDataSet) {
                Rss2PodcastFeedHandler rss2PodcastHandler = new Rss2PodcastFeedHandler();
                xmlReader.setContentHandler(rss2PodcastHandler);
                prepareAndDownloadPodcast(address);
                rss2PodcastFeed = rss2PodcastHandler.getParsedData();
            }
        }
        return rss2PodcastFeed;
    }

    public AtomPodcastFeed getAtomPodcastFeed(String address, boolean updateFeedDataSet)
            throws
            IOException,
            SAXException,
            URISyntaxException {
        if (xmlReader != null) {
            if (atomPodcastFeed == null || updateFeedDataSet) {
                AtomPodcastFeedHandler atomPodcastHandler = new AtomPodcastFeedHandler();
                xmlReader.setContentHandler(atomPodcastHandler);
                prepareAndDownloadPodcast(address);
                atomPodcastFeed = atomPodcastHandler.getParsedData();

            }
        }

        return atomPodcastFeed;
    }

    private void prepareAndDownloadPodcast(String address)
            throws URISyntaxException, IOException,
            SAXException {
        HttpGet httpget;
        httpget = new HttpGet(new URI(address));
        httpget.setHeader("User-Agent", SIMPLE_PODCAST_READER_RSS_ATOM);
        httpget.setHeader("Content-Type", "application/xml");

        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(httpget);
        HttpEntity entity = response.getEntity();

        InputSource input = new InputSource(entity.getContent());
        xmlReader.parse(input);
    }

    public Podcast getPodcast(String url) throws IOException, SAXException, URISyntaxException {
        Podcast podcast = getRss2PodcastFeed(url, true);

        if (podcast == null || podcast.getTitle() == null || podcast.getTitle().equals(""))
            podcast = getAtomPodcastFeed(url, true);

        return podcast;

    }

    public Podcast getPodcastFromDatabase(String address, Context context, boolean updateFeedsFromDatabase) throws IllegalArgumentException, IOException, SAXException, URISyntaxException {
        DBManagerHelper dbManagerHelper = new DBManagerHelper(context);
        SQLiteDatabase database = dbManagerHelper.getWritableDatabase();
        if (updateFeedsFromDatabase)
            dbManagerHelper.deleteChannelBy(database, address);


        Podcast podcast;
        podcast = dbManagerHelper.getChannelBy(database, address);


        if (podcast == null) {
            podcast = getPodcast(address);

            dbManagerHelper.createNewPodcast(address, podcast, database);

            podcast = dbManagerHelper.getChannelBy(database, address);
        }
        assert database != null;
        database.close();

        return podcast;
    }

}

