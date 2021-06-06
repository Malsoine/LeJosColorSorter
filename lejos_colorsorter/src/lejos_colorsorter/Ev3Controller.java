package lejos_colorsorter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import components.ColorReader;
import components.Conveyor;
import components.Slide;
import lejos.hardware.Sound;
import log_manager.LogFileManager;

public class Ev3Controller {

	ArrayList slideBricks; // brick are stored as String due to old JRE but Enum would have been better

	boolean inAction; // Store if controller is already performing an action. Decorator handling
						// setting value to true or false at end or beginning of each method would have
						// been great but again JRE restriction

	// Mapping stored here for compliance and coherence
	public static final List BUCKETCOLORMAPPING = Arrays.asList(new String[] { "Blue", "Yellow", "Red", "Green" });

	ColorReader colorReader;
	Conveyor conveyor;
	Slide slide;

	LogFileManager logManager;

	public Ev3Controller(LogFileManager logManager) {
		slideBricks = new ArrayList();
		slideBricks.add("Red");
		slideBricks.add("Yellow");
		slideBricks.add("Red");

		this.logManager = logManager;

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

	byte[] sortAllBricksOnSlide() {
		inAction = true;
		byte[] sortedBrickCount = { 0, 0, 0, 0 };
		boolean success = true;
		for (int i = 0; i < slideBricks.size() && success; i++) {
			String brickColor = (String) slideBricks.get(i);
			sortedBrickCount[BUCKETCOLORMAPPING.indexOf(brickColor)] += 1;
			success &= conveyor.move(brickColor);
			success &= slide.ejectOneBrick();
		}
		inAction = false;
		return sortedBrickCount;
	}

	boolean startScanningProcess() {
		return colorReader.startScanningProcess(slideBricks);
	}

	public void close() {
		colorReader.close();
		conveyor.close();
		slide.close();
	}

	private int countColoredBricks(String color) {
		int count = 0;
		for (int i = 0; i < slideBricks.size(); i++) {
			count += (slideBricks.get(i) == color ? 1 : 0);
		}
		return count;
	}

	private int getIndexForXColoredBrick(String color, int quantityAsked) {
		// assume countColoredBricks as been called before
		int colorCount = 0;
		int index;
		for (index = 0; colorCount < quantityAsked; index++) {
			colorCount += (slideBricks.get(index) == color ? 1 : 0);
		}
		return index;
	}

	public byte[] sortUntilXColoredBrickSorted(String color, byte quantityAsked) {
		int count = countColoredBricks(color);
		boolean success = count >= quantityAsked;
		byte[] sortedBrickCount = { 0, 0, 0, 0 };
		if (success) { // return success ? 
			inAction = true;
			for (int i = 0; i < getIndexForXColoredBrick(color, quantityAsked) && success; i++) {
				String brickColor = (String) slideBricks.get(i);
				sortedBrickCount[BUCKETCOLORMAPPING.indexOf(brickColor)] += 1;
				success &= conveyor.move(brickColor);
				success &= slide.ejectOneBrick();
			}
			inAction = false;
		} else {
			logManager.addLog(
					"Asking to sort too much of " + color + ". Asking " + (int) quantityAsked + " while ther is only " + count);
		}
		return sortedBrickCount;
	}
}
