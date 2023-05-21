package network;

import java.io.IOException;

// Définition d'une classe abstraite pour représenter une connexion réseau générique
public abstract class AbstractNetwork {

	// Référence à l'objet qui sera notifié en cas de réception de données
	Communicative listener;

	// Temps d'attente par défaut (en millisecondes) avant d'interrompre la connexion
	final int DEFAULT_TIMEOUT = 1000 * 60 * 3; // 3 minutes de timeout
	
	// Déclaration de variables pour la gestion de l'écoute de la connexion
	public Thread listeningThread;
	public boolean forceQuit = false;

	// Méthode pour définir l'objet à notifier en cas de réception de données
	public void start(Communicative comm) {
		this.listener = comm;
	}

	// Méthode pour envoyer des données à travers la connexion (à implémenter dans les sous-classes)
	public void send(String data) {
	};

	// Méthode pour vérifier si la connexion est établie (à implémenter dans les sous-classes)
	public boolean connected() {
		// Code de vérification de la connexion
		return false;
	};
	
	// Méthode pour lire un octet de données depuis la connexion (à implémenter dans les sous-classes)
	public byte read() throws IOException{
		// Code de lecture d'un octet de données
		return 0;
	}
}
