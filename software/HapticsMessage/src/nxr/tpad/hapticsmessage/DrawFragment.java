package nxr.tpad.hapticsmessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import nxr.tpadnexus.lib.TPadTexture;

public class DrawFragment extends Fragment {
	static MainActivity mainActivity;
	ServerFragment serverView;
	Bitmap defaultHapMap;
	int lastID = 1000;
	int viewIDCount = 0;
	public final String dl = ";";

	public static final String[] optionsNames = new String[] {"bumpsbig","bumpssmallsobel","bumpssmall", "bumpssmallvert","bumpssmall2", "defaultmap", "crescendo", "decrescendo", "dipdipdiiiip", "forward100px50px", "forwardback100px25px25px", "reflectedchirp125pxto5px",
			"reflectedlinear5px", "reflectedlinear10px", "reflectedlinear10px_lightened", "reflectedlinear10px85dark", "reflectedlinear10px170dark", "reflectedlinear20px", "reflectedlinear30px",
			"reflectedlinear60px", "repeatedlinear5px", "repeatedlinear10px", "repeatedlinear30px", "repeatedlinear60px", "speedbump5px", "whitenoise50", "whitenoise200", "whitenoise400",
			"widebumps2", "widedimples2" };

	public Bitmap[] hapMaps;

	private static int bitmapmargin = 1;

	public DrawView drawView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		BitmapFactory.Options options = new BitmapFactory.Options();

		options.inScaled = false;

		hapMaps = new Bitmap[optionsNames.length];

