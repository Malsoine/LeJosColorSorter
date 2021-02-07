package lejos_colorsorter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import lejos.hardware.Button;
/*leJOS doc here : http://lejos.org/ev3/docs/ */
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

/*
 *    LCD
 *    ------> x
 *    |
 *    |
 *    ↓
 * 	  y
 */

public class Ev3 {

	static EV3ColorSensor colorSensor;
	static SampleProvider colorProvider;
	static float[] colorSample;
	static List colorMapping = Arrays
			.asList(new String[] { "None", "Black", "Blue", "Green", "Yellow", "Red", "White", "Brown" });

	public static boolean playSound(String soundFileName) {
		/*
		 * Current sound list : success - error - full
		 * 
		 * Current bug : sometimes the sound is played normally but the program crash
		 * just after, freezing the Ev3 without raising an exception or returning
		 * anything
		 */
		File f = new File(soundFileName + ".wav");
		return f.canRead() && Sound.playSample(f) >= 0;
	}

	public static void displayColor() {
		colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("S3"));
		colorProvider = colorSensor.getColorIDMode();
		colorSample = new float[colorProvider.sampleSize()];
		
		while(Button.UP.isDown()) {
			colorProvider.fetchSample(colorSample, 0);
			float colorId = colorSample[0];
			String colorName = (String) colorMapping.get((int) colorId);
			LCD.clear();
			LCD.drawString(colorName, 1, 1);
			Delay.msDelay(1000);
		}
		LCD.clear();
		colorSensor.close();
	}

	public static void main(String[] args) {
		LCD.setAutoRefresh(true);
		LCD.drawString("Groupe 5 - ok", 1, 1);
		Delay.msDelay(3000);

		displayColor();

		Delay.msDelay(3000);
	}
}
