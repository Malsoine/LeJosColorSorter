package components;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.utility.Delay;

public class Slide {

	EV3MediumRegulatedMotor slideMotor;

	public Slide(String port) {
		slideMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort(port));
	}

	/* Eject a brick from the slide */
	public void ejectOneBrick() {
		slideMotor.rotate(-180);
		slideMotor.rotate(180);
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
