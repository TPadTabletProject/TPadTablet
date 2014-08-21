import android.view.VelocityTracker;
import android.view.MotionEvent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.nio.FloatBuffer;

/**
 * dynamicHaptic is a class that helps to manage spatial textures based
 *  in off of pictures for many different applications. 
 * 
 * By sending touch information, this class can handle all of the magic
 * of turning the touches in haptic feedback based off a picture. This
 * class will calculate velocity, investigate upcoming pixels, and send the
 * info to the TPad.
 * 
 * To initialize the class it is recommend to pass in a bitmap in the onCreate()
 * function, or whenever you want to change the picture used to define the haptic
 * texture. Then, call hapticFeedback in your touch listener with the registered
 * MotionEvents.
 * 
 */
public class dynamicHaptic {
	private Bitmap dataBitmap;
	private float scaleFactor;
	private Matrix scaleMat;

	private VelocityTracker vTracker;
	private static float vy, vx;
	private static float py, px;

	private static final int PREDICT_HORIZON = (int) (1000 * (.100f)); // 100 samples
	private static float[] predictedPixels = new float[PREDICT_HORIZON];

	/**
	 * setDataBitmap
	 * 
	 * Sets the initial Bitmap to draw data from
	 */
	public void setDataBitmap(Bitmap bmp) {
		dataBitmap = null;
		dataBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
	}

	/**
	 * resetScaleFactor 
	 * 
	 * scales the Bitmap
	 */
	private void resetScaleFactor(int viewWidth) {
		Log.i("dynamicHaptic", "resetting scale factor");
		scaleMat = null;
		scaleMat = new Matrix();
		Log.i("dynamicHaptic", "View Width: " + viewWidth);
		scaleFactor = dataBitmap.getWidth() / (float) viewWidth;
		scaleMat.postScale(1 / scaleFactor, 1 / scaleFactor);
		Log.i("dynamicHaptic", "ScaleFactor: " + scaleFactor);
	}

	/**
	 * predictPixels()
	 * 
	 * Looks ahead of touches to see the upcoming pixels
	 */
	private void predictPixels() {
		float friction;
		int x = (int) px;
		int y = (int) py;
		Log.i("dynamicHaptic", "Predicting pixels starting");
		Log.i("dynamicHaptic", "x: " + px + " y: " + py);


		for (int i = 0; i < predictedPixels.length; i++) {
			Log.i("dynamicHaptic", "dataBitmap width: " + dataBitmap.getWidth());
			Log.i("dynamicHaptic", "vx, vy:" + vx + ", " + vy);

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
			Log.i("dynamicHaptic", "friction: " + friction);
		}
	}

	/**
	 * pixelToFriction
	 * 
	 * converts the upcoming pixels to a friction value
	 */
	private float pixelToFriction(int pixel) {
		float[] hsv = new float[3];
		Color.colorToHSV(pixel, hsv);
		return hsv[2];
	}

	/**
	 * hapticFeedback
	 * 
	 * This is the function you want to call in a touch listener.
	 * 
	 * Pass through the MotionEvent that is registered and this function
	 * will handle the conversion to TPad values.
	 */
	public boolean hapticFeedback(MotionEvent event) {
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				//sendTPad(0.8f);
				Log.i("dynamicHaptic", "action down");
				Log.i("dynamicHaptic", "scale factor: " + scaleFactor);

				px = event.getX() * scaleFactor;
				py = event.getY() * scaleFactor;

				Log.i("dynamicHaptic", "px, py: " + event.getX() + ", " + event.getY());

				
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
				sendTPadBuffer(predictedPixels);

				break;

			case MotionEvent.ACTION_UP:
				sendTPad(0f);
				break;

			case MotionEvent.ACTION_CANCEL:
				vTracker.recycle();
				break;
		}

		return true;
	}
}

	
