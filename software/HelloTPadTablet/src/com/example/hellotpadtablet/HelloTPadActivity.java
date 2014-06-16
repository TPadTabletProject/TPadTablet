package com.example.hellotpadtablet;

import nxr.tpadioio.lib.TPadIOIOActivity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;

public class HelloTPadActivity extends TPadIOIOActivity {

	// First we must create a 'View' class we will be interacting with
	View helloView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hello_tpad);
		// Initialize the TPad to a specific frequency
		setFreq(33300);
		
		// Using our 'View' class, we must link it to the .xml object that is sitting in 
		helloView = (View) findViewById(R.id.view1);
		
		// Set helloView to have a dark gray background color so we can visualize it.
		helloView.setBackgroundColor(Color.DKGRAY);
		
		helloView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// Turns the TPad on when a user touches DOWN on helloView
					sendTPad(1f);
					break;
					
				case MotionEvent.ACTION_MOVE:
					sendTPad(1f);
					break;
				case MotionEvent.ACTION_UP:
					// Turns the TPad off when a user lets UP from helloView
					sendTPad(0f);
					break;
					
				}		
				

				return true;
			}
		});
	}

}
