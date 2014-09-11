package nxr.tpadnexus.lib;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;

import java.nio.FloatBuffer;

import android.os.Bundle;
import android.util.Log;

public abstract class TPadNexusFragmentActivity extends IOIOFragmentActivity {
	public final static int BUFFER_SIZE = 1000;
	public final static long TextureSampleRate = 1000; // 1kHz output rate

	private static float TPadValue;
	private static volatile boolean textureOn = false;
	private static FloatBuffer tpadValueBuffer = FloatBuffer.allocate(BUFFER_SIZE);
	private static FloatBuffer tpadTextureBuffer = FloatBuffer.allocate(BUFFER_SIZE);

	long timeoutTimer;
	long loopTimer;
	int timeoutMillis = 1000;
	private Looper looper;

	private int TPadFreq = 35450;

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
					pwmOutput_.setDutyCycle((float) (TPadValue / 2.0));
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

		int periodSamps = (int) ((1 / freq) * TextureSampleRate);

		synchronized (tpadTextureBuffer) {

			tpadTextureBuffer.clear();
			tpadTextureBuffer.limit(periodSamps);

			float tp = 0;

			switch (type) {

			case SINUSOID:

				for (float i = 0; i < periodSamps; i++) {

					tp = (float) ((1 / 2f + Math.sin(2 * Math.PI * freq * i / TextureSampleRate)) / 2f);

					tpadTextureBuffer.put(amp * tp);

				}

				break;
			case SAWTOOTH:
				for (float i = 0; i < periodSamps; i++) {

					tpadTextureBuffer.put(amp * (i % (float)periodSamps));

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

					tp = (float) ((1 / 2f + Math.sin(2 * Math.PI * freq * i / TextureSampleRate)) / 2f);

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
	
		if(periodSamps > 1000) {
			periodSamps = 1000;
			Log.e("TPad", "ERROR: periodSamps > 1000, readjusting");
		}
		
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
				tempArray[i] = amp1 * (i % (float)periodSamps);

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
				tempArray[i] *= amp2 * (i % (float)periodSamps);

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	public long getTextureSampleRate() {
		return TextureSampleRate;
	}

	/**
	 * Subclasses should call this method from their own onDestroy() if overloaded. It takes care of connecting with the IOIO.
	 */
	@Override
	protected void onDestroy() {
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

	@Override
	protected IOIOLooper createIOIOLooper() {
		looper = new Looper();
		return looper;
	}
}