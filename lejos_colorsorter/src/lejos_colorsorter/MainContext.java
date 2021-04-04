package lejos_colorsorter;

import java.io.IOException;
import java.text.DecimalFormat;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;
import network.BluetoothExchanger;
import network.NetworkInterface;
import network.WifiExchanger;

public class MainContext {

	final static int KEY_CODE = generateRadomKEY_CODE();
	static boolean user_connected = false;

	static boolean isWifiselected;

	static NetworkInterface networkExchanger;
	static Ev3Controller ev3Controller;

	static Thread inputThread;
	static Thread outputThread;

	/* action id -1 means exit */
	static int actionId;
	static byte[] input;

	public static void main(String[] args) {
		LCD.setAutoRefresh(true);

		// No program output, if the client is disconnected we wait for an other one
		while (true) {
			actionId = 0;
			chooseNetworkMode();
			displayKeyCode();
			user_connected = networkExchanger.connect();

			ev3Controller = new Ev3Controller();

			new Thread(new manageInputThread()).start();
			processLiveAction();
			Delay.msDelay(500); // Wait for all thread to end
			ev3Controller.close();
		}
	}

	private static void processLiveAction() {
		while (actionId != 1) {
			switch (actionId) {

			case 1: // Connection
				LCD.clear();
				if ((int) input[1] == KEY_CODE) {
					LCD.drawString(" Connection: ok", 0, 2);
					ev3Controller.playSound("success");
				} else {
					LCD.drawString(" Connection: ko", 0, 2);
					ev3Controller.playSound("error");
				}
				break;
			case 2: // Sort all
				if (ev3Controller.inAction) {
					boolean success = ev3Controller.sortAllBricksOnSlide();
					sendData(success, null);
				}
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
					byte[] tempInput = networkExchanger.listen();
					if (validateInputForm(tempInput)) {
						input = tempInput;
						actionId = (int) input[0];
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
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
			return success;
		}
	}

	private static void sendData(boolean success, int[] params) {
		int paramsSize;
		while (actionId != 1) {
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
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
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
		isWifiselected = true;
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
	}

	/*
	 * Display connection KEY_CODE to user
	 */
	static void displayKeyCode() {
		LCD.drawString("key_code : " + KEY_CODE, 0, 2);
	}

}
