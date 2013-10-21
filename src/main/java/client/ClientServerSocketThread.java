package client;

import java.net.Socket;

public class ClientServerSocketThread implements Runnable {

	private Socket socket = null;
	private int tcpPort;
	private Client client;
	private boolean running;

	public ClientServerSocketThread(int tcpPort, Client client) {
		this.tcpPort = tcpPort;
		this.client = client;
		this.running = true;
	}

	@Override
	public void run() {
	}

}
