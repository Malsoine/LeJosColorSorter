package components;

import java.util.Arrays;
import java.util.List;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3MediumRegulatedMotor;

public class Conveyor {

	/* TODO pass this as arg in case it changes */
	static final List BUCKETCOLORMAPPING = Arrays.asList(new String[] { "Blue", "Yellow", "Red", "Green", "Trash" });

	EV3MediumRegulatedMotor conveyerMotor;

	TouchSensor touchSensor;

	public Conveyor(String port, String portTouchSensor) {
		conveyerMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort(port));
		touchSensor = new TouchSensor(portTouchSensor);
	}

	/*
	 * TODO fix arbitrary 110
	 */
	public void move(String color) {
		resetPosition();
		conveyerMotor.rotate(BUCKETCOLORMAPPING.indexOf(color) * 110);
	}

	public void resetPosition() {
		while (!this.touchSensor.captorTouched()) {
			conveyerMotor.rotate(-50);
		}
	}

	public void close() {
		conveyerMotor.close();
	}
}
