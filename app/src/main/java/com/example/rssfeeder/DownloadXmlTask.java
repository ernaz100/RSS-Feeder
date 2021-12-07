package com.example.rssfeeder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.util.Xml;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class DownloadXmlTask extends AsyncTask<String, Void, List<Entry>> {
    private static final String ns = null;
    private Context context;
    private int declaredListLength = 0;
    private List<Entry> res;
    public DownloadXmlTask(Context context){
        this.context=context;
    }
    // Create a List of Entry Objects out of the RSS Feed that was parsed in
    @Override
    protected List<Entry> doInBackground(String... urls) {
        declaredListLength = Integer.parseInt(urls[1]);
        try {
            InputStream xmlInput = null;
            List<Entry> parsedXML;
            try {
                xmlInput = downloadUrl(urls[0]);
                parsedXML = parse(xmlInput);
            } finally {
                if (xmlInput != null) {
                    xmlInput.close();
                }
            }
            return parsedXML;
        } catch (Exception e){
            System.out.println("Exc=" + e);
            return null;
        }
    }
    // Send the Created List of Item Objects via intent to the List Activity if called from RSSActivity
    // If started from Service check if there is a new Article and if yes send Notification.
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onPostExecute(List<Entry> result) {
        Log.d("tag", "Finished Parsing RSS");

        if(result == null){
            Log.d("tag", "Failed to Load rss");
            return;
        }
        res = result;

        if("RSSActivity".equals(context.getClass().getSimpleName())){
            // Write latest pubDate of item to a file to be able to know when there is a new article
            writeToFile(result.get(0).pubDate, context);

            Intent intent = new Intent(context,FeedList.class);
            intent.putExtra("items", (Serializable) result);
            context.startActivity(intent);
        }
        else{
            try {
                compareDatesOfNewestArticle(result.get(0).pubDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void compareDatesOfNewestArticle(String pubDate) throws ParseException {
        DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        Date date = formatter.parse(pubDate);
        Date savedDate = formatter.parse(readLatestDateFromFile(context));
        if (date.after(savedDate)){
            Log.d("tag", "New Article!");
            createNotification();
        }
        else{
            Log.d("tag", "UPTODATE");
        }


    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }
    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }
    // go into rss tag return List of item Objects
    private List<Entry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Entry> entries = new ArrayList<>();
        parser.require(XmlPullParser.START_TAG, ns, "rss");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("channel")) {
                entries = readChannel(parser);
            } else {
                skip(parser);
            }
        }
        return entries;
    }
    // go into channel tag return List of item Objects of the declared length
    private List<Entry> readChannel(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "channel");
        List<Entry> entries = new ArrayList();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG || declaredListLength == 0) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the item tag
            if (name.equals("item")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }
    // Parses the contents of an item. If it encounters a title, description, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private Entry readEntry(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "item");
        String title = null;
        String description = null;
        String link = null;
        byte[] image = null;
        String pubDate = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                title = readTitle(parser);
                declaredListLength--;
            } else if (name.equals("description")) {
                description = readDescription(parser);
            } else if(name.equals("link")){
                link = readLink(parser);
            } else if(name.equals("pubDate")){
                pubDate = readPubDate(parser);
            } else if(name.equals("enclosure")){
                image = readImageEnclosure(parser);
                parser.next();
            }
            else if(name.equals("media:content")){
                image = readImageMediaContent(parser);
                parser.next();
            }
            else {
                skip(parser);
            }
        }
        return new Entry(title, description, link, image,pubDate);
    }

    private byte[] readImageMediaContent(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "media:content");
        String imageURL = parser.getAttributeValue(null, "url");
        InputStream is = (InputStream) new URL(imageURL).getContent();
        Bitmap b = BitmapFactory.decodeStream(is);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 0, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    private byte[] readImageEnclosure(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "enclosure");
        String imageURL = parser.getAttributeValue(null, "url");
        InputStream is = (InputStream) new URL(imageURL).getContent();
        Bitmap b = BitmapFactory.decodeStream(is);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 0, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    private String readPubDate(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "pubDate");
        String date = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "pubDate");
        return date;
    }

    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "link");
        String link = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }

    // Processes title tags in the feed.
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return title;
    }

    // Processes description tags in the feed.
    private String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String summary = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        return summary;
    }

    // Extracts the text value of the tags.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }


    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
    private void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("latestdate.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


    private String readLatestDateFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("latestdate.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("tag", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("tag", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public void createNotification() {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("10002",
                    "CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("CHANNEL_DESCRIPTION");
            notificationManager.createNotificationChannel(channel);
        }

        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(context,FeedList.class);
        intent.putExtra("items", (Serializable) res);
        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);

        // Build notification
        NotificationCompat.Builder noti = new NotificationCompat.Builder(context,"10002")
                .setContentTitle("Fresh News available from " + RSSActivity.subscribedUrl)
                .setContentText(res.get(0).title).setSmallIcon(R.drawable.ic_launcher_background)
                .setAutoCancel(true)//hide notification after it is selected
                .setContentIntent(pIntent);

        notificationManager.notify(0, noti.build());

    }
    
}