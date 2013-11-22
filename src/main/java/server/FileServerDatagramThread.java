package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A FileServerDatagramThread sends a Datagram packet in a specified time
 * interval
 * 
 * @author David
 */
public class FileServerDatagramThread implements Runnable {
	private DatagramPacket packet;
	private DatagramSocket socket;
	private long alive;
	private boolean running;
	private Timer udpSender;
	private TimerTask action;

	/**
	 * Initialize a new FileServerDatagramThread that sends UDP packages at a
	 * given time interval
	 * 
	 * @param packet
	 *            the udp package
	 * @param alive
	 *            the time interval
	 */
	public FileServerDatagramThread(final DatagramPacket packet, long alive) {
		this.packet = packet;
		this.alive = alive;
		this.running = true;

		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			close();
		}

		action = new TimerTask() {
			public void run() {
				try {
					socket.send(packet);
				} catch (IOException e) {
					if (running)
						e.printStackTrace();
				}
			}
		};

		this.udpSender = new Timer();
	}

	@Override
	public void run() {
		udpSender.schedule(action, 0, alive);
	}

	/**
	 * Closes all open streams and sockets
	 */
	public void close() {
		running = false;
		if (udpSender != null)
			udpSender.cancel();
		if (socket != null)
			socket.close();
	}
}
