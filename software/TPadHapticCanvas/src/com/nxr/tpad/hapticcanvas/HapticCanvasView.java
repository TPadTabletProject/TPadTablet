package com.nxr.tpad.hapticcanvas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import nxr.tpadnexus.lib.TPadTexture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.VelocityTracker;

public class HapticCanvasView extends SurfaceView implements Runnable {

	// Creates thread for drawing to run on
	Thread myThread = null;
	HapticCanvasActivity myActivity;
	// Sets up boolean to check if thread is running
	private boolean isRunning = false;

	private boolean isDrawing;

	// Screen height and width
	private int height, width;

	// Output string used for debugging
	String displayString;

	// TPad Object for this activity

	// Keeps track of touching on screen
	private boolean isTouching = false;

	private static String currentTab;

	// Controls timout timer
	private long touchTimer;

	private static final Matrix identityMatrix = new Matrix();

	// Friction level variable
	private float friction;
	private float frequency;
	private int type;

	// Finger position variables
	private float px, py;
	private float px_old, py_old;

	// Bitmap position variables (these have a velocity component added on)
	private int bx, by;

	// height of averaging patch (in pixels)
	private static final int patchWidth = 4; // must be even!
	private static final int patchHeight = 4; // must be even!
	private static final int bitmapmargin = Math.max(patchHeight, patchWidth) / 2;

	// int array that will hold our patch color values
	private static volatile int patch[] = new int[patchWidth * patchHeight];
	private static volatile int patchAvg;

	public static final PaintPalatte myPaintBucket = new PaintPalatte();

	// Sets up Holder to manipulate the surface view for us
	private SurfaceHolder holder;

	/*
	 * Holds the HSV data for each pixel hsv[0] is hue 0-360 hsv[1] is saturation 0-1 hsv[2] is value 0-1
	 */
	public float[] hsv = new float[3];

	public final int redHue;
	public final int blueHue;
	public final int yellowHue;
	public final int greenHue;
	public final int cyanHue;

	private VelocityTracker vTracker;
	private static double vy, vx;

	// private static final int PREDICT_HORIZON = (int) (TPad.TextureSampleRate * (.020f)); // 10,000Hz
	// // times
	// // 20ms
	// // =
	// // 200samples
	//
	// private static float[] predictedPixels = new float[PREDICT_HORIZON];

	// define canvas once so we are not constantly re-allocating memory in
	// our draw thread
	private static volatile Bitmap myDrawBitmap = null;
	private static volatile Bitmap myBackgroundBitmap = null;
	private static volatile Canvas myDrawCanvas = null;
	private static volatile Canvas myBackgroundCanvas = null;
	private static volatile Canvas myPostCanvas = null;
	public static volatile String backgroundName = null;

	public static volatile File logFile;
	public static Context logContext;
	public static volatile FileWriter fw;

	/*
	 * Objects we will draw onto the canvas. Initialize everything you intend to use as a draw variables here
	 */
	private Paint black = new Paint();
	private Paint white = new Paint();
	private Paint hapticPaint = new Paint();
	private Paint patchColor = new Paint();
	private Paint blue = new Paint();
	private Paint bluetext = new Paint();
	private Paint eraser = new Paint();

	// Brush variables
	private Paint brush = new Paint();
	private boolean eraserOn = false;
	private int brushColor;
	private int brushWidth;

