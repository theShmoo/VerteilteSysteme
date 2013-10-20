package proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ProxyDatagramSocketThread implements Runnable {
	protected BufferedReader in = null;
	protected boolean running = true;
	private int udpPort;

	public ProxyDatagramSocketThread(int udpPort) {
		this.udpPort = udpPort;
		System.out.println("UDP running?");
	}

	@Override
	public void run() {
		System.out.println("UDP running?");

		try (DatagramSocket socket = new DatagramSocket(udpPort)) {
			while (running) {

				byte[] buf = new byte[256];

				// receive request
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				String aliveMessage = new String(packet.getData(), 0, packet.getLength());
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
						}

					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
