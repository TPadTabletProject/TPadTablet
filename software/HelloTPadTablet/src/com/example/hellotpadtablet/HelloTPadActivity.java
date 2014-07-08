package com.example.hellotpadtablet;

import nxr.tpadnexus.lib.TPadNexusActivity;
import nxr.tpadnexus.lib.TPadTexture;
import nxr.tpadnexus.lib.views.DepthMapView;
import nxr.tpadnexus.lib.views.FrictionMapView;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;

public class HelloTPadActivity extends TPadNexusActivity {

	// Define 'View' classes that will link to the .xml file
	View basicView;
	View timeView;
	FrictionMapView fricView;
	DepthMapView depthView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the content of the screen to the .xml file that is in the layout folder
		setContentView(R.layout.activity_hello_tpad);

		// Initialize the TPad to the correct driving frequency
		setFreq(42400);

		// Link the first 'View' called basicView to the view with the id=view1
		basicView = (View) findViewById(R.id.view1);
		// Set the background color of the view to blue
		basicView.setBackgroundColor(Color.BLUE);

		basicView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				// Grab the x coordinate of the touch event, and the width of the view the event was in
				float x = event.getX();
				int width = view.getWidth();

				// The switch case below looks at the event's properties and specifies what type of touch it was
				switch (event.getAction()) {

				case MotionEvent.ACTION_DOWN:
					// If the initial touch was on the left half of the view, turn off the TPad, else turn it on to 80%
					if (x < width / 2f) {
						sendTPad(0f);
					} else {
						sendTPad(.8f);
					}
					break;

				case MotionEvent.ACTION_MOVE:
					// If the user moves to the left half of the view, turn off the TPad, else turn it on to 80%
					if (x < width / 2f) {
						sendTPad(0f);
					} else {
						sendTPad(.8f);
					}
					break;

				case MotionEvent.ACTION_UP:
					// If the user lifts up their finger from the screen, turn the TPad off (0%)
					sendTPad(0f);
					break;
				}

				return true;
			}
		});

		// Same linking as before, only with a different view
		timeView = (View) findViewById(R.id.view2);
		timeView.setBackgroundColor(Color.RED);

		timeView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				switch (event.getAction()) {

				case MotionEvent.ACTION_DOWN:
					// Turn on a time-based texture when the view is touched
					sendTPadTexture(TPadTexture.SAWTOOTH, 35, 1.0f);
					break;

				case MotionEvent.ACTION_UP:
					// Turn off the TPad when the user lifts up (set to 0%)
					sendTPad(0f);
					break;
				}

				return true;
			}
		});

		// Link friction view to .xml file
		fricView = (FrictionMapView) findViewById(R.id.view3);
		// Load in the image stored in the drawables folder
		Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.testimage);
		// Set the friction data bitmap to the test image
		fricView.setDataBitmap(defaultBitmap);

		// Same process as for the friction view
		depthView = (DepthMapView) findViewById(R.id.view4);
		defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.testdepth);
		depthView.setDataBitmap(defaultBitmap);

	}

}
