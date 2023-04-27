package lejos_colorsorter;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;
import log_manager.LogFileManager;
import network.AbstractNetwork;
import network.BluetoothExchanger;
import network.Communicative;
import network.WifiExchanger;

public class WorkflowManager implements Communicative {

	final static String KEY_CODE = generateRandomKEY_CODE();

	static boolean userConnected;
	static boolean isWifiselected;
	static boolean inMenuBT;
	static boolean quitter = false;

	static Ev3Controller ev3Controller;
	static AbstractNetwork network;

	static String action = "";

	public WorkflowManager() {
		LCD.setAutoRefresh(true);
		LogFileManager.addLog("Programm starting");
	}

	public void start() {
		try {
			while (!quitter) {
				network = null;
				userConnected = false;
				isWifiselected = true;
				action = "";
				ev3Controller = null;
				
				ev3Controller = new Ev3Controller();
				new Thread(new exitButtonListener()).start();
				chooseNetworkMode();
				if (quitter) {
					LogFileManager.addLog("User press exit button");
					LogFileManager.addLog("Force exit the software");
					quitter = true;
					LCD.drawString("En attente de", 0, 4);
					LCD.drawString("l arret du reseau", 0, 5);
					return;
				}

			//	displayKeyCode();
				//LogFileManager.addLog((isWifiselected ? "Wifi" : "Bluetooth") + " starting");
				network.start(this);
				LCD.drawString("En attente", 0, 4);
				LCD.drawString("de connection", 0, 5);
				LCD.drawString("� l'EV3", 0, 6);
				LCD.clear();

				int data = 0;
				byte donnee = (byte) data;				
				
				while (!(data==10)) {
					LCD.clear();
					LCD.drawString("En attente", 0, 4);

					donnee = network.read();
					data = (int) donnee;
					switch(data) {
					case 1 :	//Scan des briques
						LCD.clear();
						LCD.drawString("Scan", 0, 4);
						ev3Controller.startScanningProcess();
						LCD.drawString("Fin scan", 0, 5);
						Delay.msDelay(750);
						break;
					case 2:
						LCD.clear();
						LCD.drawString("Bleu : "+(int)ev3Controller.repart[0],0,3);
						LCD.drawString("Jaune : "+(int)ev3Controller.repart[1],0,4);
						LCD.drawString("Rouge :"+(int)ev3Controller.repart[2],0,5);
						LCD.drawString("Vert :"+(int)ev3Controller.repart[3],0,6);						
						Delay.msDelay(2500);
						break;
					case 3 :	//Trier tout
						LCD.clear();
						LCD.drawString("Tri des pi�ces", 0, 4);
						ev3Controller.sortAllBricksOnSlide();
						LCD.drawString("Fin tri", 0, 5);
						Delay.msDelay(750);

						break;
					case 5 :	//Update du slide
						LCD.drawString("MAJ Slide", 0, 4);
						ev3Controller.updateSlideBrick(ev3Controller.slideBricks);
						LCD.drawString("Fin MAJ", 0, 5);
						Delay.msDelay(750);
						break;
					case 4 :	//Jeter tout � la poubelle
						LCD.drawString("Poubelle", 0, 4);
						ev3Controller.ejectToTrash();
						LCD.drawString("Fin poubelle", 0, 5);
						Delay.msDelay(750);
						break;
					default :
						break;
					}
					Delay.msDelay(250);

					
				}
				LCD.clear();
				LCD.drawString("FIN ", 0, 3);
				LogFileManager.addLog("User asked to stop");

				Delay.msDelay(1000); // Wait for all thread to end
				ev3Controller.close();
			}
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			LogFileManager.addError(writer.toString());
		}

	}

	JsonObject decodeJson(String message) {
		JsonObject root = new JsonObject();

		try {
			root = JsonParser.parseString(message).getAsJsonObject();
		} catch (JsonSyntaxException e) {
			LogFileManager.addError("Error while decoding Json string : '" + message + "'\n");
		}
		return root;
	}

	public void communicate(String message) {
		JsonObject decodedMessage = decodeJson(message);
		if (validateJsonContent(decodedMessage)) {
			action = decodedMessage.get("action").getAsString();
			LogFileManager.addLog("Set action to " + action);

			processAction(action, decodedMessage);
		}
	}

