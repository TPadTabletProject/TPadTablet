package com.nxr.tpad.frictionmap;

import java.io.FileDescriptor;
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
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import com.nxr.tpad.frictionmap.R;

public class FileOptions extends Activity {

	private static Button LoadBackgroundButton, LoadHapticButton, LoadBothButton;

	private static View loadView;

	private static final int REQ_CODE_PICK_IMAGE = 100;

	private boolean hapticLoad = true;
	private boolean visualLoad = true;

	public static final String dl = ";";

	// TPad myTPad;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filelayout);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		// myTPad = HapticCanvasActivity.myTPad;

		LoadBackgroundButton = (Button) findViewById(R.id.loadBackgroundButton);
		LoadHapticButton = (Button) findViewById(R.id.loadHapticButton);
		LoadBothButton = (Button) findViewById(R.id.loadBothButton);

		// Action to take when picture selection button is clicked
		LoadBackgroundButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hapticLoad = false;
				visualLoad = true;
				// We must start a new intent to request data from the system's
				// image picker
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");

				Intent chooser = Intent.createChooser(intent, "Choose a Visual Layer");
				startActivityForResult(chooser, REQ_CODE_PICK_IMAGE);
			}
		});

		LoadHapticButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				hapticLoad = true;
				visualLoad = false;
				// We must start a new intent to request data from the system's
				// image picker
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");

				Intent chooser = Intent.createChooser(intent, "Choose a Haptic Layer");
				startActivityForResult(chooser, REQ_CODE_PICK_IMAGE);

			}
		});

		LoadBothButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				hapticLoad = false;
				visualLoad = false;
				// We must start a new intent to request data from the system's
				// image picker
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");

				Intent chooser = Intent.createChooser(intent, "Choose a Haptic Layer");
				startActivityForResult(chooser, REQ_CODE_PICK_IMAGE);

			}
		});

		/*
		 * // Action to take when save picture button is clicked SaveButton.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { saveFile(); } });
		 */
	}

	/*
	 * protected void saveFile() {
	 * 
	 * File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath()+"/Haptic Images");
	 * 
	 * String file_path = mediaStorageDir.getPath(); Log.i("Path: ", file_path); OutputStream fOut = null;
	 * 
	 * File dir = new File(file_path);
	 * 
	 * Date date = new Date(); SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd. h:mm:ss a"); String formattedDate = sdf.format(date); Log.i("Date:", formattedDate);
	 * 
	 * if (!dir.exists()) { dir.mkdirs(); } dir = new File(file_path, formattedDate + " " + FileName.getText().toString() + ".png");
	 * 
	 * Log.i("Dir:", dir.toString()); try { fOut = new FileOutputStream(dir); HapticCanvasActivity.myHapticView.getDrawBitmap().compress(Bitmap.CompressFormat.PNG, 100, fOut); fOut.flush();
	 * fOut.close(); //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(dir))); } catch (IOException e) { Log.i("Fail:", " didn't flush stream"); e.printStackTrace(); }
	 * 
	 * }
	 */

	// http://stackoverflow.com/questions/9564644/null-pointer-exception-while-loading-images-from-gallery
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Parse the activity result
		switch (requestCode) {
		case REQ_CODE_PICK_IMAGE:
			if (resultCode == RESULT_OK && data != null) {
				Uri bitmapUri = data.getData();

				try {

					// set the display bitmap based on the bitmap we just got
					// back from our intent
					// Bitmap b = Media.getBitmap(getContentResolver(), bitmapUri);
					Bitmap b = getBitmapFromUri(bitmapUri);
					if (hapticLoad) {
						HapticCanvasActivity.myHapticView.setDrawBitmap(b);
					} else if (visualLoad) {
						HapticCanvasActivity.myHapticView.setBackgroundBitmap(b);
					} else {
						HapticCanvasActivity.myHapticView.setBackgroundBitmap(b);
						HapticCanvasActivity.myHapticView.setDrawBitmap(b);
					}

					b.recycle();
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

	private Bitmap getBitmapFromUri(Uri uri) throws IOException {
		ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
		FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
		Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
		parcelFileDescriptor.close();
		return image;
	}

	@Override
	protected void onResume() {
		HapticCanvasActivity.myHapticView.setDrawing(true);
		super.onResume();
	}

	private String timestamp() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd h:mm:ss:SSS a");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
}
