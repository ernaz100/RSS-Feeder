package com.example.rssfeeder;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssactivity);
    }
    public void subscribeToFeed(View v)  {
        Log.d("tag" ,"subscribed");
        String url = ((EditText) findViewById(R.id.rssFeed)).getText().toString();
        String limit = ((EditText) findViewById(R.id.recentItemAmount)).getText().toString();
        String[] params = {url, limit};
        DownloadXmlTask loader = new DownloadXmlTask(this);
        loader.execute(params);

    }
}




