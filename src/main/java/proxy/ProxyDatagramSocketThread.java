package proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ProxyDatagramSocketThread implements Runnable {
	protected BufferedReader in = null;
	protected boolean running;
	private int udpPort;
	private Proxy proxy;
	DatagramSocket socket;
	
	public ProxyDatagramSocketThread(Proxy proxy) {
		this.udpPort = proxy.getUdpPort();
		this.proxy = proxy;
		this.running = true;
		socket = null;
	}

	@Override
	public void run() {

		try {
			socket = new DatagramSocket(udpPort);
			byte[] buf = new byte[256];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);

			while (running) {
				// receive request
				socket.receive(packet);
				sendIsAlive(packet);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} finally{
			socket.close();
		}
	}

	private void sendIsAlive(DatagramPacket packet) {
		String aliveMessage = new String(packet.getData(), 0,
				packet.getLength());
		InetAddress adress = packet.getAddress();

		String[] split = aliveMessage.split(" ");

		if (split[0].equals("!alive")) {
			String port = split[1];
			try {
				int portToInt = Integer.parseInt(port);

				if ((portToInt > 0) && (portToInt < 12500)) {
					int fileServerTCPPort = portToInt;
					proxy.isAlive(fileServerTCPPort, adress);
				}

			} catch (NumberFormatException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
