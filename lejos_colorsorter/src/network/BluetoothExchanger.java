package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import lejos.hardware.Bluetooth;
import lejos.remote.nxt.BTConnection;
import lejos.remote.nxt.BTConnector;
import lejos.remote.nxt.NXTConnection;
import log_manager.LogFileManager;

public class BluetoothExchanger extends AbstractNetwork {

	BTConnection connection;
	BTConnector connector;

	DataInputStream inputStream;
	DataOutputStream outputStream;

	public void start(Communicative listener) {
		super.start(listener);

		connector = (BTConnector) Bluetooth.getNXTCommConnector();
		System.out.println("not connected");
		connection = (BTConnection) connector.waitForConnection(60000, NXTConnection.RAW);
		System.out.println("connected");

		inputStream = connection.openDataInputStream();
		outputStream = connection.openDataOutputStream();
	}

	public boolean connected() {
		return outputStream != null;
	}

	public void send(String data) {
		try {
			outputStream.writeChars(data);
		} catch (IOException e) {
			handleException(e)
		}
	};
	   // Méthode pour gérer les exceptions
    private void handleException(IOException e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        LogFileManager.addError(writer.toString());
    }

}
