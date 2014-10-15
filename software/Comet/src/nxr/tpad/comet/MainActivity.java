package nxr.tpad.comet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import nxr.tpadioio.lib.TPadIOIOFragmentActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class MainActivity extends TPadIOIOFragmentActivity {
	DrawviewFragment drawView;	
	ServerFragment serverView;
	OptionsFragment optionView;
	static File logFile;
	static FileWriter fw;
	AlertDialog.Builder alert;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		logFile = createFile();
		drawView = (DrawviewFragment) getSupportFragmentManager().findFragmentById(R.id.drawview_fragment);
		serverView = (ServerFragment) getSupportFragmentManager().findFragmentById(R.id.server_fragment);
		optionView = (OptionsFragment) getSupportFragmentManager().findFragmentById(R.id.options_fragment);

		drawView.serverView = serverView;
		drawView.optionView = optionView;
		drawView.mainActivity = this;
		serverView.drawView = drawView;
		serverView.optionView = optionView;
		serverView.mainActivity = this;
		optionView.serverView = serverView;
		optionView.drawView = drawView;
		optionView.mainActivity = this;
		
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

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.options:
			if (optionView.isVisible()) {
				optionView.hide();
			} else {
				optionView.show();
			}
			break;
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
			fw.write("File Start \r\n");
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
			fw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		
	}
	
}