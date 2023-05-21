package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import lejos.hardware.Bluetooth;
import lejos.hardware.lcd.LCD;
import lejos.remote.nxt.BTConnection;
import lejos.remote.nxt.BTConnector;
import lejos.remote.nxt.NXTConnection;
import lejos.utility.Delay;
import log_manager.LogFileManager;

public class BluetoothExchanger extends AbstractNetwork {

    BTConnection connection; // Objet de connexion Bluetooth
    BTConnector connector; // Objet connecteur Bluetooth

    DataInputStream inputStream; // Flux d'entrée pour recevoir des données
    DataOutputStream outputStream; // Flux de sortie pour envoyer des données

    public void start(Communicative listener) {
        super.start(listener); // Appel de la méthode start de la classe mère
        connector = (BTConnector) Bluetooth.getNXTCommConnector(); // Récupération du connecteur Bluetooth
        LCD.clear();
        LCD.drawString("En attente de connexion ", 5, 5); // Affichage sur l'écran LCD
        connection = (BTConnection) connector.waitForConnection(60000, NXTConnection.RAW); // Attente et établissement de la connexion Bluetooth
        Delay.msDelay(300);
        LCD.clear();
        LCD.drawString("Connexion établie ", 5, 5); // Affichage sur l'écran LCD
        System.out.println("connected");

        inputStream = connection.openDataInputStream(); // Ouverture du flux d'entrée de la connexion
        outputStream = connection.openDataOutputStream(); // Ouverture du flux de sortie de la connexion
    }

    public boolean connected() {
        return outputStream != null; // Vérifie si le flux de sortie est initialisé, ce qui indique une connexion établie
    }

    public void send(String data) {
        try {
            outputStream.writeChars(data); // Envoie de données via le flux de sortie
        } catch (IOException e) {
            handleException(e); // Gestion des exceptions en appelant la méthode handleException
        }
    }

    // Méthode pour lire des données de la connexion Bluetooth
    public byte read() throws IOException {
        return inputStream.readByte(); // Lecture d'un octet à partir du flux d'entrée
    }

    // Méthode pour gérer les exceptions
    private void handleException(IOException e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter); // Impression de la trace de la pile d'exception
        LogFileManager.addError(writer.toString()); // Ajout de l'erreur au gestionnaire de journaux
    }

}