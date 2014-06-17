package com.nxr.tpad.frictionmap;

import android.app.Activity;
import android.os.Bundle;
import com.nxr.tpad.frictionmap.R;

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
