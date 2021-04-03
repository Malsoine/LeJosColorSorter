package lejos_colorsorter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import components.ColorReader;
import components.Conveyor;
import components.Slide;
import lejos.hardware.Sound;

public class Ev3Controller {

	ArrayList slideBricks; // brick are stored as String due to old JRE but Enum would have been better

	boolean inAction; // Store if controller is already performing an action. Decorator handling
						// setting value to true or false at end or beginning of each method would have
						// been great but again JRE restriction

	// Mapping stored here for compliance and coherence
	static final List BUCKETCOLORMAPPING = Arrays.asList(new String[] { "Blue", "Yellow", "Red", "Green" });

	ColorReader colorReader;
	Conveyor conveyor;
	Slide slide;

	public Ev3Controller() {
		slideBricks = new ArrayList();
		inAction = false;

		// Port mapping is made here
		colorReader = new ColorReader("S4");
		conveyor = new Conveyor("D", "S1", BUCKETCOLORMAPPING);
		slide = new Slide("A");
	}

	boolean playSound(String soundFileName) {
		/*
		 * Current sound list : success - error - full
		 * 
		 * Bug : sometimes the sound is played normally but the program crash just
		 * after, freezing the Ev3 without raising an exception or returning anything
		 */
		File f = new File(soundFileName + ".wav");
		return f.canRead() && Sound.playSample(f) >= 0;
	}

	boolean sortAllBricksOnSlide() {
		inAction = true;
		boolean success = true;
		for (int i = 0; i < slideBricks.size() && success; i++) {
			String brickColor = (String) slideBricks.get(i);
			success = success && conveyor.move(brickColor);
			success = success && slide.ejectOneBrick();
		}
		inAction = false;
		return true;
	}

	public void close() {
		colorReader.close();
		conveyor.close();
		slide.close();
	}
}
