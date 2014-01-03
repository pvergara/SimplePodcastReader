package com.ecos.helpers;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class FileHelper {

    public static Integer getLengthByUrl(URL url) throws IOException {
        //--------------------------------------------------
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        return urlConnection.getContentLength();
    }

}
