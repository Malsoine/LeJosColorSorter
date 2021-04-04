package network;

import java.io.IOException;

public interface NetworkInterface {
	
	public boolean connect();
	public byte[] listen() throws IOException;
	public void send(byte[] data) throws IOException;
}
