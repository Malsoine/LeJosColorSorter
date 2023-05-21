package network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.lcd.LCD;
import lejos.remote.ev3.RMIRemoteWifi;
import lejos.utility.Delay;
import log_manager.LogFileManager;

public class WifiExchanger extends AbstractNetwork {

	RMIRemoteWifi remoteWifi;

	BufferedReader input;
	BufferedWriter output;

	ServerSocket server;
	Socket client;
	final int PORT = 80;

	public void start(Communicative listener) {
		super.start(listener);
		new Thread(new listen()).start();
	}

	private class listen implements Runnable {

		public listen() {
			LogFileManager.addLog("Start listening to instructions");
		}

		public void run() {
			try {
				server = new ServerSocket(PORT);
				LogFileManager.addLog("Starting to listen for Wifi device on port " + PORT);

				while (!server.isClosed() && !forceQuit) {
					client = server.accept();

					LogFileManager.addLog("Device connected");

					input = new BufferedReader(new InputStreamReader(client.getInputStream()));
					while (!client.isClosed() && !forceQuit) {
						String inputMessage = "";
						LogFileManager.addLog("Waiting for message");

						if ((inputMessage = input.readLine()) != null) {
							LogFileManager.addLog("Received : " + inputMessage);
							listener.communicate(inputMessage);
						} else {
							client.close();
						}
						Delay.msDelay(150);
					}
				}
				LogFileManager.addLog("Stopping Wifi socket with client");
				listener.communicate("{\"action\": \"stop\"}");

				server.close();
				LCD.clear();
				LCD.drawString("Device deconnected", 2, 2);

			} catch (IOException e) {
				StringWriter writer = new StringWriter();
				PrintWriter printWriter = new PrintWriter(writer);
				e.printStackTrace(printWriter);
				LogFileManager.addError(writer.toString());
			}
		}
	}

	public void send(String data) {
		try {
			output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			output.write(data);
			output.flush();
		} catch (IOException e) {
			StringWriter writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			LogFileManager.addError(writer.toString());
		}
	}

	public boolean connected() {
		return false;
	}

}
