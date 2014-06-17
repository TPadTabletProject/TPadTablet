package nxr.tpadnexus.lib;

import java.nio.FloatBuffer;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import android.os.Bundle;
import android.util.Log;

public abstract class TPadFlowNexusFragmentActivity extends IOIOFragmentActivity {
	private static final String TAG = "TPadFlowFragmentActivity";
	private static final float MAX_VOLTAGE = 154;
	private static final float DEAD_VOLTAGE=116;
	public final static int BUFFER_SIZE = 1000; // gives us 1 second of buffer
	public final static long TextureSampleRate = 1000; // 1kHz output rate
	public final static float StrokeSampleRate = 200; // 100Hz stroke playback

	private static float TPadValue;
	private static volatile boolean textureOn = false;
	private static volatile boolean strokeOn = false;
	private static FloatBuffer tpadFrictionBuffer = FloatBuffer.allocate(BUFFER_SIZE);
	private static FloatBuffer tpadTextureBuffer = FloatBuffer.allocate(BUFFER_SIZE);
	private static FloatBuffer tpadVolumeBuffer = FloatBuffer.allocate(BUFFER_SIZE * 4);
	private static FloatBuffer tpadStrokeBuffer = FloatBuffer.allocate(BUFFER_SIZE * 4);

	private static float volPower = 2.4f;

	long timeoutTimer;
	long loopTimer;
	private boolean resetAmps = false;
	private static final int timeoutMillis = 500;
	private Looper looper;

	private long reScaleCount = 0;

	private int TPadFreq = 35450;

	class Looper extends BaseIOIOLooper {
		private PwmOutput pwmOutput_;
		private DigitalOutput led_;
		public TwiMaster twi12_, twi34_;

		private final byte[] FullVolume = new byte[] { (byte) 0x9F };
		private final byte[] Mute = new byte[] { (byte) 0x80 };
		private final byte[] TurnOn = new byte[] { 0x1C };
		private final byte[] TurnOff = new byte[] { 0x00 };
		private final byte[] Reset = new byte[] { 0x24 };
		private final byte[] Volume = new byte[] { 0x00 };

		private int freq;

