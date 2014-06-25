package com.nxr.timetexturesampler;

import nxr.tpadnexus.lib.TPadNexusFragmentActivity;
import nxr.tpadnexus.lib.TPadTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;

public class MainScreen extends TPadNexusFragmentActivity {

	private final int MINX = 1;
	private final int MAXX = 500;	
	
	EditText freqEdit, ampEdit, freqEdit2, ampEdit2;
	float freq, amp, freq2, amp2;
	int wave, wave2;
	boolean testing = false;
	TextView text;
	View testingArea;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFreq(34910);
		setContentView(R.layout.activity_main_screen);
		text = (TextView) findViewById(R.id.textView1);
		Spinner spinner = (Spinner) findViewById(R.id.wavetype);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.wave_types, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int pos, long arg3) {
				wave = pos;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
		ampEdit = (EditText) findViewById(R.id.ampEdit);
		freqEdit = (EditText) findViewById(R.id.freqEdit);
		final SeekBar freqBar = (SeekBar) findViewById(R.id.freqBar);
		/*freqEdit.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH ||
			            actionId == EditorInfo.IME_ACTION_DONE ||
			            event.getAction() == KeyEvent.ACTION_DOWN &&
			            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
//					double d = Double.parseDouble(v.getText().toString());
//					freqBar.setProgress(50);
//					(int) Math.log(d)
				}
				return false;
			}
		});*/
		freqBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if (fromUser) {
					freq = seekBar.getProgress();
//					int y = (int) (((freq*freq)/500)*Math.log10(freq));
					freq = (float) Math.exp(Math.log(MINX) + (freq - MINX) * (Math.log(MAXX) - Math.log(MINX))/(MAXX - MINX));
					Log.i("TAG", Float.toString(freq));
					String s = String.format("%.2f", freq);
					freqEdit.setText(s);
				}
			}
		});
		SeekBar ampBar = (SeekBar) findViewById(R.id.ampBar);
		ampBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				double amplitude = seekBar.getProgress() / 100.0;
				amp = (float) amplitude;
				ampEdit.setText(Double.toString(amplitude));
			}
		});
		Spinner spinner2 = (Spinner) findViewById(R.id.wavetype2);
		spinner2.setAdapter(adapter);
		spinner2.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int pos, long arg3) {
				wave2 = pos;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
		ampEdit2 = (EditText) findViewById(R.id.ampEdit2);
		freqEdit2 = (EditText) findViewById(R.id.freqEdit2);
		final SeekBar freqBar2 = (SeekBar) findViewById(R.id.freqBar2);
		/*freqEdit.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH ||
			            actionId == EditorInfo.IME_ACTION_DONE ||
			            event.getAction() == KeyEvent.ACTION_DOWN &&
			            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
//					double d = Double.parseDouble(v.getText().toString());
//					freqBar.setProgress(50);
//					(int) Math.log(d)
				}
				return false;
			}
		});*/
		freqBar2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if (fromUser) {
					freq2 = seekBar.getProgress();
//					int y = (int) (((freq*freq)/500)*Math.log10(freq));
					freq2 = (float) Math.exp(Math.log(MINX) + (freq2 - MINX) * (Math.log(MAXX) - Math.log(MINX))/(MAXX - MINX));
					Log.i("TAG", Float.toString(freq2));
					String s = String.format("%.2f", freq2);
					freqEdit2.setText(s);
				}
			}
		});
		SeekBar ampBar2 = (SeekBar) findViewById(R.id.ampBar2);
		ampBar2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				double amplitude = seekBar.getProgress() / 100.0;
				amp2 = (float) amplitude;
				ampEdit2.setText(Double.toString(amplitude));
			}
		});
		final CheckBox check = (CheckBox) findViewById(R.id.checkBox1);
		testingArea = (View) findViewById(R.id.area);
		testingArea.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (testing) {
					TPadTexture type = convertWave(wave);
					switch(event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						text.setText("On");
						if (check.isChecked()) {
							TPadTexture type2 = convertWave(wave2);
							sendTPadDualTexture(type, freq, amp, type2, freq2, amp2);
						}
						else {
							sendTPadTexture(type, freq, amp);
						}
//						sendTPad(1f);
						break;
					case MotionEvent.ACTION_MOVE:
//						TPadTexture type = convertWave(wave);
						if (check.isChecked()) {
							TPadTexture type2 = convertWave(wave2);
							sendTPadDualTexture(type, freq, amp, type2, freq2, amp2);
						}
						else {
							sendTPadTexture(type, freq, amp);
						}
//						sendTPad(1f);
						break;
					case MotionEvent.ACTION_UP:
						text.setText("Off");
						sendTPad(0);
						break;
					}
				}
				return true;
			}
		});
		Button start = (Button) findViewById(R.id.start);
		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!testing) {
					testing = true;
					
				}
				else {
					testing = false;
				}
				Toast.makeText(getApplicationContext(), "Testing: " + Boolean.toString(testing), Toast.LENGTH_SHORT).show();
			}
		});
	}

	public TPadTexture convertWave(int wave) {
		switch (wave) {
		case 0:
			return TPadTexture.SINUSOID;
		case 1:
			return TPadTexture.SQUARE;
		case 2:
			return TPadTexture.SAWTOOTH;
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_screen, menu);
		return true;
	}

}
