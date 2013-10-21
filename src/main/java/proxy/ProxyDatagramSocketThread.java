package proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ProxyDatagramSocketThread implements Runnable {
	protected BufferedReader in = null;
	protected boolean running = true;
	private int udpPort;
	private Proxy proxy;

	public ProxyDatagramSocketThread(int udpPort, Proxy proxy) {
		this.udpPort = udpPort;
		this.proxy = proxy;
	}

	@Override
	public void run() {

		try (DatagramSocket socket = new DatagramSocket(udpPort)) {
			while (running) {

				byte[] buf = new byte[256];

				// receive request
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				String aliveMessage = new String(packet.getData(), 0, packet.getLength());
				InetAddress adress = packet.getAddress();
				
				String[] split = aliveMessage.split(" ");

				if (split[0].equals("!alive")) {
					String port = split[1];
					try {
						int portToInt = Integer.parseInt(port);

						if ((portToInt > 0) && (portToInt < 12500)) {
							int fileServerTCPPort = portToInt;
							System.out.println("Der Fileserver mit TCPPort: "
									+ fileServerTCPPort
									+ " hat sich angemeldet!");
							proxy.isAlive(fileServerTCPPort,adress);
						}

					} catch (NumberFormatException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			}
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
