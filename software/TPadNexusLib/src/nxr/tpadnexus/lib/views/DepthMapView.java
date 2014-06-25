package nxr.tpadnexus.lib.views;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import nxr.tpadnexus.lib.TPadNexusActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

public class DepthMapView extends View {

	private String TAG = new String("DepthMapView");
	private int height, width;

	private TPadNexusActivity tpadActivity;
	private final Context mainContext;

	private Paint dataPaint;
	private float scaleFactor;
	private Matrix scaleMat;

	private boolean openCvLoaded = false;

	private static volatile Bitmap dataBitmap = null;
	private static volatile Bitmap gradXBitmap = null;
	private static volatile Bitmap gradYBitmap = null;

	private VelocityTracker vTracker;
	private static float vy, vx;
	private static float py, px;

	private static final int PREDICT_HORIZON = (int) (1000 * (.100f)); // 100 samples
	private static float[] predictedPixels = new float[PREDICT_HORIZON];

	public DepthMapView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mainContext = context;

		tpadActivity = (TPadNexusActivity) mainContext;

		Bitmap defaultBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
		setDataBitmap(defaultBitmap);

		dataPaint = new Paint();
		dataPaint.setColor(Color.DKGRAY);
		dataPaint.setAntiAlias(true);

		scaleMat = new Matrix();
		scaleFactor = 1;
		scaleMat.postScale(1 / scaleFactor, 1 / scaleFactor);

		Log.i(TAG, "Trying to load OpenCV library");
		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_7, mainContext, mOpenCVCallBack)) {
			Log.e(TAG, "Cannot connect to OpenCV Manager");
		}

	}

	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(getContext()) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.e("TEST", "Connected to OpenCV Manager");
				computeGradients();
				openCvLoaded = true;
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public void setDataBitmap(Bitmap bmp) {
		dataBitmap = null;
		dataBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
		resetScaleFactor();
		invalidate();
	}

	private void computeGradients() {

		gradXBitmap = null;
		gradYBitmap = null;

		gradXBitmap = dataBitmap.copy(Bitmap.Config.ARGB_8888, true);
		gradYBitmap = dataBitmap.copy(Bitmap.Config.ARGB_8888, true);

		double delta = 127.5;
		Mat tempMat = new Mat(dataBitmap.getHeight(), dataBitmap.getWidth(), CvType.CV_8UC4);
		Mat gradMatx = new Mat(dataBitmap.getHeight(), dataBitmap.getWidth(), CvType.CV_8UC4);
		Mat gradMaty = new Mat(dataBitmap.getHeight(), dataBitmap.getWidth(), CvType.CV_8UC4);
		Utils.bitmapToMat(dataBitmap, tempMat);
		Imgproc.cvtColor(tempMat, tempMat, Imgproc.COLOR_RGBA2GRAY);
		Utils.matToBitmap(tempMat, dataBitmap);

		// x direction gradient
		Imgproc.Sobel(tempMat, gradMatx, tempMat.depth(), 1, 0, 5, .5, delta);
		Imgproc.GaussianBlur(gradMatx, gradMatx, new Size(7, 7), 10);
		// Imgproc.GaussianBlur(gradMatx, gradMatx, new Size(11, 11), 20);
		Utils.matToBitmap(gradMatx, gradXBitmap);

		// y direction gradient
		Imgproc.Sobel(tempMat, gradMaty, tempMat.depth(), 0, 1, 5, .5, delta);
		Imgproc.GaussianBlur(gradMaty, gradMaty, new Size(7, 7), 10);
		// Imgproc.GaussianBlur(gradMaty, gradMaty, new Size(11, 11), 20);
		Utils.matToBitmap(gradMaty, gradYBitmap);
	}

	private void resetScaleFactor() {
		scaleMat = null;
		scaleMat = new Matrix();
		scaleFactor = dataBitmap.getWidth() / (float) width;
		scaleMat.postScale(1 / scaleFactor, 1 / scaleFactor);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.MAGENTA);
		canvas.drawBitmap(dataBitmap, scaleMat, dataPaint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		width = MeasureSpec.getSize(widthMeasureSpec);
		height = MeasureSpec.getSize(heightMeasureSpec);
		resetScaleFactor();
		invalidate();
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (openCvLoaded == false) {
			return false;
		}

		switch (event.getActionMasked()) {

		case MotionEvent.ACTION_DOWN:
			px = event.getX() * scaleFactor;
			py = event.getY() * scaleFactor;

			vx = 0;
			vy = 0;

			// Start a new velocity tracker
			if (vTracker == null) {
				vTracker = VelocityTracker.obtain();
			} else {
				vTracker.clear();
			}
			vTracker.addMovement(event);

			break;

		case MotionEvent.ACTION_MOVE:
			px = event.getX() * scaleFactor;
			py = event.getY() * scaleFactor;

			vTracker.addMovement(event);

			// Compute velocity in pixels per 1 ms
			vTracker.computeCurrentVelocity(1);

			// get current velocities
			vx = vTracker.getXVelocity() * scaleFactor;
			vy = vTracker.getYVelocity() * scaleFactor;

			predictPixels();
			tpadActivity.sendTPadBuffer(predictedPixels);

			break;

		case MotionEvent.ACTION_UP:
			tpadActivity.sendTPad(0f);
			break;

		case MotionEvent.ACTION_CANCEL:
			vTracker.recycle();
			break;
		}

		return true;
	}

	private void predictPixels() {
		float friction;
		int x = (int) px;
		int y = (int) py;

		float vAvgx;
		float vAvgy;
		float vAvgMag;

		for (int i = 0; i < predictedPixels.length; i++) {

			x = (int) (px + vx * i); // 1st order hold in x direction
			if (x >= dataBitmap.getWidth()) {
				x = dataBitmap.getWidth() - 1;
			} else if (x < 0)
				x = 0;

			y = (int) (py + vy * i); // 1st order hold in y direction
			if (y >= dataBitmap.getHeight()) {
				y = dataBitmap.getHeight() - 1;
			} else if (y < 0)
				y = 0;

			float getX = pixelToFriction(gradXBitmap.getPixel(x, y));
			float getY = pixelToFriction(gradYBitmap.getPixel(x, y));

			vAvgMag = (float) Math.sqrt(vx * vx + vy * vy);
			vAvgx = (float) (vx / vAvgMag);
			vAvgy = (float) (vy / vAvgMag);

			friction = (float) (-1 * ((vAvgx * (getX - .5f) + vAvgy * (getY - .5f)) * Math.sqrt(2.) / 2.) + .5);

			predictedPixels[i] = friction;
		}

	}

	private float pixelToFriction(int pixel) {
		float[] hsv = new float[3];
		Color.colorToHSV(pixel, hsv);
		return hsv[2];
	}

}
