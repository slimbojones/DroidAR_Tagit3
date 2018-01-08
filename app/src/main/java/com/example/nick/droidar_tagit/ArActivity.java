package com.example.nick.droidar_tagit;

import android.app.Activity;
import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import system.Setup;
import util.IO;
import util.Log;

import static junit.framework.Assert.assertTrue;

/**
 * This is an example activity which demonstrates how to use a Setup object. It
 * wraps the Setup object and forwards all needed events to it.
 * 
 * @author Simon Heinen
 * 
 */
public class ArActivity extends LifecycleActivity {

	private static final String LOG_TAG = "ArActivity";
	private static Setup staticSetupHolder;
	private Setup mySetupToUse;
	private static int RESULT_LOAD_IMAGE = 1;
	private NameViewModel mModel;
	View DynamicListView;
	private StorageReference mStorage;

	/**
	 * Called when the activity is first created.
	 *
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(LOG_TAG, "main onCreate");
		if (staticSetupHolder != null) {
			mySetupToUse = staticSetupHolder;
			staticSetupHolder = null;
			runSetup();
		} else {
			Log.e(LOG_TAG, "There was no Setup specified to use. "
					+ "Please use ArActivity.show(..) when you "
					+ "want to use this way of starting the AR-view!");
			this.finish();
		}


		mModel = ViewModelProviders.of(this).get(NameViewModel.class);

		//DynamicListView = mySetupToUse.getGuiSetup().getLeftOuter().getChildAt(0);

		//mModel.getUriPathList().observe(this, thisUriPathList -> {

		//CustomListAdapter adapter = new CustomListAdapter(this, thisUriPathList);
		//	((ListView) DynamicListView).setAdapter(adapter);
		//});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == RESULT_LOAD_IMAGE) {

				Uri uri = data.getData();

				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getContentResolver().query(uri,
						filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				cursor.close();

				//mStorage = FirebaseStorage.getInstance().getReference();
				//StorageReference filePath = mStorage.child("images/" + UUID.randomUUID().toString());

				//filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){
				//	@Override
				//	public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
				//		Toast.makeText(ArActivity.this, "Upload Complete", Toast.LENGTH_SHORT).show();

				//	}


				//}).addOnFailureListener(new OnFailureListener() {
				//	@Override
				//	public void onFailure(@NonNull Exception e) {

				//		Toast.makeText(ArActivity.this, "Upload Failed: " + e.toString(), Toast.LENGTH_LONG).show();
				//	}
				//});

				addToUriPaths(picturePath, "type1");
			}
		}
	}
	public void addToUriPaths(String textToAdd, String type){





		Tagpost newTagpost = new Tagpost(textToAdd, type, 123);
		mModel.addTagpost(newTagpost);
	}

	public void deleteFromUriPaths(Tagpost tagpost){

		mModel.addTagpost(tagpost);

	}

	public static void startWithSetup(Activity currentActivity, Setup setupToUse) {
		ArActivity.staticSetupHolder = setupToUse;
		currentActivity.startActivity(new Intent(currentActivity,
				ArActivity.class));
	}

	public static void startWithSetupForResult(Activity currentActivity,
			Setup setupToUse, int requestCode) {
		ArActivity.staticSetupHolder = setupToUse;
		currentActivity.startActivityForResult(new Intent(currentActivity,
				ArActivity.class), requestCode);
	}

	private void runSetup() {
		mySetupToUse.run(this);

	}

	@Override
	protected void onRestart() {
		if (mySetupToUse != null)
			mySetupToUse.onRestart(this);
		super.onRestart();
	}

	@Override
	protected void onResume() {
		if (mySetupToUse != null)
			mySetupToUse.onResume(this);
		super.onResume();

	}

	@Override
	protected void onStart() {
		if (mySetupToUse != null)
			mySetupToUse.onStart(this);
		super.onStart();
	}

	@Override
	protected void onStop() {
		if (mySetupToUse != null)
			mySetupToUse.onStop(this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (mySetupToUse != null)
			mySetupToUse.onDestroy(this);
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		if (mySetupToUse != null)
			mySetupToUse.onPause(this);
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((mySetupToUse != null)
				&& (mySetupToUse.onKeyDown(this, keyCode, event)))
			return true;
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (((mySetupToUse != null) && mySetupToUse.onCreateOptionsMenu(menu)))
			return true;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (mySetupToUse != null)
			return mySetupToUse.onMenuItemSelected(featureId, item);
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(LOG_TAG, "main onConfigChanged");
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
			Log.d(LOG_TAG, "orientation changed to landscape");
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
			Log.d(LOG_TAG, "orientation changed to portrait");
		super.onConfigurationChanged(newConfig);
	}


}