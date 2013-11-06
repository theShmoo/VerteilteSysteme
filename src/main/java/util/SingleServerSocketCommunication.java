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
public class SingleServerSocketCommunication{

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
	 * @throws IOException 
	 */
	public SingleServerSocketCommunication(int port, String host) throws IOException{
		this.host = host;
		this.port = port;
		this.running = true;
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
		Object input = null;
		Response response = null;
		if (running) {
			try {
				outputStream.writeObject(request);
				input = inStream.readObject();
				if (input == null) {
					close();
				} else {
					response = (Response) input;
				}
			} catch (IOException e) {
				return new MessageResponse("Could not write to Host \"" + host
						+ "\" on port " + port);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return new MessageResponse("The Response from the Host \"" + host
						+ "\" on port " + port + " could not get interpreted");
			}
		} else {
			return new MessageResponse("Socket is already closed!");
		}
		return response;
	}

	/**
	 * Kills this process needs to get called
	 * 
	 * @post the process is dead
	 */
	public void close() {
		running = false;
		try {
			if (!socket.isClosed()) {
				outputStream.writeObject(null);
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
