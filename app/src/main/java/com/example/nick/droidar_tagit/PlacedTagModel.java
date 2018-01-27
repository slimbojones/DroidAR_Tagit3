package com.example.nick.droidar_tagit;

/**
 * Created by nick on 1/3/2018.
 *
 *
 */

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PlacedTagModel extends AndroidViewModel {

    private String TAG = PlacedTagModel.class.getSimpleName();
    private AppDatabase appDatabase;

    // Create a LiveData with a String
    private LiveData<List<PlacedTag>> placedTagList;

    public PlacedTagModel(Application application) {
        super(application);

        appDatabase = AppDatabase.getDatabase(this.getApplication());

        //appDatabase.placedTagModel().deleteAllPlacedTags();

        Log.d("PlacedTag", "right before");
        placedTagList = appDatabase.placedTagModel().getPlacedTags();

    }



    public LiveData<List<PlacedTag>> getPlacedTagList() {
        Log.d("PlacedTag", "getPlacedTagList called in PlacedTagwModel");
        if (placedTagList == null) {
            placedTagList = new MutableLiveData<>();
            loadPlacedTags();
        }
        return placedTagList;
    }
    private void loadPlacedTags() {
        Log.d("PlacedTag", "loadPlacedTags started in PlacedTagwModel");
        // do async operation to fetch users
        Handler myHandler = new Handler();
        myHandler.postDelayed(() -> {

            Bitmap icon = BitmapFactory.decodeResource(getApplication().getResources(),
                    R.drawable.t_icon);

            String iconString = "first setup";

            PlacedTag newPlacedTag = new PlacedTag(iconString,"type2", 0.0f,0.0f, 0.0f);
            appDatabase.placedTagModel().addPlacedTag(newPlacedTag);

            Log.d("PlacedTag", "newPlacedTag added in PlacedTagwModel");

            //placedTagList = appDatabase.placedTagModel().getPlacedTags();
        }, 5000);
    }

    public void deletePlacedTag(PlacedTag placedTag) {
        new deletePTAsyncTask(appDatabase).execute(placedTag);
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

    public void addPlacedTag(PlacedTag placedTag) {
        new addPTAsyncTask(appDatabase).execute(placedTag);
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

    public void deleteAllPlacedTags() {
        new deleteAllPTAsyncTask(appDatabase).execute();
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

    public void deletePlacedTagById(int placedTagId) {
        new deleteByIdAsyncTask(appDatabase).execute(placedTagId);
    }

    private static class deleteByIdAsyncTask extends AsyncTask<Integer , Void, Void> {
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

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "on cleared called");
    }

}