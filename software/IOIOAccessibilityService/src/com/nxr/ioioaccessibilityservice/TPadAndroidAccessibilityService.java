package com.nxr.ioioaccessibilityservice;

import nxr.tpadnexus.lib.TPadTexture;

import android.content.Intent;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import android.os.Handler;

public class TPadAndroidAccessibilityService extends TPadNexusService {

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
//		final Toast toast = Toast.makeText(getApplicationContext(), ":" + event.getText(), Toast.LENGTH_SHORT);
//		toast.show();
//		
//		 Handler handler = new Handler();
//         handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                toast.cancel(); 
//            }
//         }, 1000);
		//Log.i("Access", "Incoming Accessibility event: " + event.getEventType() + " " + event.getText());
		int eventType = event.getEventType();
		String eventTest = "";
		Log.i("Access", "A Event: " + event.eventTypeToString(eventType) + " .. Info: " + event.getText());
		switch(eventType) {
			case AccessibilityEventCompat.TYPE_TOUCH_INTERACTION_END:
				sendTPad(0f);
				break;
		case AccessibilityEventCompat.TYPE_VIEW_HOVER_ENTER:
			Log.i("Access", "Type View Hover Event: "  + event.getContentDescription() + " " + event.getText());
			sendTPad(0f);
			hasVibrated = false;
			TPadTexture waveType;
			float hapticHover;
			String content;
			if (event.getContentDescription() != null) {
				eventTest = "TYPE_VIEW_HOVER_ENTER";
				content = (String) event.getContentDescription();
				hapticHover = 200f;
			}
			else {
				eventTest = "TYPE_VIEW_HOVER_ENTER" + "Content Description: " + event.getContentDescription() + " View text: " + event.getText();
				//		         		Toast.makeText(getApplicationContext(), eventTest, Toast.LENGTH_SHORT).show();
				content = event.getText().toString();
				content = content.substring(1, content.length() - 1);
				hapticHover = 200f;
				//		        		waveType = convertWaveType(content);
			}

			hapticConverter(content);
			if (!hasVibrated) {
				AccessibilityNodeInfo info = event.getSource();
				if (info != null) {
					////	        				Log.e("TAG", info.getViewIdResourceName());
					functionHaptics(info);	        				
				}
			}
			break;
			
		case AccessibilityEventCompat.TYPE_VIEW_HOVER_EXIT:
			//		            eventTest = "TYPE_VIEW_HOVER_EXIT";
			//		            eventTest = eventTest + "Content Description: " + event.getContentDescription() + " View text: " + event.getText();
			//		    		Toast.makeText(getApplicationContext(), eventTest, Toast.LENGTH_SHORT).show();
//					    		sendTPad(0f);
			break;
		}
		//				eventTest = eventTest + "Content Description: " + event.getContentDescription();
		//				Toast.makeText(getApplicationContext(), eventTest, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}

	/*
	 * onStartCommand is the first command to be run
	 * 
	 * (non-Javadoc)
	 * @see com.nxr.ioioaccessibilityservice.IOIOAccessibilityService#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(getBaseContext(), "onStartCmd called v2", Toast.LENGTH_SHORT).show();
		//Log.i("TPad", "onCreate Accessibility");
		return super.onStartCommand(intent, flags, startId);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.i("TPad", "onStart");
		super.onStart(intent, startId);
		Toast.makeText(getBaseContext(), "onStart started", Toast.LENGTH_LONG).show();
	}

	/*
	 * hapticConverter(String content)
	 * 
	 * Here's where the magic happens. content contains the name where the user's
	 * finger is, and we can convert that into an application name.
	 */
	
