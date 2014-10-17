package com.nxr.tpad.hapticcanvas;

import nxr.tpadnexus.lib.TPadNexusTabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class HapticCanvasActivity extends TPadNexusTabActivity {

	// Used to initialize our screenview object for
	// drawing on to

	public static TabHost mTabHost;

	public static HapticCanvasView myHapticView;


	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// set the content view to our layout .xml
		setContentView(R.layout.hapticcanvas);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		mTabHost = getTabHost();

		TabSpec FileSpec = mTabHost.newTabSpec("File");
		FileSpec.setIndicator("Save/Load");
		Intent fileIntent = new Intent(this, FileOptions.class);
		FileSpec.setContent(fileIntent);
		
		TabSpec BrushSpec = mTabHost.newTabSpec("Brush");
		BrushSpec.setIndicator("Brush Options");
		Intent brushIntent = new Intent(this, BrushOptions.class);
		BrushSpec.setContent(brushIntent);
		
		TabSpec EditSpec = mTabHost.newTabSpec("Edit");
		EditSpec.setIndicator("Edit");
		Intent editIntent = new Intent(this, EditScreen.class);
		EditSpec.setContent(editIntent);
		
		TabSpec FeelSpec = mTabHost.newTabSpec("Feel");
		FeelSpec.setIndicator("Feel");
		Intent feelIntent = new Intent(this, FeelScreen.class);
		FeelSpec.setContent(feelIntent);
		
		mTabHost.addTab(FileSpec);
		mTabHost.addTab(BrushSpec);
		mTabHost.addTab(EditSpec);
		mTabHost.addTab(FeelSpec);
				

		// initialize screenview class object
		myHapticView = (HapticCanvasView) findViewById(R.id.hapticCanvasView);
		myHapticView.setContext(this);
		// Start communication with TPad
		setFreq(32870);

	}

	// Following code modified from

	@Override
	protected void onPause() {
		// Pauses our surfaceview thread
		super.onPause();
		// Stop our drawing thread (which runs in the screenview object) when
		// the screen is paused
		myHapticView.pause();

	}

	@Override
	protected void onDestroy() {
		
		myHapticView.destroy();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// resume the drawing thread (which runs in the screenview object) when
		// screen is resumed.
		myHapticView.resume();

	}

}