		@Override
		public void setup() throws ConnectionLostException {
			ioio_.beginBatch();
			led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
			pwmOutput_ = ioio_.openPwmOutput(12, TPadFreq);
			pwmOutput_.setDutyCycle(0);

			twi12_ = ioio_.openTwiMaster(0, TwiMaster.Rate.RATE_400KHz, false);
			twi34_ = ioio_.openTwiMaster(1, TwiMaster.Rate.RATE_400KHz, false);

			twi12_.writeReadAsync(0x7c, false, TurnOn, TurnOn.length, null, 0);
			twi12_.writeReadAsync(0x7c, false, Reset, Reset.length, null, 0);

			twi34_.writeReadAsync(0x7c, false, TurnOn, TurnOn.length, null, 0);
			twi34_.writeReadAsync(0x7c, false, Reset, Reset.length, null, 0);

			twi12_.writeReadAsync(0x7d, false, TurnOn, TurnOn.length, null, 0);
			twi12_.writeReadAsync(0x7d, false, Reset, Reset.length, null, 0);

			twi34_.writeReadAsync(0x7d, false, TurnOn, TurnOn.length, null, 0);
			twi34_.writeReadAsync(0x7d, false, Reset, Reset.length, null, 0);

			ioio_.endBatch();

			freq = TPadFreq;
			tpadFrictionBuffer.clear();
			tpadTextureBuffer.clear();
			tpadVolumeBuffer.clear();

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void loop() throws ConnectionLostException {
			loopTimer = System.nanoTime() / 1000000;

			ioio_.beginBatch();

			// First part will deal with getting and setting the correct friction command
			synchronized (tpadFrictionBuffer) {
/*
				if (resetAmps) {
					twi12_.writeReadAsync(0x7c, false, Reset, Reset.length, null, 0);

					twi34_.writeReadAsync(0x7c, false, Reset, Reset.length, null, 0);

					twi12_.writeReadAsync(0x7d, false, Reset, Reset.length, null, 0);

					twi34_.writeReadAsync(0x7d, false, Reset, Reset.length, null, 0);
					resetAmps = false;
				}
*/
				if (tpadFrictionBuffer.hasRemaining()) {

					// led_.write(false);
					TPadValue = tpadFrictionBuffer.get();
					pwmOutput_.setDutyCycle(voltageToPwm(TPadValue*MAX_VOLTAGE));

					timeoutTimer = System.currentTimeMillis();

				} else if (textureOn) {

					synchronized (tpadTextureBuffer) {
						if (tpadTextureBuffer.hasRemaining()) {
							tpadFrictionBuffer.clear();
							tpadFrictionBuffer.put(tpadTextureBuffer);
							tpadFrictionBuffer.flip();
						} else
							tpadFrictionBuffer.rewind();

					}
				} else {
					// led_.write(true);

				}

			}

			synchronized (tpadVolumeBuffer) {
				if (tpadVolumeBuffer.remaining()>3) {
					//if (reScaleCount % 5 == 0) {
						setVolume(1, computeVolume(volPower, tpadVolumeBuffer.get()), 1);
						setVolume(2, computeVolume(volPower, tpadVolumeBuffer.get()), 1);
						setVolume(3, computeVolume(volPower, tpadVolumeBuffer.get()), 1);
						setVolume(4, computeVolume(volPower, tpadVolumeBuffer.get()), 1);
					//}
					//reScaleCount++;
				} else if (strokeOn) {

					synchronized (tpadStrokeBuffer) {
						if (tpadStrokeBuffer.hasRemaining()) {
							tpadVolumeBuffer.clear();
							tpadVolumeBuffer.put(tpadStrokeBuffer);
							tpadVolumeBuffer.flip();
						} else
							tpadVolumeBuffer.rewind();
					}

				}

			}

			ioio_.endBatch();

			// Timeout on the tpad for safety/battery reasons
			//if (timeoutTimer + timeoutMillis < System.currentTimeMillis())
			//	pwmOutput_.setDutyCycle(0f);

			// Check if the freqency has been changed
			if (freq != TPadFreq) {
				pwmOutput_.close();
				pwmOutput_ = ioio_.openPwmOutput(12, TPadFreq);
				freq = TPadFreq;
			}

			//while (System.nanoTime() / 1000000 - loopTimer < 1)
			//	;

		}

		/**
		 * Pow should be a value 0-10f which defines the relative power law.
		 * <p>
		 * Val should be a value 0-1f which defines the relative volume.
		 * <p>
		 * 
		 * @return scaled volume number that is then sent to amplifiers
		 */
		public short computeVolume(float pow, float val) {

			return (short) (((Math.log10((val)) + pow) / pow * 31.0));
		}

		/**
		 * 
		 * <p>
		 * Val should be a value 0-100 which defines the relative volume.
		 * <p>
		 * 
		 * @return scaled volume number that is then sent to amplifiers
		 */
		public void setVolume(int num, short vol, int chan) throws ConnectionLostException {

			short addr;
			int channel;
			TwiMaster i2c;
			// i2c is the twi master associated with each amp

			byte[] data = new byte[1];

			switch (num) {
			case 1:
				addr = 0x7c;
				i2c = twi12_;
				break;
			case 2:
				addr = 0x7d;
				i2c = twi12_;
				break;
			case 3:
				addr = 0x7c;
				i2c = twi34_;
				break;
			case 4:
				addr = 0x7d;
				i2c = twi34_;
				break;
			default:
				addr = 0x00;
				i2c = twi12_;
				Log.e(TAG, "Address Invalid. Set to 0x7C");

				break;
			}

			if (chan == 1)
				channel = 0x80;
			else if (chan == 2)
				channel = 0x60;
			else {
				channel = 0x80;
				Log.e(TAG, "Channel Invalid. Set to 1");
			}

			if (vol < 0)
				vol = 0;

			data[0] = (byte) (channel + vol);

			i2c.writeReadAsync((byte) addr, false, data, data.length, null, 0);

		}

	}

