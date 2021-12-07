package com.example.rssfeeder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class CheckForUpdateService extends Service {
    public CheckForUpdateService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags,int id){
        String[] params = {RSSActivity.subscribedUrl, RSSActivity.numberOfPosts};
        DownloadXmlTask loader = new DownloadXmlTask(this);
        loader.execute(params);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }
}