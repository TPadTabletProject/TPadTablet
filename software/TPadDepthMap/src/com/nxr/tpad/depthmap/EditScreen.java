package com.nxr.tpad.depthmap;

import com.nxr.tpad.depthmap.R;

import android.app.Activity;
import android.os.Bundle;

public class EditScreen extends Activity{
	private static HapticCanvasView myHapticView;
	//TPad myTPad;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		myHapticView = HapticCanvasActivity.myHapticView;
		//myTPad = HapticCanvasActivity.myTPad;
		setContentView(R.layout.editlayout);
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		myHapticView.setDrawing(true);
		//myTPad.send(0);
	}

	@Override
	protected void onPause() {
		super.onPause();
	
	}
	
	
	
	
	
}
