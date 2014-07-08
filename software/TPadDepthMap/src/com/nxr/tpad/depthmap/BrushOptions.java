package com.nxr.tpad.depthmap;

import nxr.tpadnexus.lib.TPadTexture;
import android.app.Activity;
import android.content.Context;
import android.drm.DrmStore.Action;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.nxr.tpad.depthmap.ColorPickerDialog.OnColorChangedListener;
import com.nxr.tpad.depthmap.R;

//import nxr.tpad.lib.TPad;

public class BrushOptions extends Activity implements OnColorChangedListener, OnTouchListener {

	private ColorButton[] ColorButtons;
	private Button eraser;
	private SeekBar BrushWidthBar;
	private BrushPreview BrushView;
	
	private HapticCanvasActivity mActivity;

	Context mContext;
	OnColorChangedListener mColorListener;
	// TPad myTPad;

	PaintPalatte myPalatte;
	int colorID = 0;
	HapticCanvasView myHapticView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.brushlayout);

		myHapticView = HapticCanvasActivity.myHapticView;
		myPalatte = HapticCanvasView.myPaintBucket;

		ColorButtons = new ColorButton[myPalatte.length()];

		ColorButtons[0] = (ColorButton) findViewById(R.id.color0);
		ColorButtons[1] = (ColorButton) findViewById(R.id.color1);
		ColorButtons[2] = (ColorButton) findViewById(R.id.color2);
		ColorButtons[3] = (ColorButton) findViewById(R.id.color3);
		ColorButtons[4] = (ColorButton) findViewById(R.id.color4);
		ColorButtons[5] = (ColorButton) findViewById(R.id.color5);
		ColorButtons[6] = (ColorButton) findViewById(R.id.color6);
		ColorButtons[7] = (ColorButton) findViewById(R.id.color7);
		ColorButtons[8] = (ColorButton) findViewById(R.id.color8);
		ColorButtons[9] = (ColorButton) findViewById(R.id.color9);
		ColorButtons[10] = (ColorButton) findViewById(R.id.color10);
		ColorButtons[11] = (ColorButton) findViewById(R.id.color11);
		ColorButtons[12] = (ColorButton) findViewById(R.id.color12);
		ColorButtons[13] = (ColorButton) findViewById(R.id.color13);
		ColorButtons[14] = (ColorButton) findViewById(R.id.color14);
		ColorButtons[15] = (ColorButton) findViewById(R.id.color15);
		ColorButtons[16] = (ColorButton) findViewById(R.id.color16);
		ColorButtons[17] = (ColorButton) findViewById(R.id.color17);
		ColorButtons[18] = (ColorButton) findViewById(R.id.color18);
		ColorButtons[19] = (ColorButton) findViewById(R.id.color19);
		ColorButtons[20] = (ColorButton) findViewById(R.id.color20);
		ColorButtons[21] = (ColorButton) findViewById(R.id.color21);
		ColorButtons[22] = (ColorButton) findViewById(R.id.color22);
		ColorButtons[23] = (ColorButton) findViewById(R.id.color23);
		ColorButtons[24] = (ColorButton) findViewById(R.id.color24);


		BrushWidthBar = (SeekBar) findViewById(R.id.brushSizeSlider);
		BrushView = (BrushPreview) findViewById(R.id.brushPreview);
		
		for (int i = 0; i < ColorButtons.length; i++) {
			ColorButtons[i].buttonId = i;
			ColorButtons[i].setOnTouchListener(this);
		}

		setColors();
		ColorButtons[0].setSelect(true);
		HapticCanvasActivity.myHapticView.setBrushColor(myPalatte.getColor(0));
		BrushView.setColor(Color.CYAN);
		
		
		eraser = (Button) findViewById(R.id.eraser);

		// myTPad = HapticCanvasActivity.myTPad;

		mContext = this;
		mColorListener = this;

		BrushWidthBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int progressChanged = 0;

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				progressChanged = progress;
				int width = (int) (1 + progressChanged / (float) BrushWidthBar.getMax() * 130);
				BrushView.setWidth(width);
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				int width = (int) (1 + progressChanged / (float) BrushWidthBar.getMax() * 130);
				myHapticView.setBrushWidth(width);

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				//

			}
		});

		eraser.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myHapticView.setEraser(true);

				BrushView.setColor(Color.TRANSPARENT);

			}
		});

		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float amp = 0;
		TPadTexture texture = null;
		float freq = 0;
		//Log.i("Touch!", String.valueOf(event.getX()));
		for (ColorButton cb : ColorButtons) {

			if (v.getId() == cb.getId()) { // we are on the current color button
				
				cb.setSelect(true);
				myHapticView.setEraser(false);
				myHapticView.setBrushColor(myPalatte.getColor(cb.buttonId));
				BrushView.setColor(myPalatte.getColor(cb.buttonId));

				if ((cb.buttonId == 0) || (cb.buttonId == 5) || (cb.buttonId == 10) || (cb.buttonId == 15) || (cb.buttonId == 20)) {
					amp = 1f;
				} else if ((cb.buttonId == 1) || (cb.buttonId == 6) || (cb.buttonId == 11) || (cb.buttonId == 16) || (cb.buttonId == 21)) {
					amp = .75f;
				} else if ((cb.buttonId == 2) || (cb.buttonId == 7) || (cb.buttonId == 12) || (cb.buttonId == 17) || (cb.buttonId == 22)) {
					amp = .5625f;
				} else if ((cb.buttonId == 3) || (cb.buttonId == 8) || (cb.buttonId == 13) || (cb.buttonId == 18) || (cb.buttonId == 23)) {
					amp = .375f;
				} else if ((cb.buttonId == 4) || (cb.buttonId == 9) || (cb.buttonId == 14) || (cb.buttonId == 19) || (cb.buttonId == 24)) {
					amp = .1875f;
				}
				
				if ((cb.buttonId == 5) || (cb.buttonId == 6) || (cb.buttonId == 7) || (cb.buttonId == 8) || (cb.buttonId == 9)) {
					freq = 100;
					texture = TPadTexture.SQUARE;
				} else if ((cb.buttonId == 10) || (cb.buttonId == 11) || (cb.buttonId == 12) || (cb.buttonId == 13) || (cb.buttonId == 14)) {
					freq = 70f;
					texture = TPadTexture.SINUSOID;
				} else if ((cb.buttonId == 15) || (cb.buttonId == 16) || (cb.buttonId == 17) || (cb.buttonId == 18) || (cb.buttonId == 19)) {
					freq = 30f;
					texture = TPadTexture.SAWTOOTH;
				} else if ((cb.buttonId == 20) || (cb.buttonId == 21) || (cb.buttonId == 22) || (cb.buttonId == 23) || (cb.buttonId == 24)) {
					freq = 20f;
					texture = TPadTexture.SINUSOID;
				}


			}else{
				cb.setSelect(false);
				
			}
		}
		
		if(event.getAction() == MotionEvent.ACTION_UP){
			myHapticView.sendTPad(0f);
			
		}else if (freq > 0) { // we have a texture, sendthe texture command
			myHapticView.sendTPadTexture(texture, freq, amp);

		} else { // send normal tpad amp command
			myHapticView.sendTPad(amp);

		}
		
		

		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		myHapticView.setDrawing(true);
		setColors();
		// myTPad.send(0);

	}

	public void setColors() {

		myPalatte.setColor(4, ligthenColor(myPalatte.getColor(0), .75f));
		myPalatte.setColor(3, ligthenColor(myPalatte.getColor(0), .5625f));
		myPalatte.setColor(2, ligthenColor(myPalatte.getColor(0), .375f));
		myPalatte.setColor(1, ligthenColor(myPalatte.getColor(0), .1875f));
		myPalatte.setColor(0, myPalatte.getColor(0));

		myPalatte.setColor(9, ligthenColor(myPalatte.getColor(5), .75f));
		myPalatte.setColor(8, ligthenColor(myPalatte.getColor(5), .5625f));
		myPalatte.setColor(7, ligthenColor(myPalatte.getColor(5), .375f));
		myPalatte.setColor(6, ligthenColor(myPalatte.getColor(5), .1875f));
		myPalatte.setColor(5, myPalatte.getColor(5));

		myPalatte.setColor(14, ligthenColor(myPalatte.getColor(10), .75f));
		myPalatte.setColor(13, ligthenColor(myPalatte.getColor(10), .5625f));
		myPalatte.setColor(12, ligthenColor(myPalatte.getColor(10), .375f));
		myPalatte.setColor(11, ligthenColor(myPalatte.getColor(10), .1875f));
		myPalatte.setColor(10, myPalatte.getColor(10));

		myPalatte.setColor(19, ligthenColor(myPalatte.getColor(15), .75f));
		myPalatte.setColor(18, ligthenColor(myPalatte.getColor(15), .5625f));
		myPalatte.setColor(17, ligthenColor(myPalatte.getColor(15), .375f));
		myPalatte.setColor(16, ligthenColor(myPalatte.getColor(15), .1875f));
		myPalatte.setColor(15, myPalatte.getColor(15));

		myPalatte.setColor(24, ligthenColor(myPalatte.getColor(20), .75f));
		myPalatte.setColor(23, ligthenColor(myPalatte.getColor(20), .5625f));
		myPalatte.setColor(22, ligthenColor(myPalatte.getColor(20), .375f));
		myPalatte.setColor(21, ligthenColor(myPalatte.getColor(20), .1875f));
		myPalatte.setColor(20, myPalatte.getColor(20));

		for (int i = 0; i < ColorButtons.length; i++) {
			ColorButtons[i].setBackgroundColor(myPalatte.getColor(i));
		}
	}

	@Override
	public void colorChanged(int color) {
		myPalatte.setColor(colorID, color);
		myHapticView.setBrushColor(color);
		BrushView.setColor(myPalatte.getColor(colorID));
		setColors();
	}

	public int ligthenColor(int color, float val) {
		float[] hsv = new float[3];

		Color.colorToHSV(color, hsv);

		hsv[2] -= val;

		color = Color.HSVToColor(hsv);

		return color;

	}

}
