package com.example.nick.droidar_tagit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.squareup.picasso.Picasso;

import java.util.List;

import util.Log;

/*
 * When our data changes in our model, change the view
 */
public class ImageResultArrayAdapter extends ArrayAdapter<ImageResult> {
	private List<ImageResult> images;

	public ImageResultArrayAdapter(Context context, List<ImageResult> images) {
		super(context, R.layout.item_image_result, images);
		this.images = images;
	}

	@Override
	@NonNull
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		ImageResult imageInfo = this.getItem(position);
		/*
		 * TODO: for tony read up on SmartImageView
		 */
		ImageView ivImage;
		String fullUrlString = imageInfo.getThumbUrl();
		String thumbUrlString = imageInfo.getFullUrl();

		// We don't have a view to re-use, inflate it from layout
		if (convertView == null) {
			LayoutInflater inflator = LayoutInflater.from(getContext());
			ivImage = (ImageView) inflator.inflate(
					R.layout.item_image_result,
					parent,
					false
			);
		} else {
			/*
			 * re-use the previous views to save resources, should be much better for memory
			 */
			ivImage = (ImageView) convertView;
			ivImage.setImageResource(android.R.color.transparent);
		}

		Picasso.get().load(imageInfo.getThumbUrl()).into(ivImage);
		Log.d("customSearch", "setimageBitmap called on thumburl: " + imageInfo.getThumbUrl());

		//TODO need better way to handle null urls.  perhaps view should never be inflated?

		return ivImage;
	}
}