	/*
	 * Validation of data as already been made
	 */
	private static void processAction(String action, JsonObject decodedMessage) {
		String result;

		LogFileManager.addLog("action is " + action);

		if (action.equals("connect")) {
			userConnected = true;
			LogFileManager.addLog("Connexion done");
			ev3Controller.success = true;

		} else if (action.equals("sortAll")) {
			if (!ev3Controller.inAction && userConnected) {
				LogFileManager.addLog("Start sorting all bricks on slide");
				LCD.clear();
				LCD.drawString("Tri de toutes", 0, 1);
				LCD.drawString("les briques", 0, 2);
				result = ev3Controller.sortAllBricksOnSlide();
				sendData(ev3Controller.success, result);
			}

		} else if (action.equals("addBricks")) {
			LogFileManager.addLog("Start adding bricks to the slide");
			if (!ev3Controller.inAction && userConnected) {
				LCD.clear();
				LCD.drawString("Mise à jour ", 0, 1);
				LCD.drawString("manuelle des", 0, 2);
				LCD.drawString("briques demandees", 0, 3);
				ev3Controller.startScanningProcess();
				sendData(ev3Controller.success, "{}");
			}

		} else if (action.equals("sortXColoredBricks")) {
			LogFileManager.addLog("Start sorting colored brick until sum");
			if (!ev3Controller.inAction && userConnected) {
				LCD.clear();
				LCD.drawString("Demande de " + decodedMessage.get("number").getAsString(), 0, 1);
				LCD.drawString("briques de couleur ", 0, 2);
				LCD.drawString(decodedMessage.get("color").getAsString(), 0, 3);
				result = ev3Controller.sortUntilXColoredBrick(decodedMessage.get("color").getAsString(),
						decodedMessage.get("number").getAsInt());
				sendData(ev3Controller.success, result);
			}

		} else if (action.equals("sortXBricks")) {
			LogFileManager.addLog("Start sorting X bricks");
			if (!ev3Controller.inAction && userConnected) {
				LCD.clear();
				LCD.drawString("Demande de tri", 0, 1);
				LCD.drawString("de " + decodedMessage.get("number").getAsString() + " briques", 0, 2);
				result = ev3Controller.sortXBricks(decodedMessage.get("number").getAsInt());
				sendData(ev3Controller.success, result);
			}

		} else if (action.equals("trash")) {
			LogFileManager.addLog("Start sorting X bricks");
			if (!ev3Controller.inAction && userConnected) {
				LCD.clear();
				LCD.drawString("Envoie des briques", 0, 1);
				LCD.drawString("à la poubelle", 0, 2);
				result = ev3Controller.ejectToTrash();
				sendData(ev3Controller.success, result);
			}

		} else if (action.equals("updateSlide")) {
			LogFileManager.addLog("Update slide brick repartition");
			if (!ev3Controller.inAction && userConnected) {
				LCD.clear();
				LCD.drawString("Mise à jour ", 0, 1);
				LCD.drawString("automatique des", 0, 2);
				LCD.drawString("briques demandees", 0, 3);
				LCD.drawString(decodedMessage.get("slide").toString(), 0, 4);

				JsonArray arr = decodedMessage.get("slide").getAsJsonArray();
				ArrayList slideBricks = new ArrayList();
				for (int i = 0; i < arr.size(); i++) {
					slideBricks.add(arr.get(i).getAsString());
				}

				result = ev3Controller.updateSlideBrick(slideBricks);
				sendData(ev3Controller.success, result);
				Delay.msDelay(1500);
			}

		} else if (action.equals("stop")) {
			LCD.clear();
			LCD.drawString("Demande de ", 0, 1);
			LCD.drawString("deconnection", 0, 2);
			LCD.drawString("recue", 0, 3);
			network.forceQuit = true;
			ev3Controller.success = true;

		} else {
			LogFileManager.addError("Unknow action : '" + action + "'");
			LogFileManager.addLog(action + "-" + "stop");
			return;
		}

		Delay.msDelay(500);
		if (!ev3Controller.success) {
			LogFileManager.addError("EV3 returned an error");
			LCD.clear();
			LCD.drawString("Une erreur", 0, 1);
			LCD.drawString("est survenue", 0, 2);
		} else {
			if (userConnected) {
				LCD.clear();
				LCD.drawString("Succes", 0, 1);
				LCD.drawString("En attente", 0, 4);
				LCD.drawString("de commandes", 0, 5);
			} else {
				displayKeyCode();
				LCD.drawString("En attente", 0, 4);
				LCD.drawString("de connexion", 0, 5);
				LCD.drawString("via le code", 0, 6);
			}

		}
		LogFileManager.addLog("Stop processing the instruction");
		action = "";
	}

