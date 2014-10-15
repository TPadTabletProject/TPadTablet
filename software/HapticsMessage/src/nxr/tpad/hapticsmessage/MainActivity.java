package nxr.tpad.hapticsmessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import nxr.tpadnexus.lib.TPadNexusFragmentActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends TPadNexusFragmentActivity implements OnSeekBarChangeListener {
	ServerFragment serverView;
	DrawFragment drawFragment;
	RelativeLayout chatView;
	SeekBar optionsBar;
	ScrollView scrollView;
	Button leftButton, rightButton;
	EditText textBox;
	static File logFile;
	static FileWriter fw;
	TextView numberText; 

	AlertDialog.Builder alert;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		logFile = createFile();		
		
		chatView = (RelativeLayout) findViewById(R.id.chat_layout);

		serverView = (ServerFragment) getSupportFragmentManager().findFragmentById(R.id.server_fragment);

		android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
		android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		drawFragment = new DrawFragment();

		fragmentTransaction.add(R.id.drawview_container, drawFragment);
		fragmentTransaction.commit();

		numberText = (TextView) findViewById(R.id.textView1);
		
		optionsBar = (SeekBar) findViewById(R.id.options);
		optionsBar.setMax(DrawFragment.optionsNames.length - 1);
		optionsBar.setOnSeekBarChangeListener(this);
		scrollView = (ScrollView) findViewById(R.id.scroll_view);

		leftButton = (Button) findViewById(R.id.leftbutton);
		rightButton = (Button) findViewById(R.id.rightbutton);

		leftButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int progress = optionsBar.getProgress();
				if (progress > 0)
					optionsBar.setProgress(progress - 1);

			}
		});

		rightButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int progress = optionsBar.getProgress();
				if (progress < optionsBar.getMax())
					optionsBar.setProgress(progress + 1);

			}
		});

		textBox = (EditText) findViewById(R.id.textbox);

		textBox.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);

		textBox.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View view, MotionEvent motionEvent) {
				serverView.hide();
				return false;
			}
		});

		textBox.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND) {
				
					sendMessage();
					InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

					im.hideSoftInputFromWindow(textBox.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

					return true;
				}

				return false;
			}
		});

		drawFragment.mainActivity = this;
		serverView.mainActivity = this;
		drawFragment.serverView = serverView;
		serverView.drawView = drawFragment;
		SharedPreferences stored = getPreferences(0);
		String freq = stored.getString("freq", "0");
		if (freq != "0") {
			setFreq(Integer.parseInt(freq));
		}
	}

	public void startPopup() {
		alert = new AlertDialog.Builder(this);
		alert.setTitle("TPad");
		alert.setMessage("Set the new frequency:");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				setFreq(Integer.parseInt(value));
				SharedPreferences freq = getPreferences(0);
				SharedPreferences.Editor editor = freq.edit();
				editor.putString("freq", value);
				editor.commit();
			}
		});
		alert.show();
	}

	public void sendMessage() {
		
		
		String s = textBox.getText().toString();
		int p = optionsBar.getProgress();
		
		serverView.sendMessage(p, s);
		drawFragment.recieveMessage(p, s, false);

		textBox.setText("");
		optionsBar.setProgress(0);
		// textBox.requestFocus();
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.connect:
			if (serverView.isVisible()) {
				serverView.hide();
			} else {
				serverView.show();
			}
			break;
		case R.id.pwmfreq:
			startPopup();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		drawFragment.drawView.reDraw(optionsBar.getProgress());
		numberText.setText(String.valueOf(arg1));
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
	}

	public File createFile() {

		String file_path = Environment.getExternalStorageDirectory().getPath() + "/logFiles";

		File saveFile = new File(file_path);

		if (!saveFile.exists()) {
			saveFile.mkdirs();
		}

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd. h:mm:ss:SSS a");
		String formattedDate = sdf.format(date);
		Log.i("Date:", formattedDate);

		int stringId = this.getApplicationInfo().labelRes;
		String appName = this.getString(stringId);

		saveFile = new File(file_path, formattedDate + " " + appName + ".txt");
	
		try {
			fw = new FileWriter(saveFile, true);
			fw.write("File Start\r\n");
			fw.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

		
		return saveFile;
	}

	public void writeToLog(String msg) {
		
		try {
			fw = new FileWriter(logFile, true);
			fw.write(msg+"\r\n");
			fw.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		
	}
	
	
}
