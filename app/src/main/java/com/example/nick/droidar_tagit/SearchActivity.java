package com.example.nick.droidar_tagit;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
	EditText etQuery;
	GridView gvImageResult;
	Button btSearch;
	TextView tvPage;

	ArrayList<ImageResult> imageResults = new ArrayList<ImageResult>();
	ImageResultArrayAdapter imageAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		setupViews();

		imageAdapter = new ImageResultArrayAdapter(this, imageResults);
		gvImageResult.setAdapter(imageAdapter);

		//Click on a thumbnail
		gvImageResult.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adaptor, View parent, int position, long rowId) {
				Intent i = new Intent(getApplicationContext(), ArActivity.class);
				ImageResult imageResult = imageResults.get(position);

				//TODO get full image url instead of thumbnail?
				i.putExtra("webImage", imageResult.getFullUrl());
				Log.d("customSearch", "onclick full url: " + imageResult.getFullUrl());

				setResult(ArActivity.RESULT_OK, i);
				finish();
			}
		});

		// When this Activity is called somewhere else
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String searchString = extras.getString(Intent.EXTRA_TEXT);
			if (searchString != null) {
				etQuery.setText(searchString);
				onImageSearch(btSearch);
			}
		}
	}

	public void setupViews() {
		etQuery = (EditText) findViewById(R.id.etQuery);
		gvImageResult = (GridView) findViewById(R.id.gvImageResult);
		btSearch = (Button) findViewById(R.id.btSearch);
		tvPage = (TextView) findViewById(R.id.tvPage);
	}

	//Clicking Search Button
	public void onImageSearch(View v) {

		String query = etQuery.getText().toString();
		Toast.makeText(this, "Searching for " + query, Toast.LENGTH_SHORT).show();

		try {
			new searchQuery().execute();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private class searchQuery extends AsyncTask<String, Void, JSONArray> {
		String key = "AIzaSyCiriyv0k9cVQrX_1QLGNA8Jt3abgnZt8Q";
		String cseID;

		String qry = URLEncoder.encode(etQuery.getText().toString(), "utf-8");
		//String qry="cat";// your keyword to search
		URL url = null;

		JSONArray imageJsonResults;

		private searchQuery() throws UnsupportedEncodingException { }

		@Override
		protected JSONArray doInBackground(String... qryString) {
			try {
				url = new URL("https://www.googleapis.com/customsearch/v1?key=" + key + "&cx=010994179142303008059:ywduztp3y6o&q=" + qry + "&alt=json" + "&searchType=image");

				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "application/json");
				BufferedReader br = new BufferedReader(new InputStreamReader(
						(conn.getInputStream())));

				StringBuilder builder = new StringBuilder();

				String output;
				Log.d("customSearch", "url.toString()" + url.toString());
				while ((output = br.readLine()) != null) {
					builder.append(output + "\n");
				}

				try {
					JSONObject outerJSON = new JSONObject(builder.toString());
					Log.d("customSearch", "outerJSON: " + outerJSON.toString());
					imageJsonResults = outerJSON.getJSONArray("items");

					Log.d("customSearch", "itemArray: " + imageJsonResults.toString());

					imageResults.clear();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Log.d("customSearch", "builder.toString()" + builder.toString());
				try {
					JSONArray jsonArray = new JSONArray(builder.toString());
					Log.d("customSearch", "jsonArray.toString" + jsonArray.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				//TODO need to close bufferedReader or string builder ??
				conn.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return imageJsonResults;
		}

		@Override
		protected void onPostExecute(JSONArray jArray) {
			imageAdapter.addAll(ImageResult.fromJSONArray(imageJsonResults));
		}
	}
}
