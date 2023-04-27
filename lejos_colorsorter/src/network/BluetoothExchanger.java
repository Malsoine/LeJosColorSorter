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
		LCD.clear();
		LCD.drawString("En attente de connexion ", 5, 5);
		connection = (BTConnection) connector.waitForConnection(60000, NXTConnection.RAW);
		Delay.msDelay(300);
		LCD.clear();
		LCD.drawString("Connexion établie ", 5, 5);
		System.out.println("connected");

	try {
		inputStream = connection.openDataInputStream();
		outputStream = connection.openDataOutputStream();
	} catch (IOException e) {
        handleException(e);
    	}
		
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
	
    // Méthode pour lire des données de la connexion Bluetooth
    public byte read() throws IOException {
        return inputStream.readByte();
    }
    
    // Méthode pour gérer les exceptions
    private void handleException(IOException e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        LogFileManager.addError(writer.toString());
    }

}
