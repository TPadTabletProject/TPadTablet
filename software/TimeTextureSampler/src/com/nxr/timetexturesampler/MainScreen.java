package com.nxr.timetexturesampler;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;

import nxr.tpadnexus.lib.TPadNexusFragmentActivity;
import nxr.tpadnexus.lib.TPadTexture;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;

public class MainScreen extends TPadNexusFragmentActivity {

	private final int MINX = 1;
	private final int MAXX = 500;
	
	private final float MINX2 = 0.25f;
	private final int MAXX2 = 70;	
	
	EditText freqEdit, ampEdit, freqEdit2, ampEdit2;
	float freq, amp, freq2, amp2;
	int wave, wave2;
	boolean testing = true;
	//TextView text;
	View testingArea;
	private GraphView graphView;
	private GraphViewSeries textureVisual;
	private long TextureSampleRate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFreq(33300);
		setContentView(R.layout.activity_main_screen);
		
		TextureSampleRate = super.getTextureSampleRate();
		UpdateGraph();
		UpdateGraph();
		//text = (TextView) findViewById(R.id.textView1);
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
				UpdateGraph();
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
					UpdateGraph();
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
				UpdateGraph();
			}
		});
		Spinner spinner2 = (Spinner) findViewById(R.id.wavetype2);
		spinner2.setAdapter(adapter);
		spinner2.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int pos, long arg3) {
				wave2 = pos;
				UpdateGraph();
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
					freq2 = (float) Math.exp(Math.log(MINX2) + (freq2 - MINX2) * (Math.log(MAXX2) - Math.log(MINX2))/(MAXX2 - MINX2));
					Log.i("TAG", Float.toString(freq2));
					String s = String.format("%.2f", freq2);
					freqEdit2.setText(s);
					UpdateGraph();
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
				UpdateGraph();
			}
		});
		final CheckBox check = (CheckBox) findViewById(R.id.checkBox1);
		check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

		       @Override
		       public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		    	   UpdateGraph();
		       }
		   }
		);   
		
		
		testingArea = (View) findViewById(R.id.area);
		testingArea.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (testing) {
					TPadTexture type = convertWave(wave);
					switch(event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						//text.setText("On");
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
						//text.setText("Off");
						sendTPad(0);
						break;
					}
				}
				return true;
			}
		});
		/*
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
		*/
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
	
	boolean set = true;
	
	public void UpdateGraph() {
        int num = 1000;
        GraphViewData[] data;
        data = new GraphViewData[num];
        LinearLayout layout;
		int periodSamps = (int) ((1 / freq) * TextureSampleRate);
		int periodSampsSaw1 = (int) ((1 / freq) * TextureSampleRate);
		int periodSampsSaw2 = (int) ((1 / freq2) * TextureSampleRate);

		
		float tp = 0;
		final CheckBox check = (CheckBox) findViewById(R.id.checkBox1);

		if(check.isChecked()) {
			float minfreq = Math.min(freq, freq2);
			periodSamps = (int) ((1 / minfreq) * TextureSampleRate);
		}
		
	    graphView = new LineGraphView(this, "Demo Plot");

		graphView.getGraphViewStyle().setVerticalLabelsWidth(-52);
	    graphView.getGraphViewStyle().setNumHorizontalLabels(0);
	    graphView.getGraphViewStyle().setGridColor(Color.BLACK);
        
		switch(wave) {
			case 0:
				//SINUSOID
		        data[0] = new GraphViewData(0, 1);
		        for (int i=1; i<num; i++) {
		        	data[i] = new GraphViewData(i, amp * (1 + Math.sin(2 * Math.PI * freq * (i - 1) / TextureSampleRate)) / 2f);
		        }
				break;
			case 1:
				//SQUARE
		        data[0] = new GraphViewData(0, 1);
		        for (int i=1; i<num; i++) {
		        	
					tp = (float) ((1 + Math.sin(2 * Math.PI * freq * i / TextureSampleRate)) / 2f);

					if (tp > (.5)) {
						data[i] = new GraphViewData(i, amp);
					} else
						data[i] = new GraphViewData(i, 0);
		        }
				break;
			case 2:
				//SAWTOOTH
		        data[0] = new GraphViewData(0, 1);
		        for (int i=1; i<num; i++) {
		        	data[i] = new GraphViewData(i, amp * ((i-1) % periodSampsSaw1));
		        }
				break;
		}
		
		if(check.isChecked()) {
			switch(wave2) {
			case 0:
				//SINUSOID
		        data[0] = new GraphViewData(0, 1);
		        for (int i=1; i<num; i++) {
		        	data[i] = new GraphViewData(i, data[i].getY() * amp2 * (1 + Math.sin(2 * Math.PI * freq2 * (i - 1) / TextureSampleRate)) / 2f);
		        }
				break;
			case 1:
				//SQUARE
		        data[0] = new GraphViewData(0, 1);
		        for (int i=1; i<num; i++) {
		        	
					tp = (float) ((1 + Math.sin(2 * Math.PI * freq2 * i / TextureSampleRate)) / 2f);

					if (tp > (.5)) {
						data[i] = new GraphViewData(i, data[i].getY() * amp2);
					} else
						data[i] = new GraphViewData(i, data[i].getY() * 0);
		        }
				break;
			case 2:
				//SAWTOOTH
		        data[0] = new GraphViewData(0, 1);
		        for (int i=1; i<num; i++) {
		        	data[i] = new GraphViewData(i, data[i].getY() * amp2 * ((i-1) % periodSampsSaw2));
		        }
				break;
			}
		}
        if(set) {
        	wave = 0;
        	amp = 1;
        	freq = 25;
        	amp2 = 1;
        	freq2 = 30;
        	wave2 = 0;
        	
        	textureVisual = new GraphViewSeries(data);
    		graphView.addSeries(textureVisual); // data
        } else
        	textureVisual.resetData(data);
        set = false;
        
        layout = (LinearLayout) findViewById(R.id.area);		        
        layout.addView(graphView);
	}
	


}