	public void setFreq(int i) {
		TPadFreq = i;
	}

	public void setVolPower(float pow) {
		volPower = pow;
	}

	public void resetAmps() {
		resetAmps = true;
	}

	public void sendTPad(float fric) {
		synchronized (tpadFrictionBuffer) {
			textureOn = false;
			tpadFrictionBuffer.clear();
			tpadFrictionBuffer.put(fric);
			tpadFrictionBuffer.flip();
		}

	}

	public void sendTPad(float fric, float vol1, float vol2, float vol3, float vol4) {
		synchronized (tpadFrictionBuffer) {
			textureOn = false;
			tpadFrictionBuffer.clear();
			tpadFrictionBuffer.put(fric);
			tpadFrictionBuffer.flip();

			strokeOn = false;
			tpadVolumeBuffer.clear();
			tpadVolumeBuffer.put(vol1);
			tpadVolumeBuffer.put(vol2);
			tpadVolumeBuffer.put(vol3);
			tpadVolumeBuffer.put(vol4);
			tpadVolumeBuffer.flip();
		}

	}

	public void sendTPad(float vol1, float vol2, float vol3, float vol4) {

		synchronized (tpadFrictionBuffer) {

			strokeOn = false;
			tpadVolumeBuffer.clear();
			tpadVolumeBuffer.put(vol1);
			tpadVolumeBuffer.put(vol2);
			tpadVolumeBuffer.put(vol3);
			tpadVolumeBuffer.put(vol4);
			tpadVolumeBuffer.flip();
		}

	}
	
	public void sendTPadBuffer(float[] fric, float vol1, float vol2, float vol3, float vol4) {
		synchronized (tpadFrictionBuffer) {
			textureOn = false;
			tpadFrictionBuffer.clear();
			tpadFrictionBuffer.put(fric);
			tpadFrictionBuffer.flip();

			strokeOn = false;
			tpadVolumeBuffer.clear();
			tpadVolumeBuffer.put(vol1);
			tpadVolumeBuffer.put(vol2);
			tpadVolumeBuffer.put(vol3);
			tpadVolumeBuffer.put(vol4);
			tpadVolumeBuffer.flip();
		}

	}

	public void sendTPadBuffer(float[] buffArray) {
		synchronized (tpadFrictionBuffer) {
			textureOn = false;
			tpadFrictionBuffer.clear();
			tpadFrictionBuffer.put(buffArray);
			tpadFrictionBuffer.flip();
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

			textureOn = true;
		}

	}

	public void sendTPadStroke(float secs, float x1, float y1, float x2, float y2) {

		int periodSamps = (int) ((secs) * StrokeSampleRate);

		float dY = (y2 - y1) / (periodSamps - 1);
		float dX = (x2 - x1) / (periodSamps - 1);

		float[] tempVols = new float[4];

		synchronized (tpadStrokeBuffer) {

			tpadStrokeBuffer.clear();
			tpadStrokeBuffer.limit(periodSamps * 4 + periodSamps / 2 * 4);

			for (int i = 0; i < periodSamps / 4; i++) {
				tempVols = scaleRelativeVolumes(x1, y1);
				tpadStrokeBuffer.put(0);
				tpadStrokeBuffer.put(0);
				tpadStrokeBuffer.put(0);
				tpadStrokeBuffer.put(0);

			}

			for (int i = 0; i < periodSamps; i++) {

				tempVols = scaleRelativeVolumes(x1 + i * dX, y1 + i * dY);

				tpadStrokeBuffer.put(tempVols[0]);
				tpadStrokeBuffer.put(tempVols[1]);
				tpadStrokeBuffer.put(tempVols[2]);
				tpadStrokeBuffer.put(tempVols[3]);

			}

			for (int i = 0; i < periodSamps / 4; i++) {
				tempVols = scaleRelativeVolumes(x1, y1);
				tpadStrokeBuffer.put(0);
				tpadStrokeBuffer.put(0);
				tpadStrokeBuffer.put(0);
				tpadStrokeBuffer.put(0);

			}

			tpadStrokeBuffer.flip();

			strokeOn = true;
		}

	}

