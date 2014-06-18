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

	View basicView;
	View timeView;
	FrictionMapView fricView;
	DepthMapView depthView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_hello_tpad);

		setFreq(33300);

		basicView = (View) findViewById(R.id.view1);
		basicView.setBackgroundColor(Color.BLUE);

		basicView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				switch (event.getAction()) {
				
				case MotionEvent.ACTION_DOWN:
					if (event.getX() < view.getWidth() / 2f) {
						sendTPad(0f);
					} else {
						sendTPad(.8f);
					}
					break;

				case MotionEvent.ACTION_MOVE:
					if (event.getX() < view.getWidth() / 2f) {
						sendTPad(0f);
					} else {
						sendTPad(.8f);
					}
					break;
					
				case MotionEvent.ACTION_UP:
					sendTPad(0f);
					break;
				}
				
				return true;
			}
		});

		timeView = (View) findViewById(R.id.view2);
		timeView.setBackgroundColor(Color.RED);

		timeView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				switch (event.getAction()) {
				
				case MotionEvent.ACTION_DOWN:
					sendTPadTexture(TPadTexture.SAWTOOTH, 35, 1.0f);
					break;

				case MotionEvent.ACTION_MOVE:
					sendTPadTexture(TPadTexture.SAWTOOTH, 35, 1.0f);
					break;
					
				case MotionEvent.ACTION_UP:
					sendTPad(0f);
					break;
				}
				
				return true;
			}
		});

		fricView = (FrictionMapView) findViewById(R.id.view3);
		Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.testimage);
		fricView.setDataBitmap(defaultBitmap);

		depthView = (DepthMapView) findViewById(R.id.view4);
		defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.testdepth);
		depthView.setDataBitmap(defaultBitmap);

	}

}
