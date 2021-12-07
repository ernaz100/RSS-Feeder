package com.example.rssfeeder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FeedListAdapter<E> extends ArrayAdapter<Entry> {
    private final Context context;
    private final ArrayList<Entry> values;

    public FeedListAdapter(Context context, ArrayList<Entry> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }
    // Custom List Layout
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.mylistlayout, parent, false);

        TextView titleView = (TextView) rowView.findViewById(R.id.title);
        TextView descriptionView = (TextView) rowView.findViewById(R.id.description);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.label);
        // if image downloading worked convert the compressed image back to a Bitmap and display it else show dummy
        Bitmap bmp = null;
        if(values.get(position).imageByteArray != null) {
            bmp = BitmapFactory.decodeByteArray(values.get(position).imageByteArray, 0, values.get(position).imageByteArray.length);
        }
        if(bmp != null){
            imageView.setImageBitmap(bmp);
        }
        else{
            imageView.setImageResource(R.drawable.common_google_signin_btn_icon_dark);
        }
        // set title and description per list item
        titleView.setText(values.get(position).title);
        descriptionView.setText(values.get(position).summary);
        return rowView;
    }
}
