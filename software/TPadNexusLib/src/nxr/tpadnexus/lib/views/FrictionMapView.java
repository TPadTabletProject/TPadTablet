package nxr.tpadnexus.lib.views;

import nxr.tpadnexus.lib.TPadNexusActivity;
import android.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

public class FrictionMapView extends View {
	private int height, width;

	private TPadNexusActivity tpadActivity;

	private Bitmap dataBitmap;
	private Paint dataPaint;
	private float scaleFactor;
	private Matrix scaleMat;

	private VelocityTracker vTracker;
	private static float vy, vx;
	private static float py, px;

	private static final int PREDICT_HORIZON = (int) (1000 * (.100f)); // 100 samples
	private static float[] predictedPixels = new float[PREDICT_HORIZON];

	public FrictionMapView(Context context, AttributeSet attrs) {
		super(context, attrs);

		tpadActivity = (TPadNexusActivity) context;

		Bitmap defaultBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
		setDataBitmap(defaultBitmap);

		dataPaint = new Paint();
		dataPaint.setColor(Color.DKGRAY);
		dataPaint.setAntiAlias(true);

		scaleMat = new Matrix();
		scaleFactor = 1;
		scaleMat.postScale(1 / scaleFactor, 1 / scaleFactor);

	}

	public void setDataBitmap(Bitmap bmp) {
		dataBitmap = null;
		dataBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
		invalidate();

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

			friction = pixelToFriction(dataBitmap.getPixel(x, y));

			predictedPixels[i] = friction;
		}

	}

	private float pixelToFriction(int pixel) {
		float[] hsv = new float[3];
		Color.colorToHSV(pixel, hsv);
		return hsv[2];
	}

}
