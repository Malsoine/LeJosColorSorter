package network;

public abstract class AbstractNetwork {

	Communicative listener;
	public boolean forceQuit = false;

	// Object which can be notified
	public void start(Communicative comm) {
		this.listener = comm;
	}

	public void send(String data) {
	};

	public boolean connected() {
		return false;
	};
}
