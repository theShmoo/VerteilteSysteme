package util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import message.Response;
import message.response.MessageResponse;
import model.RequestTO;

/**
 * A SingleServerSocketCommunication is a a
 * 
 * @author David
 */
public class SingleServerSocketCommunication {

	private Socket socket = null;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inStream;
	private String host;
	private int port;
	private boolean running;

	/**
	 * Initialize a new SingleServerSocketCommunication for TCP connections
	 * 
	 * @param port
	 *            the port of the connection
	 * @param host
	 *            the host of the connection
	 */
	public SingleServerSocketCommunication(int port, String host) {
		this.host = host;
		this.port = port;
		this.running = false;
	}

	private void connect() throws IOException {
		try {
			socket = new Socket(host, port);
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inStream = new ObjectInputStream(socket.getInputStream());
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + host);
			close();
		}
	}

	/**
	 * @pre The connection is established
	 * @param request
	 *            the request transfer object
	 * @return the response
	 */
	public Response send(RequestTO request) {

		if (!running) {
			try {
				connect();
			} catch (IOException e) {
				return new MessageResponse("The Host \"" + host
						+ "\" with the port " + port
						+ " does not answer! Please try again later!");
			}
		}
		Object input = null;
		Response response = null;

		try {
			outputStream.writeObject(request);
			input = inStream.readObject();
			if (input == null || !(input instanceof Response)) {
				return new MessageResponse("The response from Host \"" + host
						+ "\" with the port " + port
						+ " could not get interpreted correctly.");
			} else {
				response = (Response) input;
			}
			return response;
		} catch (IOException e) {
			return new MessageResponse("Could not write to Host \"" + host
					+ "\" on port " + port);
		} catch (ClassNotFoundException e) {
			return new MessageResponse("The Response from the Host \"" + host
					+ "\" on port " + port + " could not get interpreted");
		} finally {
			if (!running)
				close();
		}
	}

	/**
	 * Kills this process needs to get called
	 * 
	 * @post the process is dead
	 */
	public void close() {
		running = false;
		try {
			if (socket != null && !socket.isClosed()) {
				if (outputStream != null)
					outputStream.writeObject(null);
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * If you want to close your Connection by yourself you can tell the
	 * SingleServerSocketCommunication not to open and close the connection by
	 * itself.
	 * 
	 * You have to call the method close!
	 * 
	 * @throws IOException
	 * 
	 */
	public void holdConnectionOpen() throws IOException {
		running = true;
		connect();
	}
}
