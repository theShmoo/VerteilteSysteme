package proxy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * This Thread listens to incoming udp packets from file servers and contacts
 * the proxy that they are alive
 * 
 * @author David
 */
public class ProxyDatagramSocketThread implements Runnable {
	private boolean running;
	private int udpPort;
	private Proxy proxy;
	private DatagramSocket socket;

	/**
	 * initialize a new Socket that listens on the udp port
	 * 
	 * @param proxy
	 *            the proxy
	 */
	public ProxyDatagramSocketThread(Proxy proxy) {
		this.udpPort = proxy.getUdpPort();
		this.proxy = proxy;
		this.running = true;
		try {
			this.socket = new DatagramSocket(udpPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		try {

			byte[] buf = new byte[256];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);

			while (running) {
				// receive request
				socket.receive(packet);
				sendIsAlive(packet);
			}
		} catch (IOException e) {
			if(!socket.isClosed())
				e.printStackTrace();
		} finally {
			close();
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
				//Wrong package received Clam down and carry on
			}
		}
	}

	/**
	 * Closes all resources
	 */
	public void close() {
		running = false;
		if(!socket.isClosed()){
			socket.close();
		}
	}
}
