package components;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.utility.Delay;

public class Slide {

	EV3MediumRegulatedMotor slideMotor;
	boolean pointingUp;

	public Slide(String port) {
		slideMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort(port));
		pointingUp = false;
	}

	public void resetPosition() {
		// No try catch to ensure process crash if we are unable to reach a safe state
		if (pointingUp) {
			slideMotor.rotate(180);
			pointingUp = false;
		}
	}

	/* Eject a brick from the slide */
	public boolean ejectOneBrick() {
		boolean success = true;
		resetPosition();

		try {
			slideMotor.rotate(-180);
			pointingUp = true;
		} catch (Exception e) {
			success = false;
		}
		if (success) {  // If rotation failed we don't try to rotate back 
			try {
				slideMotor.rotate(180);
				pointingUp = false;
			} catch (Exception e) {
				success = false;
			}
		}
		return success;
	}

	/* Eject <number> bricks from the slide */
	public void ejectBricks(int number) {
		for (int i = 0; i < number; i++) {
			ejectOneBrick();
			Delay.msDelay(100);
		}
	}

	public void close() {
		slideMotor.close();
	}
}
