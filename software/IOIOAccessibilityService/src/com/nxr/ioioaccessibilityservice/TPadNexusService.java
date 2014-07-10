package com.nxr.ioioaccessibilityservice;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import android.util.Log;

import java.nio.FloatBuffer;

import nxr.tpadnexus.lib.TPadTexture;


/*
 * TPadSurface is a class to hold the TPad texture functionality. 
 * This class sits between the IOIO parent and the app child. 
 * 
 * All necessary TPad data can be contained in here, with methods
 * for apps to call. 
 *
 */

public abstract class TPadNexusService extends IOIOAccessibilityService {
 
	public final static int BUFFER_SIZE = 1000;
	public final static float MAX_VOLTAGE = 154;
	public final static float DEAD_VOLTAGE = 116;
	public final static long TextureSampleRate = 1000; // 1kHz output rate
	
	protected boolean hasVibrated = false;
	
	private static float TPadValue;
	private static volatile boolean textureOn = false;
	private static FloatBuffer tpadValueBuffer = FloatBuffer.allocate(BUFFER_SIZE);
	private static FloatBuffer tpadTextureBuffer = FloatBuffer.allocate(BUFFER_SIZE);
	
	long timeoutTimer;
	long loopTimer;
	int timeoutMillis = 1000;
	
	private int TPadFreq = 32870;
	
	private Looper looper;

	
	class Looper extends BaseIOIOLooper {
		private PwmOutput pwmOutput_;
		private PwmOutput testPWM;
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
	
	
	public void setFreq(int i) {
		TPadFreq = i;
	}
	
	public void sendTPad(float f) {
		synchronized (tpadValueBuffer) {
			textureOn = false;
			tpadValueBuffer.clear();
			tpadValueBuffer.put(f);
			tpadValueBuffer.flip();
		}

	}
	
	public void sendTPadBuffer(float[] buffArray) {
		synchronized (tpadValueBuffer) {
			textureOn = false;
			tpadValueBuffer.clear();
			tpadValueBuffer.put(buffArray);
			tpadValueBuffer.flip();
		}
	}

	public void sendTPadTexture(TPadTexture type, float freq, float amp) {
		Log.i("TPad", "Send Texture");
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

	//Changed to protected when moved
	protected float voltageToPwm(float voltage) {
		float duty = 0;

		// Linear approximations below found from calibration of a tpad amp
		if (voltage <= DEAD_VOLTAGE) {
			duty = .5472f * voltage + 2.0482f;

		} else if (voltage > DEAD_VOLTAGE) {
			duty = 1.9789f * voltage - 47.158f;
		}

		duty = duty / 256 * .5f;				

		return duty;
	}
	
	@Override
	protected IOIOLooper createIOIOLooper() {
		looper = new Looper();
		return looper;
	}
	


}


