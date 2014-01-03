package com.ecos.SimplePodcastReader.util;

import android.util.Log;

import com.ecos.SimplePodcastReader.model.atom.AtomPodcastFeed;
import com.ecos.SimplePodcastReader.model.atom.Entry;
import com.ecos.SimplePodcastReader.model.generic.Enclosure;
import com.ecos.collections.BreadCrumbStack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AtomPodcastFeedHandler extends DefaultHandler {

    private static final String SUBTITLE = "subtitle";
    private static final String ALTERNATE = "alternate";
    private static final Object TITLE = "title";
    private static final String FEED = "feed";
    private static final String ID = "id";
    private static final String UPDATED = "updated";
    private static final String LOGO = "logo";
    private static final String ENTRY = "entry";
    private static final String CONTENT = "content";
    private static final String LINK = "link";
    private static final String LINK_REL = "rel";
    private static final String ENCLOSURE = "enclosure";
    private static final String TYPE = "type";
    private static final String LENGTH = "length";
    private static final String LINK_HREF = "href";
    private StringBuilder stringBuilder;
    private BreadCrumbStack bc;
    private AtomPodcastFeed atomPodcastFeed = new AtomPodcastFeed();
    private Entry entryAux;

    public AtomPodcastFeedHandler() {
        stringBuilder = new StringBuilder();
        bc = new BreadCrumbStack();
    }

    public AtomPodcastFeed getParsedData() {
        return this.atomPodcastFeed;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        bc.add(localName);

        if (bc.toString().equals(FEED + "/" + ENTRY)) {
            this.entryAux = new Entry();
        }

        if (bc.toString().equals(FEED + "/" + ENTRY + "/" + LINK)) {
            if (attributes.getValue(LINK_REL) != null
                    && attributes.getValue(LINK_REL).equals(ENCLOSURE)) {
                URL href = null;
                try {
                    href = new URL(attributes.getValue(LINK_HREF));
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Enclosure enclosure = new Enclosure();
                enclosure.setUrl(href);
                enclosure.setType(attributes.getValue(TYPE));
                enclosure.setLength(new Integer(attributes.getValue(LENGTH)));
                this.entryAux.setEnclosure(enclosure);
                updateEntry(this.entryAux.getId());
            }
        }

        if (bc.toString().equals(FEED + "/" + LINK)) {
            if (attributes.getValue(LINK_REL) != null
                    && attributes.getValue(LINK_REL).equals(ALTERNATE)) {
                atomPodcastFeed
                        .setLink(attributes.getValue(LINK_HREF));
            }
        }

    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        if (bc.toString().equals(FEED + "/" + TITLE)) {
            this.atomPodcastFeed.setTitle(stringBuilder.toString().trim());
        }

        if (bc.toString().equals(FEED + "/" + ID)) {
            this.atomPodcastFeed.setId(stringBuilder.toString().trim());
        }

        if (bc.toString().equals(FEED + "/" + LOGO)) {
            this.atomPodcastFeed.setLogo(stringBuilder.toString().trim());
        }

        if (bc.toString().equals(FEED + "/" + SUBTITLE)) {
            atomPodcastFeed.setSubTitle(stringBuilder.toString().trim());
        }

        if (bc.toString().equals(FEED + "/" + UPDATED)) {
            String aux = stringBuilder.toString().trim();
            this.atomPodcastFeed.setUpdated(getAtomDateOrDefault(aux));
        }

        if (bc.toString().equals(FEED + "/" + ENTRY + "/" + ID)) {
            this.entryAux.setId(stringBuilder.toString().trim());
            updateEntry(this.entryAux.getId());
        }

        if (bc.toString().equals(FEED + "/" + ENTRY + "/" + TITLE)) {
            this.entryAux.setTitle(stringBuilder.toString().trim());
            updateEntry(this.entryAux.getId());
        }

        if (bc.toString().equals(FEED + "/" + ENTRY + "/" + UPDATED)) {
            this.entryAux.setUpdated(getAtomDateOrDefault(stringBuilder
                    .toString().trim()));
            updateEntry(this.entryAux.getId());
        }

        if (bc.toString().equals(FEED + "/" + ENTRY + "/" + CONTENT)) {
            this.entryAux.setContent(stringBuilder.toString().trim());
            updateEntry(this.entryAux.getId());
        }

        bc.pop();
        stringBuilder = new StringBuilder();

    }

    private Date getAtomDateOrDefault(String aux) {
        Date result = new Date();
        try {
            result = parseAtomDate(aux);
        } catch (Exception e) {
            Log.e(AtomPodcastFeedHandler.class.toString(), e.toString());
        }
        return result;
    }

    private Date parseAtomDate(String aux) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(aux);
    }

    private void updateEntry(String entryId) {
        if (entryId != null)
            this.atomPodcastFeed.Entries.put(entryId, this.entryAux);
    }

    @Override
    public void characters(char ch[], int start, int length) {
        stringBuilder.append(ch, start, length);
    }

}
