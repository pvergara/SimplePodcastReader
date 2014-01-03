package com.ecos.SimplePodcastReader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class PodcastDescriptionActivity extends Activity {

    public static final String DESCRIPTION = "description";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.podcast_description);
        WebView wv = (WebView) this.findViewById(R.id.description);

        Bundle bundle = getIntent().getExtras();

        String description = bundle != null ? bundle.getString(DESCRIPTION) : null;

        if (description != null) {
            description = description.replaceAll("#", "%23");
            description = description.replaceAll("%", "%25");
            description = description.replaceAll("\\?", "%27");
            description = "<html><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-16le\"><body>" + description + "</body></html>";
        }
        if (wv != null)
            wv.loadData(description, "text/html", "utf-8");

        if (description == null)
            Log.d(PodcastDescriptionActivity.class.toString(), "No se recuperó el text view o no hay descripción");
    }
}
