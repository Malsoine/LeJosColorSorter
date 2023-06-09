package components;

import java.util.List;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3MediumRegulatedMotor;

public class Conveyor {

	List bucketColorMapping;

	EV3MediumRegulatedMotor conveyerMotor;

	TouchSensor touchSensor;

	public Conveyor(String port, String portTouchSensor) {
		conveyerMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort(port));
		touchSensor = new TouchSensor(portTouchSensor);
	}

	/*
	 * TODO fix more or less arbitrary 115
	 */
	public boolean move(int bucketIndex) {
		resetPosition();
		conveyerMotor.rotate(bucketIndex * 155);//old value 115
		return true;
	}

	public void resetPosition() {
		while (!this.touchSensor.captorTouched()) {
			conveyerMotor.rotate(-40);
		}
	}

	public void close() {
		conveyerMotor.close();
	}
}
