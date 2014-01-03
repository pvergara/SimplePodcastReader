package com.ecos.helpers;

import android.os.Environment;

import java.io.File;

public class DirectoryHelper {

    private static final String PODCAST = "Podcast";

    public static String getPodcastFolder() {
        String result;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File file = Environment.getExternalStorageDirectory();
            StringBuilder sb = new StringBuilder();
            sb.append(file.getAbsolutePath());
            sb.append(File.separator);
            sb.append(PODCAST);
            sb.append(File.separator);
            String path = sb.toString();
            File postDir = new File(path);
            if (!postDir.isDirectory())
                postDir.mkdir();
            result = postDir.getAbsolutePath();
        } else {
            result = "";
        }
        return result;
    }

}
