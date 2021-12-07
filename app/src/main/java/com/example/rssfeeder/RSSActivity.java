package com.example.rssfeeder;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RSSActivity extends AppCompatActivity {
    public static String subscribedUrl;
    public static String numberOfPosts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssactivity);
    }

    // on Subscribe Click, start an AsyncTask to parse the entered XML via the "DownloadXmlTask" class
    // also Start the Service to Check for updates
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void subscribeToFeed(View v)  {
        Log.d("tag" ,"subscribed");
        String url = ((EditText) findViewById(R.id.rssFeed)).getText().toString();
        String limit = ((EditText) findViewById(R.id.recentItemAmount)).getText().toString();
        subscribedUrl = url;
        numberOfPosts = limit;
        String[] params = {subscribedUrl, numberOfPosts};

        Util.scheduleJob(getApplicationContext());

        DownloadXmlTask loader = new DownloadXmlTask(this);
        loader.execute(params);

    }
}