		for (int i = 0; i < optionsNames.length; i++) {
			hapMaps[i] = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(optionsNames[i], "drawable", getActivity().getPackageName()), options);
		}

		defaultHapMap = hapMaps[0];

		RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(500, 100);

		params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		drawView = new DrawView(getActivity());
		drawView.setLayoutParams(params1);
		drawView.s = "default";

		drawView.setFocusable(true);
		drawView.setFocusableInTouchMode(true);

		return drawView;
	}

	private String timestamp() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd h:mm:ss:SSS a");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	public void recieveMessage(int i, String s, Boolean recieve) {

		RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(500, 100);
		if (recieve) {
			Log.i("R ", "Message: " + s + " " + "Texture: " + DrawFragment.optionsNames[i]);
			mainActivity.writeToLog(timestamp() + dl + "RecieveMessage" + dl + s + dl + "Texture" + dl + DrawFragment.optionsNames[i]);
			params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		} else {
			Log.i("S ", "Message: " + s + " " + "Texture: " + DrawFragment.optionsNames[i]);
			mainActivity.writeToLog(timestamp() + dl + "SentMessage" + dl + s + dl + "Texture" + dl + DrawFragment.optionsNames[i]);
			params1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		}
		DrawView dView = new DrawView(getActivity());

		params1.bottomMargin = 5;
		params1.topMargin = 15;
		dView.setLayoutParams(params1);

		if (lastID == 1000) {
			params1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		} else {
			params1.addRule(RelativeLayout.BELOW, lastID - 1);
		}
		dView.setId(lastID);
		lastID++;
		mainActivity.chatView.addView(dView);

		dView.reDraw(i);
		if (recieve) {
			dView.feel = true;
			dView.recieve = true;
		} else {
			dView.feel = false;
			dView.showText = true;
		}
		dView.s = s;

		mainActivity.scrollView.post(new Runnable() {
			@Override
			public void run() {
				mainActivity.scrollView.fullScroll(View.FOCUS_DOWN);
				mainActivity.textBox.requestFocus();
			}
		});
	}

	class DrawView extends View implements OnTouchListener {
		Paint text, paint;

		private Bitmap hapticMap;
		private String hapticName;

		private VelocityTracker vTracker;
		private double vy, vx;

		private static final int PREDICT_HORIZON = (int) (1000 * (.040f)); // 1000Hz times 32ms = 32samples
		private static final float border = 5;
		private float[] predictedPixels = new float[PREDICT_HORIZON];

		Boolean startedLeft = false;
		Boolean recieve = false;
		Boolean showText = false;
		Boolean feel = true;
		String s = "";
		int UID = 0;

		double lastTime, currentTime;

		private volatile float px, py = 0;
		private int bx, by = 0;
		private float[] hsv = new float[3];

		public DrawView(Context context) {
			super(context);
			hapticMap = defaultHapMap;
			UID = viewIDCount++;
			setFocusable(true);
			setFocusableInTouchMode(true);

			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setStrokeWidth(5f);
			paint.setColor(Color.rgb(200, 100, 100));
			paint.setAlpha(100);

			text = new Paint(Paint.ANTI_ALIAS_FLAG);
			text.setAntiAlias(true);
			text.setStrokeWidth(0f);
			text.setStyle(Paint.Style.STROKE);

			this.setOnTouchListener(this);

			reDraw(0);
		}

		public void reDraw(int o) {

			hapticMap = hapMaps[o];
			hapticName = optionsNames[o];

			invalidate();
		}

		@Override
		public void onDraw(Canvas canvas) {
			canvas.drawARGB(255, 150, 150, 150);
			if (showText) {

				paint.setColor(Color.rgb(200, 200, 200));
				paint.setAlpha(255);
				canvas.drawRect(border, border, 500f - border, 100f - border, paint);

				if (recieve) {
					paint.setColor(Color.rgb(150, 150, 255));
					paint.setAlpha(100);
					canvas.drawRect(border, border, 500f - border, 100f - border, paint);
				}

				text.setTextSize(20f);
				text.setColor(Color.BLACK);
				canvas.drawText(s, 20, 40, text);

			} else if (this != drawView) {

				paint.setColor(Color.rgb(200, 200, 200));
				paint.setAlpha(255);
				canvas.drawRect(border, border, 500 - border, 100 - border, paint);

				text.setTextSize(60f);
				text.setColor(Color.BLACK);
				canvas.drawText("Swipe to Open", 20, 60, text);

				paint.setColor(Color.rgb(150, 150, 255));
				paint.setAlpha(100);
				canvas.drawRect(border, border, px, 100f - border, paint);

			} else {

				canvas.drawBitmap(hapticMap, 0, 0, text);
			}
		}

		private void predictPixels() {
			float friction;
			int x, y;

			for (int i = 0; i < predictedPixels.length; i++) {

				x = (int) (px + vx * i); // 1st order hold in x direction
				if (x >= hapticMap.getWidth()) {
					x = hapticMap.getWidth() - 1;
				} else if (x < 0)
					x = 0;

				y = (int) (py + vy * i); // 1st order hold in y direction
				if (y >= hapticMap.getHeight()) {
					y = hapticMap.getHeight() - 1;
				} else if (y < 0)
					y = 0;

				friction = pixelToFriction(hapticMap.getPixel(x, y));
				// Log.i("Friction: ", String.valueOf(friction));
				predictedPixels[i] = friction;
			}

		}

		private float pixelToFriction(int color) {
			/*
			 * HSV holds the data for each pixel hsv[0] is hue 0-360 hsv[1] is saturation 0-1 hsv[2] is value 0-1
			 */

			Color.colorToHSV(color, hsv);
			return hsv[2];
		}

		@Override
		public boolean onTouch(View view, MotionEvent event) {

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:

				px = event.getX();
				py = event.getY();
				mainActivity.writeToLog(timestamp() + dl + "TouchDown" + dl + String.valueOf(UID) + dl + s + dl + hapticName);
				if (px < 100) {
					startedLeft = true;
				}

				vx = 0;
				vy = 0;

				// Start a new velocity tracker
				if (vTracker == null) {
					vTracker = VelocityTracker.obtain();
				} else {
					vTracker.recycle();
					vTracker = VelocityTracker.obtain();
				}
				vTracker.addMovement(event);
				invalidate();
				break;

			case MotionEvent.ACTION_MOVE:

				px = event.getX();
				py = event.getY();

				vTracker.addMovement(event);

				// Compute velocity in pixels per 1 ms
				vTracker.computeCurrentVelocity(1);

				// get current velocities
				vx = vTracker.getXVelocity();
				vy = vTracker.getYVelocity();

				if (px >= hapticMap.getWidth() - bitmapmargin)
					bx = hapticMap.getWidth() - bitmapmargin;
				else if (px <= bitmapmargin)
					bx = bitmapmargin;
				else
					bx = (int) px;
				if (py >= hapticMap.getHeight() - bitmapmargin)
					by = hapticMap.getHeight() - bitmapmargin;
				else if (py <= bitmapmargin)
					by = bitmapmargin;
				else
					by = (int) py;
				/*
				 * int pixel = hapticMap.getPixel(bx, by);
				 * 
				 * mainActivity.sendTPadTexture(TPadTexture.SINUSOID, 50f, pixelToFriction(pixel));
				 */

				//if (Math.abs(vx) > .2 || Math.abs(vy) > .1) {

					predictPixels();
					mainActivity.sendTPadBuffer(predictedPixels);

				//} else {

				//	int pixel = hapticMap.getPixel(bx, by);

				//	mainActivity.sendTPad(pixelToFriction(pixel));

				//}

				if ((px > 500 - 50) && (startedLeft == true) && (recieve)) {
					showText = true;

				}
				invalidate();

				break;

			case MotionEvent.ACTION_UP:
				px = event.getX();
				py = event.getY();
				mainActivity.writeToLog(timestamp() + dl + "TouchUp" + dl + String.valueOf(UID) + dl + s + dl + hapticName);
				if (px < 500 - 100) {
					startedLeft = false;
				}

				if (showText == false) {
					px = 0;
					invalidate();
				}

				mainActivity.sendTPad(0);

				break;
			case MotionEvent.ACTION_CANCEL:
				mainActivity.writeToLog(timestamp() + dl + "TouchCancel" + dl + String.valueOf(UID) + dl + s + dl + hapticName);
				break;

			}

			return true;

		}
	}
}