	private void hapticConverter(String content) {
		
		if (content.length() == 1) {
			hasVibrated = true;
			if (content.charAt(0) == 'f') {
				sendTPadTexture(TPadTexture.SQUARE, 100f, .5f);
			}
			else if (content.charAt(0) == 'j') {
				sendTPadTexture(TPadTexture.SQUARE, 200f, .5f);
			}
			else {
				sendTPad(1f);
			}
			//Toast.makeText(getApplicationContext(), "Character", Toast.LENGTH_SHORT).show();
			final Toast toast = Toast.makeText(getApplicationContext(), "Character", Toast.LENGTH_SHORT);
	        toast.show();

	        Handler handler = new Handler();
	            handler.postDelayed(new Runnable() {
	               @Override
	               public void run() {
	                   toast.cancel(); 
	               }
	        }, 1000);
		}
		else {
			if (content.equals("Dropbox")) {
				sendTPadTexture(TPadTexture.SQUARE, 150f,  1f);
				hasVibrated = true;
				//Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
			}
			else if (content.equals("Back")) {
				sendTPadTexture(TPadTexture.SQUARE, 4.5f, 1f);
				//Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
				hasVibrated = true;
			}
			else if (content.equals("Home")) {
				sendTPadDualTexture(TPadTexture.SINUSOID, 25f, 1f, TPadTexture.SINUSOID, 1.05f, 1f);
				//Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
				hasVibrated = true;
			}
			else if (content.equals("Recent apps")) {
				sendTPadDualTexture(TPadTexture.SQUARE, 6f, 1f, TPadTexture.SQUARE, 1.5f, 1f);
				//Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
				hasVibrated = true;
			}
			else if (content.equals("Gmail")) {
				sendTPadTexture(TPadTexture.SINUSOID, 50f, 1f);
				//Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
				hasVibrated = true;
			}
			else if (content.equals("Workshop Demo")) {
				sendTPadTexture(TPadTexture.SQUARE, 12f, 1f);
				//Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
				hasVibrated = true;
			}
			else if (content.equals("Settings")) {
				sendTPadTexture(TPadTexture.SAWTOOTH, 150f, 1f);
				//Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
				hasVibrated = true;
			}
			else if (content.equals("Chrome")) {
				sendTPadTexture(TPadTexture.SINUSOID, 100f, 1f);
				//Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
				hasVibrated = true;
			}
			else if (content.equals("Button")) {
				sendTPadTexture(TPadTexture.SINUSOID, 100f, 1f);
				//Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
				hasVibrated = true; 
			}
			else if (content.startsWith("Home screen")) {
				//				sendTPadTexture(TPadTexture.SQUARE, 20f,  1f);
				Log.i("TPad", "Home");
				hasVibrated = true;
				//Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
			}
			else if (content.split(" ").equals("Wi-Fi")) {
				sendTPadTexture(TPadTexture.SINUSOID, 20f, .3f);
				//Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
				hasVibrated = true;
			}
			else if (content.split(" ").equals("Wi-Fi")) {
				sendTPadTexture(TPadTexture.SQUARE, 50f, .5f);
				//Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
				hasVibrated = true;
			}
			else if (content.split(" ").equals("Wi-Fi")) {
				sendTPadTexture(TPadTexture.SINUSOID, 80f, .7f);
				//Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
				hasVibrated = true;
			}
			else if (content.split(" ").equals("Wi-Fi")) {
				sendTPadTexture(TPadTexture.SQUARE, 110f, 1f);
				//Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
				hasVibrated = true;
			}
			
		}
	}

	private void functionHaptics(AccessibilityNodeInfo info) {
		if (info.isEditable()) {
			sendTPadTexture(TPadTexture.SAWTOOTH, 100f, 1f);
			//Toast.makeText(getApplicationContext(), "Editable", Toast.LENGTH_SHORT).show();
		}
		else if (info.isCheckable()) {
			sendTPadTexture(TPadTexture.SINUSOID, 200f, 1f);
			//Toast.makeText(getApplicationContext(), "Checkable", Toast.LENGTH_SHORT).show();
		}
		else if (info.isClickable()) {
			sendTPadTexture(TPadTexture.SQUARE, 200f, 1f);
			//Toast.makeText(getApplicationContext(), "Clickable", Toast.LENGTH_SHORT).show();
		}

	}
	

}
