package com.example.nick.droidar_tagit;

/**
 * Created by nick on 1/3/2018.
 *
 *
 */

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class NameViewModel extends ViewModel {

    private String TAG = NameViewModel.class.getSimpleName();

    // Create a LiveData with a String
    private MutableLiveData<List<List<String>>> uriPathList;

    public MutableLiveData<List<List<String>>> getUriPathList() {
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
            List<List<String>> masterList = new ArrayList<>();
            List<String> uriPathStringList = new ArrayList<>();
            uriPathStringList.add("/storage/emulated/0/DCIM/100MEDIA/IMAG0181.jpg");
            uriPathStringList.add("type1");
            masterList.add(uriPathStringList);

            uriPathList.setValue(masterList);
        }, 5000);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "on cleared called");
    }

}
