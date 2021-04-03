package components;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.SensorMode;

public class TouchSensor {

	EV3TouchSensor touchSensor;

	SensorMode sensorProvider;
	float[] touchSample;

	public TouchSensor(String port) {
		touchSensor = new EV3TouchSensor(LocalEV3.get().getPort(port));
		sensorProvider = touchSensor.getTouchMode();
		touchSample = new float[sensorProvider.sampleSize()];
	}

	public boolean captorTouched() {
		sensorProvider.fetchSample(touchSample, 0);
		return touchSample[0] == 1;
	}

	public void close() {
		touchSensor.close();
	}
}
