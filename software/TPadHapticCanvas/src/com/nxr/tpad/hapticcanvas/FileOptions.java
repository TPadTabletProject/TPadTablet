package com.nxr.tpad.hapticcanvas;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class FileOptions extends Activity {

	private static Button LoadBackgroundButton;

	private static View loadView;

	private static final int REQ_CODE_PICK_IMAGE = 100;

	private boolean hapticLoad = true;
	
	public static final String dl = ";";

	// TPad myTPad;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filelayout);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		// myTPad = HapticCanvasActivity.myTPad;

		LoadBackgroundButton = (Button) findViewById(R.id.loadBackgroundButton);
		loadView = (View) findViewById(R.id.loadView);
		
		// Action to take when picture selection button is clicked
		LoadBackgroundButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hapticLoad = false;
				// We must start a new intent to request data from the system's
				// image picker
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);
			}
		});
		
		loadView.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				hapticLoad = true;
				// We must start a new intent to request data from the system's
				// image picker
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);
				
				return false;
			}
		});

		/*
		// Action to take when save picture button is clicked
		SaveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveFile();
			}
		});
		*/
	}
/*
	protected void saveFile() {
		
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath()+"/Haptic Images");

		String file_path = mediaStorageDir.getPath();
		Log.i("Path: ", file_path);
		OutputStream fOut = null;

		File dir = new File(file_path);

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd. h:mm:ss a");
		String formattedDate = sdf.format(date);
		Log.i("Date:", formattedDate);

		if (!dir.exists()) {
			dir.mkdirs();
		}
		dir = new File(file_path, formattedDate + " " + FileName.getText().toString() + ".png");

		Log.i("Dir:", dir.toString());
		try {
			fOut = new FileOutputStream(dir);
			HapticCanvasActivity.myHapticView.getDrawBitmap().compress(Bitmap.CompressFormat.PNG, 100, fOut);
			fOut.flush();
			fOut.close();
			//sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(dir)));
		} catch (IOException e) {
			Log.i("Fail:", " didn't flush stream");
			e.printStackTrace();
		}
		
	}
	*/

	// http://stackoverflow.com/questions/9564644/null-pointer-exception-while-loading-images-from-gallery
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		// Parse the activity result
		switch (requestCode) {
		case REQ_CODE_PICK_IMAGE:
			if (resultCode == RESULT_OK) {
				Uri bitmapUri = imageReturnedIntent.getData();
				String fileName = null;
				String scheme = bitmapUri.getScheme();
				if (scheme.equals("file")) {
				    fileName = bitmapUri.getLastPathSegment();
				}
				else if (scheme.equals("content")) {
				    String[] proj = { MediaStore.Images.Media.TITLE };
				    Cursor cursor = this.getContentResolver().query(bitmapUri, proj, null, null, null);
				    if (cursor != null && cursor.getCount() != 0) {
				        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
				        cursor.moveToFirst();
				        fileName = cursor.getString(columnIndex);
				    }
				}
				
				HapticCanvasActivity.myHapticView.writeToLog(timestamp() + dl + "BackgroundLoaded" + dl + fileName);
				HapticCanvasActivity.myHapticView.backgroundName = fileName;
				
				try {

					// set the display bitmap based on the bitmap we just got
					// back from our intent
					Bitmap b = Media.getBitmap(getContentResolver(), bitmapUri);
					
					if (hapticLoad){
						HapticCanvasActivity.myHapticView.setDrawBitmap(b);
					}else{
						HapticCanvasActivity.myHapticView.setBackgroundBitmap(b);
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;

			}
		}
	}

	@Override
	protected void onResume() {
		HapticCanvasActivity.myHapticView.setDrawing(true);
		super.onResume();
	}
	private String timestamp(){
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd h:mm:ss:SSS a");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
}
