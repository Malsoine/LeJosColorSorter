package lejos_colorsorter;

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

	final static int KEY_CODE = generateRadomKEY_CODE();

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
		while (true) {
			actionId = 0;
			user_connected = false;
			isWifiselected = true;

			chooseNetworkMode();
			displayKeyCode();
			user_connected = networkExchanger.connect();
			logManager.addLog("User connected"); // TODO Ensure true ?

			ev3Controller = new Ev3Controller();

			new Thread(new manageInputThread()).start();
			processLiveAction();
			Delay.msDelay(500); // Wait for all thread to end
			ev3Controller.close();
		}
	}

	private static void processLiveAction() {
		boolean success;
		logManager.addLog("Start processing instructions");

		while (actionId != -1) { // action id -1 means exit
			switch (actionId) {

			case 1: // Connection
				if (!user_connected) {
					LCD.clear();
					success = input[1] == KEY_CODE;
					if (success) {
						LCD.drawString(" Connection: ok", 0, 2);
						ev3Controller.playSound("success");
					} else {
						LCD.drawString(" Connection: ko", 0, 2);
						ev3Controller.playSound("error");
					}
				}
				break;
			case 2: // Sort all
				if (ev3Controller.inAction) {
					success = ev3Controller.sortAllBricksOnSlide();
					sendData(success, null);
				}
				break;
			}
		}
		logManager.addLog("Stop processing instructions");
	}

	/* */
	private static class manageInputThread implements Runnable {
		public manageInputThread() {
			logManager.addLog("Start listening to instructions");
		}

		public void run() {
			while (actionId != -1) {
				try {
					byte[] tempInput = networkExchanger.listen();
					logManager.addLog("Received " + tempInput); // byte array to string ?

					if (validateInputForm(tempInput)) {
						input = tempInput;
						actionId = (int) input[0];
						logManager.addLog("Updated input with" + input);// byte array to string ?
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			logManager.addLog("Stop listening to instructions");
		}

		private boolean validateInputForm(byte[] tempInput) {
			int tempActionId = tempInput[0];
			boolean success = true;
			switch (tempActionId) {

			case 1:
				// connection - Should be [actionId, keycode]
				success = tempInput.length == 2;
				break;
			case 2:
				// sort all - Should be [actionId]
				success = tempInput.length == 1;
				break;
			}
			String isValid = success ? "valid" : "invalid";
			logManager.addLog("Command " + tempInput + " is " + isValid);// byte array to string ?
			return success;
		}
	}

	private static void sendData(boolean success, int[] params) {
		int paramsSize;
		if (params == null) {
			paramsSize = 0;
		} else {
			paramsSize = params.length;
		}

		try {
			byte[] data = new byte[paramsSize + 1];
			data[0] = (byte) (success ? 1 : 0);
			for (int i = 0; i < paramsSize; i++) {
				data[i + 1] = (byte) params[i];
			}
			networkExchanger.send(data);
			logManager.addLog("Sent " + data);// byte array to string ?
		} catch (IOException ioe) {
		}
	}

	/*
	 * Generate a 4 digit number between 0000 and 9999
	 */
	static int generateRadomKEY_CODE() {
		int number = (int) (Math.random() * 9999);
		DecimalFormat decimalFormat = new DecimalFormat("0000");
		return Integer.parseInt(decimalFormat.format(number));
	}

	/*
	 * Ask user which networking mode to use TODO update always true with button
	 * press
	 */
	static void chooseNetworkMode() {
		while (Button.ENTER.isUp()) { // Validate mode on middle button press
			if (Button.DOWN.isDown()) { // Swap mode on press of up or down arrows
				isWifiselected = false;
				ev3Controller.playSound("success");
			} else if (Button.UP.isDown()) {
				isWifiselected = false;
				ev3Controller.playSound("success");
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

		ev3Controller.playSound("success"); // Acknowledge choice made
		if (isWifiselected) {
			networkExchanger = new WifiExchanger();
		} else {
			networkExchanger = new BluetoothExchanger();
		}
		String networkChosen = isWifiselected ? "Wifi" : "Bluetooth";
		logManager.addLog("User selected " + networkChosen);
	}

	/*
	 * Display connection KEY_CODE to user
	 */
	static void displayKeyCode() {
		logManager.addLog("This time the key_code is " + KEY_CODE);
		LCD.drawString("key_code : " + KEY_CODE, 0, 2);
	}

}
