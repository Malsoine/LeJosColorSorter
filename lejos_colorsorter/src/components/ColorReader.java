package components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class ColorReader {

	EV3ColorSensor colorSensor;
	SampleProvider colorProvider;
	float[] colorSample;

	int colorIndex;
	static final List COLORMAPPING = Arrays.asList(new String[] { "Red", "Green", "Blue", "Yellow" });
	
	public ColorReader(String port) {
		colorSensor = new EV3ColorSensor(LocalEV3.get().getPort(port));
		colorProvider = colorSensor.getColorIDMode();
		colorSample = new float[colorProvider.sampleSize()];
	}

	public boolean startScanningProcess(ArrayList brickSlide) {
		/**/
		while (brickSlide.size() < 8 && Button.ENTER.isUp()) {
			LCD.clear();
			LCD.drawString("Scan piece here", 0, 0);
			LCD.drawString("------->", 0, 1);

			colorProvider.fetchSample(colorSample, 0);
			colorIndex = (int) colorSample[0];

			if (colorIndex > -1 && colorIndex < 4) {
				String colorName = (String) COLORMAPPING.get(colorIndex);
				// playSound("success");
				LCD.clear();
				LCD.drawString(colorName.toUpperCase()+" scanned", 0, 4);
				brickSlide.add(colorName);
				LCD.drawString("Slide count:" + brickSlide.size(), 0, 5);
				LCD.drawString("<- Brick", 0, 6);
				Delay.msDelay(750);
			}
			Delay.msDelay(750);
		}
		return true;
	}
	
	public void close() {
		colorSensor.close();
	}
}