	// SurfaceView Constructor
	public HapticCanvasView(Context context, AttributeSet attrs) {

		super(context, attrs);

		// Passes surface view to our holder;

		holder = getHolder();

		logFile = createFile();

		float[] hueHolder = new float[3];

		Color.colorToHSV(Color.RED, hueHolder);
		redHue = (int) hueHolder[0];
		Color.colorToHSV(Color.BLUE, hueHolder);
		blueHue = (int) hueHolder[0];
		Color.colorToHSV(Color.YELLOW, hueHolder);
		yellowHue = (int) hueHolder[0];
		Color.colorToHSV(Color.GREEN, hueHolder);
		greenHue = (int) hueHolder[0];
		Color.colorToHSV(Color.CYAN, hueHolder);
		cyanHue = (int) hueHolder[0];

		// Setup the paint variables here to certain colors/attributes

		black.setColor(Color.BLACK);
		black.setAlpha(255);
		black.setAntiAlias(true);

		white.setColor(Color.WHITE);

		patchColor.setColor(Color.WHITE);
		patchColor.setAntiAlias(true);

		bluetext.setColor(Color.BLUE);
		bluetext.setTextAlign(Paint.Align.LEFT);
		bluetext.setTextSize(24);
		bluetext.setAntiAlias(true);

		blue.setColor(Color.BLUE);
		blue.setDither(true);
		blue.setAlpha(200);

		brushColor = Color.WHITE;
		brushWidth = 10;

		brush.setColor(brushColor);
		brush.setStrokeCap(Paint.Cap.ROUND);
		brush.setStrokeWidth(brushWidth);
		brush.setAntiAlias(true);

		hapticPaint.setAlpha(200);
		hapticPaint.setAntiAlias(true);
		hapticPaint.setDither(true);

		eraser.setColor(brushColor);
		eraser.setStrokeCap(Paint.Cap.ROUND);
		eraser.setStrokeWidth(brushWidth);
		eraser.setAntiAlias(true);
		eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

		width = 900;
		height = 1300;

		myDrawBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		myDrawCanvas = new Canvas(myDrawBitmap);

		// Import default background and set as initial display bmp
		myBackgroundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		myBackgroundCanvas = new Canvas(myBackgroundBitmap);
		
		synchronized(myBackgroundBitmap){
			
			myBackgroundCanvas.drawARGB(255, 0, 0, 0);
		}

		isDrawing = false;

		if (!this.isInEditMode()) {
			// myTPad = HapticCanvasActivity.myTPad;
		}

		isFocusable();
		isFocusableInTouchMode();

	}

	public void setContext(HapticCanvasActivity a) {
		myActivity = a;
	}

	public void sendTPadTexture(TPadTexture text, float freq, float amp) {

		myActivity.sendTPadTexture(text, freq, amp);
		
	}

	public void sendTPad(float amp) {

		myActivity.sendTPad(amp);
	}

	public boolean getTouching() {

		return isTouching;
	}

	public void setEraser(boolean bool) {
		eraserOn = bool;
	}

	public void setDrawBitmap(Bitmap bmp) {

		synchronized (myDrawCanvas) {
			myDrawCanvas.drawBitmap(bmp, 0, 0, eraser);
			myDrawCanvas.drawBitmap(bmp, 0, 0, hapticPaint);

		}

	}

	public void setBackgroundBitmap(Bitmap bmp) {

		synchronized (myDrawCanvas) {
			myDrawCanvas.drawBitmap(bmp, 0, 0, eraser);
		}
		
		synchronized (myBackgroundCanvas) {
			
			myBackgroundCanvas.drawBitmap(bmp, 0, 0, eraser);
			myBackgroundCanvas.drawBitmap(bmp, 0, 0, brush);
		}
	}

	public Bitmap getDrawBitmap() {

		return myDrawBitmap;

	}

	public Bitmap getBackgroundBitmap() {

		return myBackgroundBitmap;

	}

	public void setBrushWidth(int i) {

		brushWidth = i;
		eraser.setStrokeWidth(brushWidth);
		brush.setStrokeWidth(brushWidth);
	}

	public void setBrushColor(int c) {

		brushColor = c;
		brush.setColor(brushColor);
	}

	public void setDrawing(boolean bool) {
		isDrawing = bool;
	}

	public boolean getDrawing() {
		return isDrawing;
	}

