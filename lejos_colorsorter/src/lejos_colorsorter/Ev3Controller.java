package lejos_colorsorter;

import java.io.File;
import java.util.ArrayList;

import components.ColorReader;
import components.Conveyor;
import components.Slide;
import lejos.hardware.Sound;

public class Ev3Controller {

	ArrayList slideBricks;

	ColorReader colorReader;
	Conveyor conveyor;
	Slide slide;

	public Ev3Controller() {
		slideBricks = new ArrayList();
			
		// Port mapping is made here
		colorReader = new ColorReader("S4");
		conveyor = new Conveyor("D", "S1" );
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
}
