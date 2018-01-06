package com.example.nick.droidar_tagit;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import util.IO;

public class CustomListAdapter extends ArrayAdapter<List<String>> {

    private final Activity context;
    private final List<List<String>> itemname;

    public CustomListAdapter(Activity context, List<List<String>> itemname) {
        super(context, R.layout.mylist, itemname);

        this.context=context;
        this.itemname=itemname;
    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.mylist, null,true);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        String type;
        type = itemname.get(position).get(1);

        if(type.equals("type1")) {
            imageView.setImageBitmap(IO.loadBitmapFromFile(itemname.get(position).get(0)));
        }
        else {
            Bitmap tempBitmap = StringToBitMap(itemname.get(position).get(0));
            imageView.setImageBitmap(tempBitmap);
        }
        return rowView;

    };

    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

}