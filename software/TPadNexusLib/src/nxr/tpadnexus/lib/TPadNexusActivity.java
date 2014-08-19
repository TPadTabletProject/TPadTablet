package nxr.tpadnexus.lib;

import java.nio.FloatBuffer;
/*
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
*/
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

public abstract class TPadNexusActivity extends Activity {//IOIOActivity {
	public final static int BUFFER_SIZE = 1000;
	public final static float MAX_VOLTAGE = 154;
	public final static float DEAD_VOLTAGE = 116;
	public final static long TextureSampleRate = 1000; // 1kHz output rate

	private static float TPadValue;
	private static volatile boolean textureOn = false;
	private static FloatBuffer tpadValueBuffer = FloatBuffer.allocate(BUFFER_SIZE);
	private static FloatBuffer tpadTextureBuffer = FloatBuffer.allocate(BUFFER_SIZE);

	long timeoutTimer;
	long loopTimer;
	int timeoutMillis = 1000;
	//private Looper looper;

	private int TPadFreq = 10000;

	/**
	 * Keystone Service FLAGS
	 */
	public final int GET_TPAD_FREQ = 1;
	public final int VOLTAGE_TO_PWM = 2;
	public final int SEND_TPAD_TEXTURE = 3;
	public final int SEND_TPAD = 4;
	public final int SEND_TPAD_BUFFER = 5;


	//	SINUSOID, SQUARE, SAWTOOTH, TRIANGLE, RANDOM
	public final int iSINUSOID = 1;
	public final int iSQUARE = 2;
	public final int iSAWTOOTH = 3;
	public final int iTRIANGLE = 4;
	public final int iRANDOM = 5;
	

	Messenger myService = null;
	boolean isBound;
	
	class ResponseHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			int respCode = msg.what;
			
