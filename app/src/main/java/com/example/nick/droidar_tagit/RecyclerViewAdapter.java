package com.example.nick.droidar_tagit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.List;

import util.IO;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {

    private List<Tagpost> tagpostList;
    private View.OnLongClickListener longClickListener;
    private View.OnClickListener onClickListener;

    public RecyclerViewAdapter(List<Tagpost> tagpostList, View.OnLongClickListener longClickListener, View.OnClickListener onClickListener ) {
        this.tagpostList = tagpostList;
        this.longClickListener = longClickListener;
        this.onClickListener = onClickListener;
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
        if(type.equals("type1")) {
            holder.imageView.setImageBitmap(IO.loadBitmapFromFile(tagString));
        }
        else {
            Bitmap tempBitmap = StringToBitMap(tagString);
            holder.imageView.setImageBitmap(tempBitmap);
        }

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

    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }
}
