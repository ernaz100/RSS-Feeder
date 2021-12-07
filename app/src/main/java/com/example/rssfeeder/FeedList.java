package com.example.rssfeeder;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class FeedList extends AppCompatActivity {
    ListView feedList;
    ArrayList<Entry> arrayList;
    FeedListAdapter<Entry> adapter;

    // Create List with Custom List Layout "FeedListAdapter"
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



        // On Item Click send User to Web View and save to Firebase
        feedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //Fetch title and description into Firebase ready Structure
                List<String> databaseEntry = new ArrayList<>();
                databaseEntry.add(arrayList.get(position).title);
                databaseEntry.add(arrayList.get(position).summary);

                // Write a title and description to specific userUID node in the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference();
                myRef.child("users").child(user.getUid()).push().setValue(databaseEntry);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(arrayList.get(position).link));
                startActivity(intent);
            }
        });

    }



}