package com.example.nick.droidar_tagit;

/*
 * Created by nick on 1/3/2018.
 */

import android.app.Application;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class NameViewModel extends AndroidViewModel {

	private String TAG = NameViewModel.class.getSimpleName();
	private AppDatabase appDatabase;

	// Create a LiveData with a String
	private LiveData<List<Tagpost>> uriPathList;

	public NameViewModel(Application application) {
		super(application);
		appDatabase = AppDatabase.getDatabase(this.getApplication());
		uriPathList = appDatabase.itemAndPersonModel().getAllTagposts();
	}

	public LiveData<List<Tagpost>> getUriPathList() {
		if (uriPathList == null) {
			uriPathList = new MutableLiveData<>();
			loadUriPaths();
		}
		return uriPathList;
	}

	private void loadUriPaths() {
		// do async operation to fetch users
		Handler myHandler = new Handler();
		myHandler.postDelayed(() -> {
			Tagpost uriPathStringList = new Tagpost("/storage/emulated/0/DCIM/100MEDIA/IMAG0181.jpg", "type1", 123);
			appDatabase.itemAndPersonModel().addTagpost(uriPathStringList);
			//uriPathList = appDatabase.itemAndPersonModel().getAllTagposts();
		}, 5000);
	}

	public void deleteTagpost(Tagpost tagpost) {
		new deleteAsyncTask(appDatabase).execute(tagpost);
	}

	public void addTagpost(Tagpost tagpost) {
		new addAsyncTask(appDatabase).execute(tagpost);
	}

	public void deleteAllTagposts() {
		new NameViewModel.deleteAllTagpostsAsyncTask(appDatabase).execute();
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		Log.d(TAG, "on cleared called");
	}

	private static class deleteAsyncTask extends AsyncTask<Tagpost, Void, Void> {
		private AppDatabase db;

		deleteAsyncTask(AppDatabase appDatabase) {
			db = appDatabase;
		}

		@Override
		protected Void doInBackground(final Tagpost... params) {
			db.itemAndPersonModel().deleteTagpost(params[0]);
			return null;
		}
	}

	private static class addAsyncTask extends AsyncTask<Tagpost, Void, Void> {
		private AppDatabase db;

		addAsyncTask(AppDatabase appDatabase) {
			db = appDatabase;
		}

		@Override
		protected Void doInBackground(final Tagpost... params) {
			db.itemAndPersonModel().addTagpost(params[0]);
			return null;
		}
	}

	private static class deleteAllTagpostsAsyncTask extends AsyncTask<Tagpost, Void, Void> {
		private AppDatabase db;

		deleteAllTagpostsAsyncTask(AppDatabase appDatabase) {
			db = appDatabase;
		}

		@Override
		protected Void doInBackground(final Tagpost... params) {
			db.itemAndPersonModel().deleteAllTagposts();
			return null;
		}
	}
}
