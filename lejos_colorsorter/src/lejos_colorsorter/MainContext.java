package lejos_colorsorter;

import java.io.IOException;
import java.text.DecimalFormat;

import lejos.hardware.lcd.LCD;
import network.BluetoothExchanger;
import network.NetworkInterface;
import network.WifiExchanger;

public class MainContext {

	final static int keyCode = generateRadomKeyCode();
	static boolean user_connected = false;

	static NetworkInterface networkExchanger;
	static Ev3Controller ev3Controller;

	static Thread inputThread;
	static Thread outputThread;

	/* action id -1 means exit */
	static int actionId;

	/* Is everything really need to be static */
	public static void main(String[] args) {
		LCD.setAutoRefresh(true);

		chooseNetworkMode();
		displayKeyCode();
		user_connected = networkExchanger.connect();

		ev3Controller = new Ev3Controller();
		ev3Controller.playSound("success");

		actionId = 0;

		new Thread(new manageInputThread()).start();

		processLiveAction();
	}

	private static void processLiveAction() {
		while (actionId != 1) {
			switch (actionId) {
			
			case 1:   // 
				break;

			}
		}

	}

	/* */
	private static class manageInputThread implements Runnable {
		public manageInputThread() {
		}

		public void run() {
			while (actionId != -1) {
				try {
					actionId = networkExchanger.listen();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
	}

	private void sendData() {
		while (actionId != 1) {

			try {
				byte[] data = new byte[8];
				data[0] = 99;
				networkExchanger.send(data);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	/*
	 * Generate a 4 digit number between 0000 and 9999
	 */
	static int generateRadomKeyCode() {
		int number = (int) (Math.random() * 9999);
		DecimalFormat decimalFormat = new DecimalFormat("0000");
		return Integer.parseInt(decimalFormat.format(number));
	}

	/*
	 * Ask user which networking mode to use TODO update always true with button
	 * press
	 */
	static void chooseNetworkMode() {
		LCD.drawString("Welcome!", 0, 1);

		String mode = true ? "WIFI" : "BT";
		if (mode.equals("BL")) {
			networkExchanger = new BluetoothExchanger();
		} else {
			networkExchanger = new WifiExchanger();
		}
	}

	/*
	 * Display connection keyCode to user
	 */
	static void displayKeyCode() {
		LCD.drawString("KeyCode :  " + keyCode, 0, 2);
	}

}
