package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class FileServerDatagramThread implements Runnable {
	private DatagramPacket packet;
	private DatagramSocket socket;
	private long alive;
	private boolean running;

	public FileServerDatagramThread(DatagramPacket packet, DatagramSocket socket,long alive) {
		this.packet = packet;
		this.socket = socket;
		this.alive = alive;
		this.running = true;
	}

	@Override
	public void run() {
		try {
			while (running) {
				socket.send(packet);
				Thread.sleep(alive);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		socket.close();

	}

}