			switch(respCode) {
				case 1:
					setFreq(msg.getData().getInt("Freq"));
					break;
			} 
		}
	}
	
	
	public void sendMessage() {
		if (!isBound) return;
		Message msg = Message.obtain(null, GET_TPAD_FREQ);
        msg.replyTo = new Messenger(new ResponseHandler());
		Bundle bundle = new Bundle();
		bundle.putString("MyString", "Message Received");
		bundle.putInt("method", GET_TPAD_FREQ);

		msg.setData(bundle);
		
		try {
			myService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * IOIO loop classes
	 */
	
	/*
	class Looper extends BaseIOIOLooper {
		private PwmOutput pwmOutput_;
		private DigitalOutput led_;
		private int freq;

		@Override
		public void setup() throws ConnectionLostException {
			led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
			pwmOutput_ = ioio_.openPwmOutput(12, TPadFreq);
			freq = TPadFreq;
			tpadValueBuffer.clear();
			tpadTextureBuffer.clear();
		}

		@Override
		public void loop() throws ConnectionLostException {
			loopTimer = System.nanoTime() / 1000000;
			synchronized (tpadValueBuffer) {

				if (tpadValueBuffer.hasRemaining()) {
					ioio_.beginBatch();

					led_.write(false);
					TPadValue = tpadValueBuffer.get();

					pwmOutput_.setDutyCycle(voltageToPwm(TPadValue * MAX_VOLTAGE));
					ioio_.endBatch();

					timeoutTimer = System.currentTimeMillis();

				} else if (textureOn) {

					synchronized (tpadTextureBuffer) {
						if (tpadTextureBuffer.hasRemaining()) {
							tpadValueBuffer.clear();
							tpadValueBuffer.put(tpadTextureBuffer);
							tpadValueBuffer.flip();
						} else
							tpadValueBuffer.rewind();
					}
				} else {
					led_.write(true);
				}
			}

			if (freq != TPadFreq) {
				pwmOutput_.close();
				pwmOutput_ = ioio_.openPwmOutput(12, TPadFreq);
				freq = TPadFreq;
			}

			// check if we haven't sent a new tpad value in awhile. If so, TURN OFF THE TPAD!
			if (timeoutTimer + timeoutMillis < System.currentTimeMillis())
				pwmOutput_.setDutyCycle(0f);

			// wait until the end of our refresh period. Ensures more precise timings
			while ((loopTimer + 1) > (System.nanoTime() / 1000000))
				;
		}
	}
*/
	public void setFreq(int i) {
		TPadFreq = i;
	}
/*
	public void sendTPad(float f) {
		synchronized (tpadValueBuffer) {
			textureOn = false;
			tpadValueBuffer.clear();
			tpadValueBuffer.put(f);
			tpadValueBuffer.flip();
		}

	}
	*/
	public void sendTPad(float f) {
		Log.i("TPad", "(sendTPad) Step 3: " + isBound);
		if (!isBound) return;
		Message msg = Message.obtain(null, SEND_TPAD);		
        msg.replyTo = new Messenger(new ResponseHandler());
		Bundle bundle = new Bundle();
		bundle.putFloat("f", f);
		msg.setData(bundle);

		try {
			myService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		} 
	}

	/*
	public void sendTPadBuffer(float[] buffArray) {
		synchronized (tpadValueBuffer) {
			textureOn = false;
			tpadValueBuffer.clear();
			tpadValueBuffer.put(buffArray);
			tpadValueBuffer.flip();
		}
	}
	*/

	public void sendTPadBuffer(float[] buffArray) {
		Log.i("TPad", "Step 3: " + isBound);
		if (!isBound) return;
		Message msg = Message.obtain(null, SEND_TPAD_BUFFER);		
        msg.replyTo = new Messenger(new ResponseHandler());
		Bundle bundle = new Bundle();
		bundle.putFloatArray("buffArray", buffArray);
		msg.setData(bundle);
		try {
			myService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
/*
	public void sendTPadTexture(TPadTexture type, float freq, float amp) {

		int periodSamps = (int) ((1 / freq) * TextureSampleRate);

		synchronized (tpadTextureBuffer) {

			tpadTextureBuffer.clear();
			tpadTextureBuffer.limit(periodSamps);

			float tp = 0;

			switch (type) {

			case SINUSOID:

				for (float i = 0; i < periodSamps; i++) {

					tp = (float) ((1 + Math.sin(2 * Math.PI * freq * i / TextureSampleRate)) / 2f);

					tpadTextureBuffer.put(amp * tp);

				}

				break;
			case SAWTOOTH:
				for (float i = 0; i < periodSamps; i++) {

					tpadTextureBuffer.put(amp * (i / periodSamps));

				}
				break;
			case RANDOM:
				break;
			case TRIANGLE:
				for (float i = 0; i < periodSamps / 2; i++) {

					tpadTextureBuffer.put(amp * tp++ * 2 / periodSamps);

				}
				for (float i = periodSamps / 2; i < periodSamps; i++) {

					tpadTextureBuffer.put(amp * tp-- * 2 / periodSamps);

				}

				break;
			case SQUARE:

				for (float i = 0; i < tpadTextureBuffer.limit(); i++) {

					tp = (float) ((1 + Math.sin(2 * Math.PI * freq * i / TextureSampleRate)) / 2f);

					if (tp > (.5)) {
						tpadTextureBuffer.put(amp);

					} else
						tpadTextureBuffer.put(0);

				}

				break;
			default:
				break;

			}

			tpadTextureBuffer.flip();
		}

		synchronized (tpadValueBuffer) {
			textureOn = true;
		}

	}
*/

	public void sendTPadTexture(int type, float freq, float amp) {
		if (!isBound) return;
		Message msg = Message.obtain(null, SEND_TPAD_TEXTURE);
		
        msg.replyTo = new Messenger(new ResponseHandler());
		Bundle bundle = new Bundle();
		bundle.putInt("type", type);
		bundle.putFloat("freq", freq);
		bundle.putFloat("amp", amp);
		msg.setData(bundle);
		try {
			Log.i("TPad", "send message");
			myService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	
	public void sendTPadDualTexture(TPadTexture type1, float freq1, float amp1, TPadTexture type2, float freq2, float amp2) {
		float minfreq = Math.min(freq1, freq2);
		int periodSamps = (int) ((1 / minfreq) * TextureSampleRate);
		float[] tempArray = new float[periodSamps];
		float tp = 0;
		switch (type1) {
		case SINUSOID:
			for (int i = 0; i < periodSamps; i++) {
				tp = (float) ((1 + Math.sin(2 * Math.PI * freq1 * i / TextureSampleRate)) / 2f);
				tempArray[i] = amp1 * tp;
			}
			break;
		case SAWTOOTH:
			for (int i = 0; i < periodSamps; i++) {
				tempArray[i] = amp1 * (i / periodSamps);
			}
			break;
		case SQUARE:
			for (int i = 0; i < periodSamps; i++) {
				tp = (float) ((1 + Math.sin(2 * Math.PI * freq1 * i / TextureSampleRate)) / 2f);
				if (tp > (.5)) {
					tempArray[i] = amp1;
				} else
					tempArray[i] = 0;
			}
			break;
		default:
			break;
		}
		switch (type2) {
		case SINUSOID:
			for (int i = 0; i < periodSamps; i++) {
				tp = (float) ((1 + Math.sin(2 * Math.PI * freq2 * i / TextureSampleRate)) / 2f);
				tempArray[i] *= amp2 * tp;
			}
			break;
		case SAWTOOTH:
			for (int i = 0; i < periodSamps; i++) {
				tempArray[i] *= amp2 * (i / periodSamps);
			}
			break;
		case SQUARE:
			for (int i = 0; i < periodSamps; i++) {
				tp = (float) ((1 + Math.sin(2 * Math.PI * freq2 * i / TextureSampleRate)) / 2f);
				if (tp > (.5)) {
					tempArray[i] *= amp2;

				} else
					tempArray[i] *= 0;
			}
			break;
		default:
			break;
		}
		synchronized (tpadTextureBuffer) {
			tpadTextureBuffer.clear();
			tpadTextureBuffer.limit(periodSamps);
			tpadTextureBuffer.put(tempArray);
			tpadTextureBuffer.flip();
		}
		synchronized (tpadValueBuffer) {
			textureOn = true;
		}
	}



	public void addTextureBuff() {
		synchronized (tpadValueBuffer) {
			tpadValueBuffer.clear();
			tpadValueBuffer.put(tpadTextureBuffer.array());
			tpadValueBuffer.flip();
		}
	}

	
	
	private float voltageToPwm(float voltage) {
		float duty = 0;
		// Linear approximations below found from calibration of a tpad amp
		if (voltage <= DEAD_VOLTAGE) {
			duty = .5472f * voltage + 2.0482f;

		} else if (voltage > DEAD_VOLTAGE) {
			duty = 1.9789f * voltage - 47.158f;
		}

		duty = (duty / 256f) * .5f;
		Log.i("TPad Duty", String.valueOf(duty));
		return duty;
	}
	
	/*
	private float voltageToPwm() {
		if (!isBound) return;
		Message msg = Message.obtain(null, VOLTAGE_TO_PWM);
        msg.replyTo = new Messenger(new ResponseHandler());
		Bundle bundle = new Bundle();
		bundle.putString("MyString", "Message Received");
		bundle.putInt("method", GET_TPAD_FREQ);

		msg.setData(bundle);
		
		try {
			myService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = new Intent("nxr.tpad.KeystoneService");
		bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
	}

	/**
	 * Subclasses should call this method from their own onDestroy() if overloaded. It takes care of connecting with the IOIO.
	 */
	@Override
	protected void onDestroy() {
		unbindService(myConnection);
		super.onDestroy();
	}

	/**
	 * Subclasses should call this method from their own onStart() if overloaded. It takes care of connecting with the IOIO.
	 */
	@Override
	protected void onStart() {
		super.onStart();
	}

	/**
	 * Subclasses should call this method from their own onStop() if overloaded. It takes care of disconnecting from the IOIO.
	 */
	@Override
	protected void onStop() {
		super.onStop();
	}
/*
	@Override
	protected IOIOLooper createIOIOLooper() {
		looper = new Looper();
		return looper;
	}
	*/
	/*
	 * Functions to communicate with Keystone service
	 * to get device specific info
	 */
	
	private ServiceConnection myConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			myService = new Messenger(service);
			isBound = true;
			sendMessage();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			myService = null;
			isBound = false;
		}
	};
	
	
}