	// This method takes in a pixel value and maps it to a corresponding
	// friction
	// See the hsv declaration for info on what the array contains
	public void pixelToTexture(int pix) {

		float freq = 0;
		float amp = 0;

		TPadTexture waveType = TPadTexture.SINUSOID;

		Color.colorToHSV(pix, hsv);

		amp = hsv[2];

		if ((Color.green(pix)==Color.blue(pix))&& (Color.blue(pix)==Color.red(pix))&& Color.red(pix)==Color.green(pix)){
			// this is a grayscale image, skip
			
		}
		else if
		(hsv[0] == redHue) {
			freq = 100f;
			waveType = TPadTexture.SQUARE;
		} else if (hsv[0] == yellowHue) {
			freq = 70f;
			waveType = TPadTexture.SINUSOID;
		} else if (hsv[0] == greenHue) {
			freq = 30f;
			waveType = TPadTexture.SAWTOOTH;
		} else if (hsv[0] == blueHue) {
			freq = 20f;
			waveType = TPadTexture.SINUSOID;
		} else if (hsv[0] == cyanHue) {

		}

		if (freq > 0) { // we have a texture, sendthe texture command
			myActivity.sendTPadTexture(waveType, freq, amp);

		} else { // send normal tpad amp command
			myActivity.sendTPad(amp);

		}

	}

	public float pixelToFriction(int pixel) {
		// int[] rgb = { Color.red(pixel), Color.green(pixel), Color.blue(pixel)
		// };
		Color.colorToHSV(pixel, hsv);

		// return (.5f / 255f) * (computeMax(rgb) + computeMin(rgb));
		return hsv[2];
	}

	private void bitmapTasks() {

		// Here we make sure that the position coordinate are within the
		// displaying bitmap
		if (px >= myDrawBitmap.getWidth() - bitmapmargin)
			bx = myDrawBitmap.getWidth() - bitmapmargin;
		else if (px <= bitmapmargin)
			bx = bitmapmargin;
		else
			bx = (int) px;
		if (py >= myDrawBitmap.getHeight() - bitmapmargin)
			by = myDrawBitmap.getHeight() - bitmapmargin;
		else if (py <= bitmapmargin)
			by = bitmapmargin;
		else
			by = (int) py;

		// Read in the bitmap pixel value off and convert it to a fiction value
		/*
		 * int pixel = myDrawBitmap.getPixel(bx, by); friction = pixelToFriction(pixel); frequency = pixelToFrequency(pixel); type = pixelToWaveType(pixel);
		 */

		/*
		 * myDrawBitmap.getPixels(patch, 0, patchWidth, bx - patchWidth / 2, by - patchHeight / 2, patchWidth, patchHeight);
		 * 
		 * patchAvg = computePatchAvg();
		 * 
		 * friction = pixelToFriction(patchAvg); frequency = pixelToFrequency(patchAvg); type = pixelToWaveType(patchAvg);
		 */

		patchColor.setColor(patchAvg);

	}

	private int computePatchAvg() {
		int redBin = 0;
		int greenBin = 0;
		int blueBin = 0;

		for (int i = 0; i < patch.length; i++) {
			redBin += Color.red(patch[i]);
			greenBin += Color.green(patch[i]);
			blueBin += Color.blue(patch[i]);
		}

		return Color.rgb(redBin / patch.length, greenBin / patch.length, blueBin / patch.length);
	}

	private int computeMin(int[] minArray) {

		int min = minArray[0];
		for (int i : minArray) {
			if (i < min)
				min = i;
		}

		return min;
	}

	private int computeMax(int[] maxArray) {

		int max = maxArray[0];
		for (int i : maxArray) {
			if (i > max)
				max = i;
		}

		return max;
	}

	// Handling touch events
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		currentTab = HapticCanvasActivity.mTabHost.getCurrentTabTag();
		if (currentTab == "Brush") {
			HapticCanvasActivity.mTabHost.setCurrentTabByTag("Edit");
			return false;

		} else if (currentTab == "File")
			HapticCanvasActivity.mTabHost.setCurrentTabByTag("Feel");

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:

			px = event.getX();
			py = event.getY();

			vx = 0;
			vy = 0;

			// Start a new velocity tracker
			if (vTracker == null) {
				vTracker = VelocityTracker.obtain();
			} else {
				vTracker.clear();
			}
			vTracker.addMovement(event);

			bitmapTasks();

			// Call the timeout timer
			touchTimer = System.nanoTime();

