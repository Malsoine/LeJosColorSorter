package lejos_colorsorter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import components.ColorReader;
import components.Conveyor;
import components.Slide;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import log_manager.LogFileManager;
//import test.WorflowManagerTest;

public class Ev3Controller {

	ArrayList slideBricks; // Bricks are stored as String due to old JRE but Enum would have been better

	boolean inAction; // Store if controller is already performing an action. Decorator handling
						// setting value to true or false at end or beginning of each method would have
						// been great but again JRE restriction

	// Mapping stored here for compliance and coherence
	public static final List BUCKETCOLORMAPPING = Arrays.asList(new String[] { "Blue", "Yellow", "Red", "Green" });
	public static final int MAXIMUM_BRICK_IN_SLIDE = 8;

	ColorReader colorReader;
	Conveyor conveyor;
	Slide slide;
	byte[] contenu = { 0, 0, 0, 0 };
	int bucketIndex;
	public boolean success = true;

	public Ev3Controller() {
		slideBricks = new ArrayList();


		inAction = false;

		// Port mapping is made here
		colorReader = new ColorReader("S3");
		conveyor = new Conveyor("D", "S1");
		slide = new Slide("A");
	}

	/*
	 * Current sound list : success - error - full
	 * 
	 * Bug : sometimes the sound is played normally but the program crash just
	 * after, freezing the EV3 without raising an exception or returning anything
	 */
	boolean playSound(String soundFileName) {
		File f = new File(soundFileName + ".wav");
		return f.canRead() && Sound.playSample(f) >= 0;
	}

	/*
	 * Sort and eject all bricks in the slide in the corresponding bucket
	 */
	public String sortAllBricksOnSlide() {
		inAction = true;
		byte[] sortedBrickCount = { 0, 0, 0, 0 };
		success = true;

		while (slideBricks.size() > 0 && success) {
			String brickColor = (String) slideBricks.get(0);
			bucketIndex = BUCKETCOLORMAPPING.indexOf(brickColor);
			sortedBrickCount[bucketIndex] += 1;

			success &= conveyor.move(bucketIndex + 1); //+1 car le premier bac est la poubelle
			success &= slide.ejectOneBrick();
			slideBricks.remove(0);
		}
		contenu = sortedBrickCount;
		inAction = false;
		LogFileManager.addLog("Slide is now empty");
		return getBucketRepartition(sortedBrickCount);
	}

	/*
	 * Eject all bricks in the slide in the first bucket
	 */
	public String ejectToTrash() {
		inAction = true;
		byte[] sortedBrickCount = { 0, 0, 0, 0 };
		success = true;
		//System.out.println(slideBricks);
		while (slideBricks.size() > 0 && success) {
			eject(sortedBrickCount, 0);
		}
		contenu = sortedBrickCount;
		inAction = false;
		LogFileManager.addLog("Slide is now empty");
		return getBucketRepartition(sortedBrickCount);
	}

	/*
	 * Start the process of user manually adding brick to the slide
	 */
	public boolean startScanningProcess() {
		boolean success = colorReader.startScanningProcess(slideBricks);
		if (slideBricks.size() == MAXIMUM_BRICK_IN_SLIDE) {
			LogFileManager.addLog("Slide is now full of bricks");
		} else {
			LogFileManager.addError("There is currently " + slideBricks.size() + " bricks in the slide");
		}
		LCD.clear();
		return success;
	}

	public void close() {
		colorReader.close();
		conveyor.close();
		slide.close();
	}

	/*
	 * Return the number of brick for a given color in the slide
	 */
	int countColoredBricks(String color) {
		int count = 0;
		for (int i = 0; i < slideBricks.size(); i++) {
			count += (slideBricks.get(i).equals(color) ? 1 : 0);
		}
		return count;
	}

	/*
	 * Return the given index so X bricks from a specific color will be sorted
	 */
	int getIndexForXColoredBrick(String color, int quantityAsked) {
		// assume countColoredBricks as been called before
		int colorCount = 0;
		int index;
		for (index = 0; colorCount < quantityAsked; index++) {
			colorCount += (slideBricks.get(index).equals(color) ? 1 : 0);
		}
		return index;
	}

	/*
	 * Sort until X bricks of a given color are send into the bucket
	 */
	public String sortUntilXColoredBrick(String color, int quantityAsked) {
		int count = countColoredBricks(color);
		success = count >= quantityAsked;
		byte[] sortedBrickCount = { 0, 0, 0, 0 };
		LogFileManager.addLog("Asking " + quantityAsked + " " + color.toLowerCase() + " bricks");

		if (success) { // return success ?
			inAction = true;
			int i = 0;
			for (i = 0; i < getIndexForXColoredBrick(color, quantityAsked) && success; i++) {
				eject(sortedBrickCount);
			}
			inAction = false;
			LogFileManager.addLog(i + " bricks have been ejected");
		} else {
			LogFileManager.addError("Their is only " + count + " " + color + "brick");
		}
		contenu = sortedBrickCount;
		return getBucketRepartition(sortedBrickCount);
	}

	/*
	 * Sort x bricks from the slide
	 */
	public String sortXBricks(int quantityAsked) {
		byte[] sortedBrickCount = { 0, 0, 0, 0 };

		success = true;
		inAction = true;
		for (int _ = 0; _ < quantityAsked && success; _++) {
			eject(sortedBrickCount);
		}
		contenu = sortedBrickCount;
		inAction = false;
		return getBucketRepartition(sortedBrickCount);
	}

	public String updateSlideBrick(ArrayList slideBricks) {
		success = true;
		LogFileManager.addLog("The slide was looking like " + this.slideBricks);

		this.slideBricks = slideBricks;
		LogFileManager.addLog("The slide now look like " + slideBricks);
		return "{}";
	}

	/*
	 * Manage the whole eject process like doing the physical action and modifying
	 * the digital representation of the brick
	 */
	void eject(byte[] sortedBrickCount) {
		String brickColor = (String) slideBricks.get(0);
		bucketIndex = BUCKETCOLORMAPPING.indexOf(brickColor);
		sortedBrickCount[bucketIndex] += 1;

		success &= conveyor.move(bucketIndex);
		success &= slide.ejectOneBrick();
		slideBricks.remove(0);
		LogFileManager.addLog(brickColor + " brick ejected");
	}

	/*
	 * Override bucket in which we will send the brick
	 */
	void eject(byte[] sortedBrickCount, int bucketIndexOvrride) {
		String brickColor = (String) slideBricks.get(0);
		bucketIndex = BUCKETCOLORMAPPING.indexOf(brickColor);
		sortedBrickCount[bucketIndex] += 1;

		success &= conveyor.move(bucketIndexOvrride);
		success &= slide.ejectOneBrick();
		slideBricks.remove(0);
		LogFileManager.addLog(brickColor + " brick ejected");
	}

	String getBucketRepartition(byte[] bucketRepartition) {
		String res = "{";
		for (int i = 0; i < BUCKETCOLORMAPPING.size(); i++) {
			res += '"' + (String) BUCKETCOLORMAPPING.get(i) + "\": " + bucketRepartition[i] + ",";
		}
		res = res.substring(0, res.length() - 1) + "}";
		LogFileManager.addLog("Formatted json is " + res);
		return res;
	}

	public int getSlideBrickCount() {
		return slideBricks.size();
	}
}