	public void sendTpadStroke(TPadStrokeDirection dir, float duration, float SOA) {

		
		int periodSamps = (int) ((duration + SOA) * StrokeSampleRate);

		float pulseDuration = duration;

		float buffDuration = duration/5f;	

		int buffSamps = (int) (buffDuration * StrokeSampleRate) + 1;

		int pulseSamps = (int) (pulseDuration * StrokeSampleRate);
		float buffCount1 = 0f;
		float buffCount2 = 0f;
		float[] tempVols = new float[4];
		

		synchronized (tpadStrokeBuffer) {

			tpadStrokeBuffer.clear();
			tpadStrokeBuffer.limit(periodSamps * 4 + buffSamps * 4 + (int) StrokeSampleRate * 4);

			for (float t = 0; t < SOA + pulseDuration + buffDuration; t = t + 1f / StrokeSampleRate) {

				switch (dir) {
				case DOWN:
					if (t >= 0 && t < buffDuration) { // before first intervals

						tempVols[0] = buffCount1;
						tempVols[1] = buffCount1;
						buffCount1 += 1f / buffSamps;

					} else if (t >= buffDuration && t < pulseDuration + buffDuration) { // first interval
						tempVols[0] = 1;
						tempVols[1] = 1;
					} else if (t >= pulseDuration + buffDuration && t < pulseDuration + 2 * buffDuration) {

						buffCount1 -= 1f / buffSamps;

						tempVols[0] = buffCount1;
						tempVols[1] = buffCount1;

					} else {
						tempVols[0] = 0;
						tempVols[1] = 0;
					}

					if (t > (SOA - buffDuration) && t < SOA) {

						tempVols[2] = buffCount2;
						tempVols[3] = buffCount2;
						buffCount2 += 1f / buffSamps;

					} else if ((t >= SOA) && t < (SOA + pulseDuration)) {
						tempVols[2] = 1;
						tempVols[3] = 1;

					} else if (t >= (SOA + pulseDuration) && t < (SOA + pulseDuration + buffDuration)) {
						buffCount2 -= 1f / buffSamps;
						tempVols[2] = buffCount2;
						tempVols[3] = buffCount2;
					} else {

						tempVols[2] = 0;
						tempVols[3] = 0;

					}

					break;
				case LEFT:
					if (t >= 0 && t < buffDuration) { // before first intervals

						tempVols[1] = buffCount1;
						tempVols[3] = buffCount1;
						buffCount1 += 1f / buffSamps;

					} else if (t >= buffDuration && t < pulseDuration + buffDuration) { // first interval
						tempVols[1] = 1;
						tempVols[3] = 1;
					} else if (t >= pulseDuration + buffDuration && t < pulseDuration + 2 * buffDuration) {

						buffCount1 -= 1f / buffSamps;

						tempVols[1] = buffCount1;
						tempVols[3] = buffCount1;

					} else {
						tempVols[1] = 0;
						tempVols[3] = 0;
					}

					if (t > (SOA - buffDuration) && t < SOA) {

						tempVols[0] = buffCount2;
						tempVols[2] = buffCount2;
						buffCount2 += 1f / buffSamps;

					} else if ((t >= SOA) && t < (SOA + pulseDuration)) {
						tempVols[0] = 1;
						tempVols[2] = 1;

					} else if (t >= (SOA + pulseDuration) && t < (SOA + pulseDuration + buffDuration)) {
						buffCount2 -= 1f / buffSamps;
						tempVols[0] = buffCount2;
						tempVols[2] = buffCount2;
					} else {

						tempVols[0] = 0;
						tempVols[2] = 0;

					}
					break;
				case RIGHT:

					if (t >= 0 && t < buffDuration) { // before first intervals

						tempVols[0] = buffCount1;
						tempVols[2] = buffCount1;
						buffCount1 += 1f / buffSamps;

					} else if (t >= buffDuration && t < pulseDuration + buffDuration) { // first interval
						tempVols[0] = 1;
						tempVols[2] = 1;
					} else if (t >= pulseDuration + buffDuration && t < pulseDuration + 2 * buffDuration) {

						buffCount1 -= 1f / buffSamps;

						tempVols[0] = buffCount1;
						tempVols[2] = buffCount1;

					} else {
						tempVols[0] = 0;
						tempVols[2] = 0;
					}

					if (t > (SOA - buffDuration) && t < SOA) {

						tempVols[1] = buffCount2;
						tempVols[3] = buffCount2;
						buffCount2 += 1f / buffSamps;

					} else if ((t >= SOA) && t < (SOA + pulseDuration)) {
						tempVols[1] = 1;
						tempVols[3] = 1;

					} else if (t >= (SOA + pulseDuration) && t < (SOA + pulseDuration + buffDuration)) {
						buffCount2 -= 1f / buffSamps;
						tempVols[1] = buffCount2;
						tempVols[3] = buffCount2;
					} else {

						tempVols[1] = 0;
						tempVols[3] = 0;

					}
					break;
				case UP:
					if (t >= 0 && t < buffDuration) { // before first intervals

						tempVols[2] = buffCount1;
						tempVols[3] = buffCount1;
						buffCount1 += 1f / buffSamps;

					} else if (t >= buffDuration && t < pulseDuration + buffDuration) { // first interval
						tempVols[2] = 1;
						tempVols[3] = 1;
					} else if (t >= pulseDuration + buffDuration && t < pulseDuration + 2 * buffDuration) {

						buffCount1 -= 1f / buffSamps;

						tempVols[2] = buffCount1;
						tempVols[3] = buffCount1;

					} else {
						tempVols[2] = 0;
						tempVols[3] = 0;
					}

					if (t > (SOA - buffDuration) && t < SOA) {

						tempVols[0] = buffCount2;
						tempVols[1] = buffCount2;
						buffCount2 += 1f / buffSamps;

					} else if ((t >= SOA) && t < (SOA + pulseDuration)) {
						tempVols[0] = 1;
						tempVols[1] = 1;

					} else if (t >= (SOA + pulseDuration) && t < (SOA + pulseDuration + buffDuration)) {
						buffCount2 -= 1f / buffSamps;
						tempVols[0] = buffCount2;
						tempVols[1] = buffCount2;
					} else {

						tempVols[0] = 0;
						tempVols[1] = 0;

					}
					break;

				}

				tpadStrokeBuffer.put(tempVols[0]);
				tpadStrokeBuffer.put(tempVols[1]);
				tpadStrokeBuffer.put(tempVols[2]);
				tpadStrokeBuffer.put(tempVols[3]);

			}
			for (int i = 0; i < (int) StrokeSampleRate- 1; i++) {

				tpadStrokeBuffer.put(0);
				tpadStrokeBuffer.put(0);
				tpadStrokeBuffer.put(0);
				tpadStrokeBuffer.put(0);

			}

			tpadStrokeBuffer.flip();
		}
		synchronized (tpadStrokeBuffer) {
			strokeOn = true;
		}

	}

	/**
	 * 
	 * @param x
	 *            X Coordinate from 0-1f
	 * @param y
	 *            Y Coordinate form 0-1f
	 * @return array of floats representing scaled volumes
	 */
	public float[] scaleRelativeVolumes(float x, float y) {
		float[] vols = new float[4];

		vols[0] = ((1f - y) / 1f) * (x / 1f);
		vols[1] = ((1f - y) / 1f) * ((1f - x) / 1f);
		vols[2] = (y / 1f) * (x / 1f);
		vols[3] = (y / 1f) * ((1f - x) / 1f);

		return vols;
	}
	
	private float voltageToPwm(float voltage){
		float duty = 0;
		
		// Linear approximations below found from calibration of a tpad amp
		if(voltage<=DEAD_VOLTAGE){
			duty = .5472f*voltage+2.0482f;
			
		}else if(voltage>DEAD_VOLTAGE){
			duty = 1.9789f*voltage-47.158f;
		}
		
		duty = duty/256*.5f;
		
		return duty;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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