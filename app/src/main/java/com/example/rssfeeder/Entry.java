package com.example.rssfeeder;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class Entry implements Serializable {
    public final String title;
    public final String summary;
    public final String link;
    public final byte[] imageByteArray;
    public final String pubDate;

    Entry(String title, String summary, String link, byte[] imageByteArray,String pubDate) {
        this.title = title;
        this.summary = summary;
        this.link = link;
        this.imageByteArray = imageByteArray;
        this.pubDate = pubDate;
    }
}