	/*
	 * Validate info received by network exchanger
	 */
	private static boolean validateJsonContent(JsonObject decodedInput) {
		boolean success = true;
		if (decodedInput == null || decodedInput.get("action") == null)
			return false;
		String action = decodedInput.get("action").getAsString();

		if (action.equals("connect")) {
			success = decodedInput.get("KEYCODE") != null && decodedInput.get("KEYCODE").getAsString().equals(KEY_CODE);
			LogFileManager.addLog(KEY_CODE);
			LogFileManager.addLog(decodedInput.get("KEYCODE").getAsString());
			LCD.clear();
			if (success) {
				LCD.drawString("Code valide", 0, 2);
				// ev3Controller.playSound("success");

			} else {
				displayKeyCode();
				LCD.drawString("Code recu invalide", 0, 4);
				// ev3Controller.playSound("error");
				userConnected = false;
			}

			LogFileManager.addLog("Received KEYCODE is " + (success ? "good" : "wrong"));

		} else if (action.equals("sortXColoredBricks")) {
			// As there is two brick of each color the max number is 2
			// and color should be in the four available ones
			success &= decodedInput.get("color") != null
					&& Ev3Controller.BUCKETCOLORMAPPING.contains(decodedInput.get("color").getAsString());
			success &= decodedInput.get("number") != null && decodedInput.get("number").getAsInt() > 0
					&& decodedInput.get("number").getAsInt() <= 2;

		} else if (action.equals("sortXBricks")) {
			JsonElement number = decodedInput.get("number");
			success &= number != null && number.getAsInt() > 0
					&& number.getAsInt() <= ev3Controller.getSlideBrickCount();
			if (!success && number != null)
				LogFileManager.addError("You asked to sort " + number + " bricks while there is only "
						+ ev3Controller.getSlideBrickCount() + " of them in the slide");

		} else if (action.equals("updateSlide")) {
			JsonElement slide = decodedInput.get("slide");
			success &= slide != null && slide.getAsJsonArray().size() > 0
					&& slide.getAsJsonArray().size() <= Ev3Controller.MAXIMUM_BRICK_IN_SLIDE;
			if (!success) {
				LogFileManager.addError("You asked to update " + slide.getAsJsonArray().size()
						+ " bricks while you should ask " + "to update between 1 and 8 of them");
			} else {
				JsonArray arr = slide.getAsJsonArray();
				for (int i = 0; i < arr.size(); i++) {
					success &= Ev3Controller.BUCKETCOLORMAPPING.contains(arr.get(i).getAsString());
				}
			}
		}

		LogFileManager.addLog("Command " + action + " is " + (success ? "valid" : "invalid"));
		return success;
	}

	/*
	 * Send data to chosen network
	 */
	private static void sendData(boolean success, String formattedJson) {
		formattedJson = uglyFormatJson(success, formattedJson);
		LogFileManager.addLog("Sending " + formattedJson);
		network.send(formattedJson);
	}

	private static String uglyFormatJson(boolean success, String formattedJson) {
		String successString = "\"success\": "+String.valueOf(success)+(formattedJson.length()> 2? ",": "");
		formattedJson = "{" + successString + formattedJson.substring(1, formattedJson.length());
		return formattedJson;
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
	 * Ask user which networking mode to use
	 */
	static void chooseNetworkMode() {
		inMenuBT = true;
		while (Button.ENTER.isUp()) { // Validate mode on middle button press
			if (Button.DOWN.isDown()) { // Swap mode on press of up or down arrows
				isWifiselected = false;
				// ev3Controller.playSound("success");
			} else if (Button.UP.isDown()) {
				isWifiselected = true;
				// ev3Controller.playSound("success");
			}

			LCD.clear();
			LCD.drawString("Choix du reseau", 0, 0);
			LCD.drawString((isWifiselected ? "->" : "  ") + " Wifi", 0, 2);
			LCD.drawString((!isWifiselected ? "->" : "  ") + " Bluetooth", 0, 3);
			Delay.msDelay(100);
		}
	if(!quitter){
		// ev3Controller.playSound("success"); // Acknowledge choice made
		if (isWifiselected) {
			network = new WifiExchanger();
		} else {
			network = new BluetoothExchanger();
		}
		LogFileManager.addLog("User selected " + (isWifiselected ? "Wifi" : "Bluetooth"));
	}
		inMenuBT = false;
	
	}

	/*
	 * Display connection KEY_CODE to user
	 */
	static void displayKeyCode() {
		LCD.clear();
		LogFileManager.addLog("This time the KEYCODE is " + KEY_CODE);
		LCD.drawString("Code : " + KEY_CODE, 0, 2);
	}

	static String byteArrayToNiceString(byte[] array) {
		String display = "[";
		for (int i = 0; i < array.length; i++) {
			display += array[i] + ",";
		}
		display = display.substring(0, display.length() - 1);
		return display + "]";
	}

	private class exitButtonListener implements Runnable {

		public void run() {
			while (Button.ESCAPE.isUp()) { // Escape on button press
				Delay.msDelay(100);
			}
			LCD.clear();
			LCD.drawString("Retour au menu", 0, 0);
			LogFileManager.addLog("User want to escape programm");
			network.forceQuit = true;
			communicate("{\"action\": \"stop\"}");
		}
	}

}
