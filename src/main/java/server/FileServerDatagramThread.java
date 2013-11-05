package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

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

	/**
	 * @param packet
	 * @param alive
	 */
	public FileServerDatagramThread(DatagramPacket packet, long alive) {
		this.packet = packet;
		this.alive = alive;
		this.running = true;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			close();
		}
	}

	@Override
	public void run() {
		try {
			while (running) {
				Thread.sleep(alive);
				socket.send(packet);
			}
		} catch (IOException e) {
			if(running)
				e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			close();
		}

	}

	/**
	 * Closes all open streams and sockets
	 */
	public void close() {
		running = false;
		if (!socket.isClosed())
			socket.close();
	}

}
