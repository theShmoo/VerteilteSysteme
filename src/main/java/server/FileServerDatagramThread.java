package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

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
			System.exit(1);
		}
	}

	@Override
	public void run() {
		try {
			while (running) {
				socket.send(packet);
				Thread.sleep(alive);
			}
		} catch (IOException e) {
			running = false;
			e.printStackTrace();
		} catch (InterruptedException e) {
			running = false;
			e.printStackTrace();
		} finally {
			socket.close();
		}

	}

}
