package com.example.rssfeeder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class FeedList extends AppCompatActivity {
    ListView feedList;
    ArrayList<Entry> arrayList;
    FeedListAdapter<Entry> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_list);
        arrayList = new ArrayList<>();
        feedList = findViewById(R.id.list);

        Intent i = getIntent();
        List<Entry> list = (List<Entry>) i.getSerializableExtra("items");
        for (Entry entry : list) {
           arrayList.add(entry);
        }
        adapter =  new FeedListAdapter<Entry>(getApplicationContext(), arrayList);
        feedList.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        feedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(arrayList.get(position).link));
                startActivity(intent);
            }
        });

    }

}