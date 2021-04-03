package network;

import java.io.IOException;

public interface NetworkInterface {
	
	public boolean connect();
	public int listen() throws IOException;
	public void send(byte[] data) throws IOException;
	public void sendString(String message) throws IOException;
}
