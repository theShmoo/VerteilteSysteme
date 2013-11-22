package util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import proxy.ProxyServerSocketThread;
import server.FileServerSocketThread;

/**
 * This abstract class provides functionality on a specific port for incoming
 * messages
 * 
 * @author David
 * @see ProxyServerSocketThread
 * @see FileServerSocketThread
 */
public abstract class SocketThread implements Runnable {

	private Socket socket = null;
	private ObjectInputStream inStream = null;
	private ObjectOutputStream outputStream = null;
	protected boolean running;

	/**
	 * Initialize a new SocketThread
	 * 
	 * @param socket
	 *            the socket
	 */
	public SocketThread(Socket socket) {
		this.running = true;
		this.socket = socket;

		try {
			this.inStream = new ObjectInputStream(socket.getInputStream());
			this.outputStream = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Waits for a stream
	 * 
	 * @throws UnexpectedCloseException
	 * @throws ClassNotFoundException
	 */
	protected Object receive() throws UnexpectedCloseException {
		Object input = null;
		try {
			input = inStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if (running)
				throw new UnexpectedCloseException("Connection reset");
		}
		handleInput(input);
		return input;
	}

	/**
	 * Send an object via the stream
	 * 
	 * @param response
	 *            the object to send
	 */
	protected void send(Object response) {
		try {
			outputStream.writeObject(response);
		} catch (IOException e) {
			if (running)
				e.printStackTrace();
		}
	}

	/**
	 * If the SocketThread receives a null Object it makes an controlled close
	 * otherwise this method returns the received object
	 * 
	 * @param input
	 *            the received object
	 * @return the received input
	 */
	private Object handleInput(Object input) {
		if (input == null) {
			// controlled close if input is null Object
			close();
		}
		return input;
	}

	/**
	 * Closes all resources that includes the input/output Stream and the socket
	 */
	public void close() {
		running = false;
		try {
			if (!socket.isClosed()) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
