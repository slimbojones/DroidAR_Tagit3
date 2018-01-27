package com.example.nick.droidar_tagit;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

import util.IO;
import util.Log;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {

    private List<Tagpost> tagpostList;
    private View.OnLongClickListener longClickListener;
    private View.OnClickListener onClickListener;
    private Context context;


    public RecyclerViewAdapter(List<Tagpost> tagpostList, View.OnLongClickListener longClickListener, View.OnClickListener onClickListener, Context context ) {
        this.tagpostList = tagpostList;
        this.longClickListener = longClickListener;
        this.onClickListener = onClickListener;
        this.context = context;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mylist, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, int position) {
        Tagpost tagpost = tagpostList.get(position);
        String tagString = tagpost.getitemString();
        String type = tagpost.gettagType();

        Bitmap tempBitmap;


        if(type.equals("type1")) {
            tempBitmap = IO.loadBitmapFromFile(tagString, 8);
            holder.imageView.setImageBitmap(tempBitmap);

        }
        else if(type.equals("type2")) {
            TextView v = new TextView(context);
            v.setTypeface(null, Typeface.BOLD);
            v.setText(tagString);
            v.setShadowLayer(0.01f, -2, 2,  Color.BLACK);
            tempBitmap = IO.loadBitmapFromView(v);
            holder.imageView.setImageBitmap(tempBitmap);

        }
        //load bitmap from url
        else {
            Uri myURI = Uri.parse(tagString);
            Picasso.with(context).load(myURI).into(holder.imageView);
            //tempBitmap = IO.loadBitmapFromURL(tagString);
            Log.d("imageURL", "RVA tagstring: " + tagString);
        }

        //tempBitmap.recycle();
        holder.itemView.setTag(tagpost);
        holder.itemView.setOnLongClickListener(longClickListener);
        holder.itemView.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return tagpostList.size();
    }

    public void addItems(List<Tagpost> tagpostList) {
        this.tagpostList = tagpostList;
        notifyDataSetChanged();
    }

    static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        RecyclerViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.icon);
        }
    }

}
