package com.example.nick.droidar_tagit;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.content.CursorLoader;

import com.google.firebase.storage.StorageReference;

import system.Setup;
import util.Log;

/**
 * This is an example activity which demonstrates how to use a Setup object. It
 * wraps the Setup object and forwards all needed events to it.
 *
 * @author Simon Heinen
 */
public class ArActivity extends AppCompatActivity/*LifecycleActivity*/ {

	static final int REQUEST_IMAGE_CAPTURE = 3;
	static final int IMAGE_SEARCH_CODE = 5;
	private static final String LOG_TAG = ArActivity.class.getSimpleName();
	private static Setup staticSetupHolder;
	private static int RESULT_LOAD_IMAGE = 1;
	View DynamicListView;
	private Setup mySetupToUse;
	private NameViewModel mModel;
	private PlacedTagModel ptModel;
	private StorageReference mStorage;

	public static void startWithSetup(AppCompatActivity currentActivity, Setup setupToUse) {
		ArActivity.staticSetupHolder = setupToUse;
		currentActivity.startActivity(new Intent(currentActivity, ArActivity.class));
	}

	public static void startWithSetupForResult(AppCompatActivity currentActivity, Setup setupToUse, int requestCode) {
		ArActivity.staticSetupHolder = setupToUse;
		currentActivity.startActivityForResult(new Intent(currentActivity, ArActivity.class), requestCode);
	}

	/**
	 * Called when the activity is first created.
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
		ptModel = ViewModelProviders.of(this).get(PlacedTagModel.class);

		Intent intent = getIntent();
		Uri data = intent.getData();

		//TODO handle the error when user presses back button after launching activity from outside application
		//"Fail to connect to camera service"

		if (getIntent().getExtras() != null & intent.getStringExtra("webImage") == null) {
			Log.d("customSearch", "data.toString: " + intent.getExtras().toString());

			ArActivity.startWithSetup(ArActivity.this, new TagitSetup() { });

			// Figure out what to do based on the intent type
			if (intent.getType().contains("image/")) {
				Toast.makeText(getApplicationContext(), "Image recognized", Toast.LENGTH_SHORT).show();

				//TODO this is redundant, create class and refer to it, Dont repeat yourself

				String[] projection = {MediaStore.Images.Media.DATA};
				Cursor cursor = managedQuery(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						projection, null, null, null);
				int column_index_data = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToLast();

				String imagePath = cursor.getString(column_index_data);
				addToUriPaths(imagePath, "type1");
				Log.d("imageURL", "Uri uri: " + imagePath);
			} else if (intent.getType().equals("text/plain")) {

				String imageUri = intent.getStringExtra(Intent.EXTRA_TEXT);
				//String imageUri = (String) intent.getParcelableExtra(Intent.EXTRA_TEXT);
				//Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_TEXT);
				//TODO verify that link works and url is valid image file
				Log.d("imageURL", "imageUri : " + imageUri);
				if (imageUri != null && !imageUri.isEmpty()) {

					String textToDisplay = imageUri;

					if (textToDisplay.startsWith("http://") || textToDisplay.startsWith("https://")) {
						addToUriPaths(textToDisplay, "type3");
					} else {
						addToUriPaths(textToDisplay, "type2");
					}
				}
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("customSearch", "intent result: " + data.toString());
		Log.d("customSearch", "intent requestCode: " + requestCode);
		if (resultCode == RESULT_OK) {

			if (requestCode == RESULT_LOAD_IMAGE) {
				Uri uri = data.getData();
				String[] filePathColumn = {MediaStore.Images.Media.DATA};
				Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				cursor.close();

				addToUriPaths(picturePath, "type1");
			}
			if (requestCode == IMAGE_SEARCH_CODE) {

				//TODO currently nothing happens on image search result. Change this?
				String webImageUrl = data.getStringExtra("webImage");

				Log.d("customSearch", "webImage: " + webImageUrl);
				addToUriPaths(webImageUrl, "type3");
				//Toast.makeText(getApplicationContext(), "Image search URI: " + uri.toString(), Toast.LENGTH_SHORT).show();
			}
			if (requestCode == REQUEST_IMAGE_CAPTURE) {

				String[] projection = {MediaStore.Images.Media.DATA};
				Cursor cursor = managedQuery(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						projection, null, null, null);
				int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToLast();

				String imagePath = cursor.getString(column_index_data);
				addToUriPaths(imagePath, "type1");
				Log.d("imageURL", "Uri uri: " + imagePath);
			}
		} else {
			Log.d("customSearch", "resultCode: " + resultCode);
		}
	}

	public void addToUriPaths(String textToAdd, String type) {
		Tagpost newTagpost = new Tagpost(textToAdd, type, 123);
		mModel.addTagpost(newTagpost);
	}

	public void deleteFromUriPaths(Tagpost tagpost) {
		mModel.addTagpost(tagpost);
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
		Log.d("ARkeydown", "keycode: " + keyCode);
		Log.d("ARkeydown", "keyevent: " + event.toString());
		if ((mySetupToUse != null) && (mySetupToUse.onKeyDown(this, keyCode, event)))
			return true;
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (((mySetupToUse != null) && mySetupToUse.onCreateOptionsMenu(menu)))
			return true;
		return super.onCreateOptionsMenu(menu);
	}

	/*	@Override
		public boolean onMenuItemSelected(int featureId, MenuItem item) {
			if (mySetupToUse != null)
				return mySetupToUse.onMenuItemSelected(featureId, item);
			return super.onMenuItemSelected(featureId, item);
		}
	*/
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(LOG_TAG, "main onConfigChanged");
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
			Log.d(LOG_TAG, "orientation changed to landscape");
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
			Log.d(LOG_TAG, "orientation changed to portrait");
		super.onConfigurationChanged(newConfig);
	}

	public void showAlertDialog(int tagid) {
		runOnUiThread(new Runnable() {
			public void run() {
				final CharSequence[] items = {"Delete All", "Delete", "Nevermind"};
				AlertDialog.Builder builder = new AlertDialog.Builder(ArActivity.this);
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
//						MediaPlayer mPlayer;
//						mPlayer = MediaPlayer.create(getBaseContext(), R.raw.roll);
//						mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
						//Toast.makeText(getActivity().getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
						switch (item) {
							case 0:
								ptModel.deleteAllPlacedTags();
								Toast.makeText(ArActivity.this, "All Tags Deleted", Toast.LENGTH_SHORT).show();
//								mPlayer.start();
								break;
							case 1:

								ptModel.deletePlacedTagById(tagid);
								Toast.makeText(ArActivity.this, "Deleted" + tagid, Toast.LENGTH_SHORT).show();
//								mPlayer.start();
								break;
							default:
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
	}
}