			// Set touching to true
			isTouching = true;

			break;

		case MotionEvent.ACTION_MOVE:
			// Update old positions

			// Update cursor positions
			px_old = px;
			py_old = py;

			px = event.getX();
			py = event.getY();

			vTracker.addMovement(event);

			// Compute velocity in pixels per 1 ms
			vTracker.computeCurrentVelocity(1);

			// get current velocities
			vx = vTracker.getXVelocity();
			vy = vTracker.getYVelocity();

			Log.i("Velocities: ", String.valueOf(vx) + " " + String.valueOf(vy));

			bitmapTasks();

			if (!isDrawing) {

				pixelToTexture(myDrawBitmap.getPixel((int) px, (int) py));

			}

			if (isDrawing) {

				synchronized (myDrawCanvas) {

					if (eraserOn) {
						myDrawCanvas.drawLine(px_old, py_old, px, py, eraser);
					} else
						myDrawCanvas.drawLine(px_old, py_old, px, py, brush);
				}
			}

			touchTimer = System.nanoTime();

			break;

		case MotionEvent.ACTION_UP:

			isTouching = false;
			touchTimer = System.nanoTime();
			myActivity.sendTPad(0f);
			break;

		case MotionEvent.ACTION_CANCEL:
			vTracker.recycle();
			break;
		}

		return true;
	}

	// Below is the main background thread for performing other tasks
	// On this thread we do the drawing to the screen, and updating of state
	// variables except the finger position and friction.
	public void run() {

		// Make sure thread is running
		while (isRunning) {
			// checks to make sure the holder has a valid surfaceview in it,
			// if not then skip
			if (!holder.getSurface().isValid()) {
				continue;
			}

			displayString = String.valueOf(friction);

			/*
			 * The following code is where we do all of the drawing to the screen.
			 * 
			 * Drawing to the PostCanvas does not reflect bitmap changes
			 */

			// Lock canvas so we can manpipulate it
			myPostCanvas = holder.lockCanvas();

			synchronized (myBackgroundBitmap) {
				myPostCanvas.drawBitmap(myBackgroundBitmap, 0, 0, null);
			}

			if (isDrawing) {
				synchronized (myDrawCanvas) {
					myPostCanvas.drawBitmap(myDrawBitmap, 0, 0, hapticPaint);
				}
			}

			// Unlock the canvas and draw it to the screen
			holder.unlockCanvasAndPost(myPostCanvas);

		}

	}

	// Takes care of stopping our drawing thread when the system is paused
	public void pause() {
		isRunning = false;
		while (true) {
			try {
				myThread.join();
				break;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		myThread = null;
	}

	// Takes care of resuming our thread when the system is resumed
	public void resume() {
		isRunning = true;
		myThread = new Thread(this);
		myThread.setPriority(Thread.MAX_PRIORITY - 3);
		myThread.start();

	}

	// Takes care of resuming our thread when the system is resumed
	public void destroy() {

		myBackgroundBitmap.recycle();
		myDrawBitmap.recycle();
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

		// int stringId = logContext.getApplicationInfo().labelRes;
		// String appName = logContext.getString(stringId);

		saveFile = new File(file_path, formattedDate + " " + "Haptic Canvas" + ".txt");

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
			fw.write(msg + "\r\n");
			fw.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	public void saveFile() {

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/Haptic Images");

		String file_path = mediaStorageDir.getPath();
		OutputStream fOut = null;

		File dir = new File(file_path);

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd. h:mm:ss a");
		String formattedDate = sdf.format(date);

		if (!dir.exists()) {
			dir.mkdirs();
		}
		dir = new File(file_path, formattedDate + ".png");

		try {
			fOut = new FileOutputStream(dir);
			HapticCanvasActivity.myHapticView.getDrawBitmap().compress(Bitmap.CompressFormat.PNG, 100, fOut);
			fOut.flush();
			fOut.close();
			// sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(dir)));
		} catch (IOException e) {
			Log.i("Fail:", " didn't flush stream");
			e.printStackTrace();
		}

	}

}
