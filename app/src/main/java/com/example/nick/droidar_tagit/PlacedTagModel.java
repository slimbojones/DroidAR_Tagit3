package com.example.nick.droidar_tagit;

/*
 * Created by nick on 1/3/2018.
 */

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class PlacedTagModel extends AndroidViewModel {

	private String LOG_TAG = PlacedTagModel.class.getSimpleName();
	private AppDatabase appDatabase;

	// Create a LiveData with a String
	private LiveData<List<PlacedTag>> placedTagList;

	public PlacedTagModel(Application application) {
		super(application);

		appDatabase = AppDatabase.getDatabase(this.getApplication());

		//appDatabase.placedTagModel().deleteAllPlacedTags();

		Log.d(LOG_TAG, "right before");
		placedTagList = appDatabase.placedTagModel().getPlacedTags();
	}

	public LiveData<List<PlacedTag>> getPlacedTagList() {
		Log.d(LOG_TAG, "getPlacedTagList called in PlacedTagModel");
		if (placedTagList == null) {
			placedTagList = new MutableLiveData<>();
			loadPlacedTags();
		}
		return placedTagList;
	}

	private void loadPlacedTags() {
		Log.d(LOG_TAG, "loadPlacedTags started in PlacedTagModel");
		// do async operation to fetch users
		Handler myHandler = new Handler();
		myHandler.postDelayed(() -> {

			Bitmap icon = BitmapFactory.decodeResource(getApplication().getResources(), R.drawable.t_icon);

			String iconString = "first setup";

			PlacedTag newPlacedTag = new PlacedTag(iconString, "type2", 0.0f, 0.0f, 0.0f);
			appDatabase.placedTagModel().addPlacedTag(newPlacedTag);

			Log.d(LOG_TAG, "newPlacedTag added in PlacedTagModel");

			//placedTagList = appDatabase.placedTagModel().getPlacedTags();
		}, 5000);
	}

	public void deletePlacedTag(PlacedTag placedTag) {
		new deletePTAsyncTask(appDatabase).execute(placedTag);
	}

	public void addPlacedTag(PlacedTag placedTag) {
		new addPTAsyncTask(appDatabase).execute(placedTag);
	}

	public void deleteAllPlacedTags() {
		new deleteAllPTAsyncTask(appDatabase).execute();
	}

	public void deletePlacedTagById(int placedTagId) {
		new deleteByIdAsyncTask(appDatabase).execute(placedTagId);
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		Log.d(LOG_TAG, "on cleared called");
	}

	private static class deletePTAsyncTask extends AsyncTask<PlacedTag, Void, Void> {
		private AppDatabase db;

		deletePTAsyncTask(AppDatabase appDatabase) {
			db = appDatabase;
		}

		@Override
		protected Void doInBackground(final PlacedTag... params) {
			db.placedTagModel().deletePlacedTag(params[0]);
			return null;
		}
	}

	private static class addPTAsyncTask extends AsyncTask<PlacedTag, Void, Void> {
		private AppDatabase db;

		addPTAsyncTask(AppDatabase appDatabase) {
			db = appDatabase;
		}

		@Override
		protected Void doInBackground(final PlacedTag... params) {
			db.placedTagModel().addPlacedTag(params[0]);
			return null;
		}
	}

	private static class deleteAllPTAsyncTask extends AsyncTask<PlacedTag, Void, Void> {
		private AppDatabase db;

		deleteAllPTAsyncTask(AppDatabase appDatabase) {
			db = appDatabase;
		}

		@Override
		protected Void doInBackground(final PlacedTag... params) {
			db.placedTagModel().deleteAllPlacedTags();
			return null;
		}
	}

	private static class deleteByIdAsyncTask extends AsyncTask<Integer, Void, Void> {
		private AppDatabase db;

		deleteByIdAsyncTask(AppDatabase appDatabase) {
			db = appDatabase;
		}

		@Override
		protected Void doInBackground(final Integer... params) {
			db.placedTagModel().deletePlacedTagById(params[0]);
			return null;
		}
	}
}