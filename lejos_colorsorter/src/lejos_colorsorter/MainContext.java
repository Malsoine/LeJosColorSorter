package lejos_colorsorter;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;
import log_manager.LogFileManager;
import network.BluetoothExchanger;
import network.NetworkInterface;
import network.WifiExchanger;

public class MainContext {

	final static String KEY_CODE = generateRandomKEY_CODE();

	static boolean user_connected;
	static boolean isWifiselected;

	static Ev3Controller ev3Controller;
	static LogFileManager logManager;
	static NetworkInterface networkExchanger;

	static Thread inputThread;
	static Thread outputThread;

	static int actionId;
	static byte[] input;

	public static void main(String[] args) {
		LCD.setAutoRefresh(true);
		logManager = new LogFileManager();
		logManager.addLog("Programm starting");

		// No program output, if the client is disconnected we wait for an other one
		while (actionId == 0) { // test exit first run
			actionId = 0;
			user_connected = false;
			isWifiselected = true;

			chooseNetworkMode();
			displayKeyCode();
			user_connected = networkExchanger.connect();
			logManager.addLog("User connected");

			ev3Controller = new Ev3Controller(logManager);

			new Thread(new manageInputThread()).start();
			processLiveAction();
			Delay.msDelay(500); // Wait for all thread to end
			ev3Controller.close();
		}
		logManager.addLog("Programm stoping");
	}

	private static void processLiveAction() {
		boolean success;
		byte[] result;
		logManager.addLog("Start processing instructions");

		while (actionId != -1) { // action id -1 means exit
			logManager.addLog("actionId is " + actionId);

			switch (actionId) {
			case 1: // Connection
				if (!user_connected) {
					LCD.clear();
					logManager.addLog("Start connecting");
					if (validateReceivedKeyCode()) {
						LCD.drawString("Connection: ok", 0, 2);
						ev3Controller.playSound("success");
						user_connected = true;
					} else {
						LCD.drawString("Connection: ko", 0, 2);
						ev3Controller.playSound("error");
					}
				}
				break;

			case 2: // Sort all
				logManager.addLog("Start sorting all bricks on slide");
				if (!ev3Controller.inAction /* && user_connected */) {
					result = ev3Controller.sortAllBricksOnSlide();
					sendData(true, result);
				}
				break;

			case 3: // Add brick to the slide
				logManager.addLog("Start adding bricks to the slide");
				if (!ev3Controller.inAction /* && user_connected */) {
					success = ev3Controller.startScanningProcess();
					sendData(success, null);
				}
				break;

			case 4: // Add brick to the slide
				logManager.addLog("Start adding bricks to the slide");
				if (!ev3Controller.inAction /* && user_connected */) {
					result = ev3Controller.sortUntilXColoredBrickSorted(
							(String) Ev3Controller.BUCKETCOLORMAPPING.get(input[1]), input[2]);
					sendData(true, result);
				}
				break;
			}
		}
		logManager.addLog("Stop processing instructions");
	}

	private static boolean validateReceivedKeyCode() {
		boolean isValid = true;
		for (int i = 0; i < KEY_CODE.length(); i++) {
			// input[0] is actionId
			isValid &= input[i + 1] == Byte.parseByte(KEY_CODE.charAt(i) + "");
		}
		logManager.addLog("Received key_code is " + (isValid ? "valid" : "invalid"));
		return isValid;
	}

	/* */
	private static class manageInputThread implements Runnable {
		public manageInputThread() {
			logManager.addLog("Start listening to instructions");
		}

		public void run() {
			while (actionId != -1) {
				try {
					// byte[] tempInput = networkExchanger.listen();
					byte[] tempInput = { 4, 0, 2 };

					logManager.addLog("Received a command " + byteArrayToNiceString(tempInput));

					if (tempInput != null && validateInputForm(tempInput)) {
						input = tempInput;
						actionId = (int) input[0];
						logManager.addLog("Updated input with " + byteArrayToNiceString(input));
					}
					File f = new File("dddd");
					f.createNewFile();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				actionId = -1;
			}
			logManager.addLog("Stop listening to instructions");
		}

		private static boolean validateInputForm(byte[] tempInput) {
			int tempActionId = tempInput[0];
			boolean success = true;
			switch (tempActionId) {

			case 1:
				// connection - Should be [actionId, **keycode]
				success = tempInput.length == 5;
				break;
			case 2:
			case 3:
				// sort all - Should be [actionId]
				success = tempInput.length == 1;
				break;
			case 4:
				// sort all - Should be [actionId, color index, number]
				// As there is two brick of each color the max number is 2
				// and color should be in the four available ones
				success = tempInput.length == 3 && tempInput[1] < Ev3Controller.BUCKETCOLORMAPPING.size()
						&& tempInput[2] <= 2;
				break;
			}
			logManager.addLog("Command " + byteArrayToNiceString(tempInput) + " is " + (success ? "valid" : "invalid"));
			return success;
		}
	}

	private static void sendData(boolean success, byte[] params) {
		int paramsSize;
		if (params == null) {
			paramsSize = 0;
		} else {
			paramsSize = params.length;
		}

		try {
			// Form array {success, params0, params1, ...}
			byte[] data = new byte[paramsSize + 1];
			data[0] = (byte) (success ? 1 : 0);
			System.arraycopy(params, 0, data, 1, params.length);

			logManager.addLog("Sending " + byteArrayToNiceString(data));
			networkExchanger.send(data);
		} catch (IOException ioe) {
		}
	}

	/*
	 * Generate a 4 digit number between 0000 and 9999
	 */
	static String generateRandomKEY_CODE() {
		int number = (int) (Math.random() * 9999);
		DecimalFormat decimalFormat = new DecimalFormat("0000");
		return decimalFormat.format(number);
	}

	/*
	 * Ask user which networking mode to use TODO update always true with button
	 * press
	 */
	static void chooseNetworkMode() {
		while (Button.ENTER.isUp()) { // Validate mode on middle button press
			if (Button.DOWN.isDown()) { // Swap mode on press of up or down arrows
				isWifiselected = false;
				// ev3Controller.playSound("success");
			} else if (Button.UP.isDown()) {
				isWifiselected = true;
				// ev3Controller.playSound("success");
			}

			LCD.clear();
			if (isWifiselected) {
				LCD.drawString("-> Wifi", 0, 1);
				LCD.drawString("   Bluetooth", 0, 2);
			} else {
				LCD.drawString("   Wifi", 0, 1);
				LCD.drawString("-> Bluetooth", 0, 2);
			}
			Delay.msDelay(150);
		}

		// ev3Controller.playSound("success"); // Acknowledge choice made
		if (isWifiselected) {
			networkExchanger = new WifiExchanger();
		} else {
			networkExchanger = new BluetoothExchanger();
		}
		logManager.addLog("User selected " + (isWifiselected ? "Wifi" : "Bluetooth"));
		LCD.clear();
	}

	/*
	 * Display connection KEY_CODE to user
	 */
	static void displayKeyCode() {
		logManager.addLog("This time the key_code is " + KEY_CODE);
		LCD.drawString("key_code : " + KEY_CODE, 0, 2);
	}

	static String byteArrayToNiceString(byte[] array) {
		String display = "[";
		for (int i = 0; i < array.length; i++) {
			display += array[i] + ",";
		}
		display = display.substring(0, display.length() - 1);
		return display + "]";
	}

}
