package com.ecos.SimplePodcastReader.util;

import android.util.Log;

import com.ecos.SimplePodcastReader.model.generic.Enclosure;
import com.ecos.SimplePodcastReader.model.rss2.Item;
import com.ecos.SimplePodcastReader.model.rss2.Rss2PodcastFeed;
import com.ecos.collections.BreadCrumbStack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

public class Rss2PodcastFeedHandler extends DefaultHandler {

    public Rss2PodcastFeedHandler() {
        stringBuilder = new StringBuilder();
        bc = new BreadCrumbStack();
    }

    private static final String RSS = "rss";
    private static final String CHANNEL = "channel";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String LINK = "link";

    private static final String ITEM = "item";
    private static final String GUID = "guid";
    private static final String PUBLISH_DATE = "pubDate";
    private static final Object ENCLOSURE = "enclosure";
    private static final String URL = "url";
    private static final String TYPE = "type";
    private static final String LENGTH = "length";

    private Rss2PodcastFeed rss2PodcastFeed = new Rss2PodcastFeed();
    private String actualItemGuid = null;
    private String actualItemTitle = null;
    private Date actualItemPubDate = null;
    private String actualItemDescription = null;
    private URL actualItemEnclosureUri = null;
    private String actualItemEnclosureType = null;
    private Integer actualItemEnclosureLength = null;
    private StringBuilder stringBuilder;

    private BreadCrumbStack bc;

    public Rss2PodcastFeed getParsedData() {
        return this.rss2PodcastFeed;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        bc.add(localName);

        if (bc.toString().equals(RSS + "/" + CHANNEL + "/" + ITEM + "/" + ENCLOSURE)) {
            URL aux = null;
            try {
                aux = new URL(attributes.getValue(URL));
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.actualItemEnclosureUri = aux;
            this.actualItemEnclosureType = attributes.getValue(TYPE);
            try {
                this.actualItemEnclosureLength = getLength(attributes);
            } catch (IOException e) {
                this.actualItemEnclosureLength = 0;
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            updateItemData();
        }
    }

    private Integer getLength(Attributes attributes) throws IOException {
        Integer result = 0;
        if (attributes.getValue(LENGTH) != null) {
            result = new Integer(attributes.getValue(LENGTH));
//			if(result <2)
//			{
//				if(this.actualItemEnclosureUri!=null)
//					result = FileHelper.getLengthByUrl(this.actualItemEnclosureUri);
//			}
        }
//		else
//		{
//			if(this.actualItemEnclosureUri!=null)
//				result = FileHelper.getLengthByUrl(this.actualItemEnclosureUri);
//		}
        return result;
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        if (bc.toString().equals(RSS + "/" + CHANNEL + "/" + TITLE) && namespaceURI.equals(""))
            rss2PodcastFeed.setTitle(stringBuilder.toString().trim().trim());

        if (bc.toString().equals(RSS + "/" + CHANNEL + "/" + LINK) && namespaceURI.equals(""))
            rss2PodcastFeed.setLink(stringBuilder.toString().trim());

        if (bc.toString().equals(RSS + "/" + CHANNEL + "/" + DESCRIPTION) && namespaceURI.equals(""))
            rss2PodcastFeed.setDescription(stringBuilder.toString().trim());

        if (bc.toString().equals(RSS + "/" + CHANNEL + "/" + ITEM)) {
            Log.d(Rss2PodcastFeedHandler.class.toString(), bc.toString());
            if (this.actualItemGuid == null) {
                Log.d(Rss2PodcastFeedHandler.class.toString(), this.actualItemEnclosureType == null ? "null" : this.actualItemEnclosureType);
                Log.d(Rss2PodcastFeedHandler.class.toString(), this.actualItemEnclosureUri == null ? "null" : this.actualItemEnclosureUri.toString());
                Log.d(Rss2PodcastFeedHandler.class.toString(), this.actualItemEnclosureLength == null ? "null" : this.actualItemEnclosureLength.toString());
                this.actualItemGuid = UUID.randomUUID().toString();
                addNewItem(this.actualItemGuid);
                updateItemData();
            }

            this.actualItemGuid = null;
            this.actualItemTitle = null;
            this.actualItemPubDate = null;
            this.actualItemDescription = null;
            this.actualItemEnclosureUri = null;
            this.actualItemEnclosureType = null;
            this.actualItemEnclosureLength = null;
        }

        if (bc.toString().equals(RSS + "/" + CHANNEL + "/" + ITEM + "/" + DESCRIPTION)) {
            this.actualItemDescription = stringBuilder.toString().trim();
            updateItemData();
        }

        if (bc.toString().equals(RSS + "/" + CHANNEL + "/" + ITEM + "/" + PUBLISH_DATE)) {
            String aux = stringBuilder.toString();
            try {
                this.actualItemPubDate = new Date(aux);
            } catch (Exception e) {
                this.actualItemPubDate = new Date();
                Log.e(Rss2PodcastFeedHandler.class.toString(), e.toString());
            }
            updateItemData();

        }

        if (bc.toString().equals(RSS + "/" + CHANNEL + "/" + ITEM + "/" + GUID)) {
            this.actualItemGuid = stringBuilder.toString();
            addNewItem(this.actualItemGuid);
            updateItemData();

        }

        if (bc.toString().equals(RSS + "/" + CHANNEL + "/" + ITEM + "/" + TITLE)) {
            this.actualItemTitle = stringBuilder.toString();
            updateItemData();
        }

        bc.pop();
        stringBuilder = new StringBuilder();

    }

    @Override
    public void characters(char ch[], int start, int length) {
        stringBuilder.append(ch, start, length);
    }

    private void updateItemData() {
        Item itemAux = (Item) this.rss2PodcastFeed.Items.get(this.actualItemGuid);
        if (itemAux != null) {
            if (this.actualItemTitle != null)
                itemAux.setTitle(this.actualItemTitle.trim());

            if (this.actualItemPubDate != null)
                itemAux.setPubDate(this.actualItemPubDate);

            if (this.actualItemDescription != null)
                itemAux.setDescription(this.actualItemDescription);

            if (this.actualItemEnclosureLength != null
                    || this.actualItemEnclosureType != null
                    || this.actualItemEnclosureUri != null) {
                Enclosure enclosureAux = new Enclosure();

                if (this.actualItemEnclosureLength != null)
                    enclosureAux.setLength(actualItemEnclosureLength);

                if (this.actualItemEnclosureType != null)
                    enclosureAux.setType(actualItemEnclosureType);

                if (this.actualItemEnclosureUri != null)
                    enclosureAux.setUrl(actualItemEnclosureUri);

                // Un enclosure no tiene sentido si no lleva URI (el resto)
                // PUEDE ser opcional...
                // pero sin enlace no hay nada
                // TODO: REVISAR POSIBLE PROBLEMA DE SEGURIDAD (URI's que
                // apuntan a direcciones "peligrosas")
                if (this.actualItemEnclosureType != null)
                    itemAux.setEnclosure(enclosureAux);
            }
        }

    }

    private void addNewItem(String guid) {
        rss2PodcastFeed.Items.put(guid, new Item());
    }

}
