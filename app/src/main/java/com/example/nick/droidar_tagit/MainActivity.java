package com.example.nick.droidar_tagit;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

	private static int RESULT_LOAD_IMAGE = 1;

	// Used to load the 'native-lib' library on application startup.
	static {
		System.loadLibrary("native-lib");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Button b = new Button(this);
		b.setBackground/*Drawable*/(getResources().getDrawable(R.drawable.ic_t_logo));
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ArActivity.startWithSetup(MainActivity.this, new TagitSetup() { });
			}
		});
		setContentView(b);

		//b.performClick();

		// Example of a call to a native method
		// TextView tv = (TextView) findViewById(R.id.sample_text);
		// tv.setText(stringFromJNI());
	}

	/**
	 * A native method that is implemented by the 'native-lib' native library,
	 * which is packaged with this application.
	 */
	public native String stringFromJNI();
}
