package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import message.Response;
import model.RequestTO;

public class ClientServerSocketThread implements Runnable {

	private Socket socket = null;
	private int port;
	private String host;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inStream;

	public ClientServerSocketThread(Client client) {
		this.host = client.getProxyHost();
		this.port = client.getTcpPort();
	}

	@Override
	public void run() {
		try {
			socket = new Socket(host, port);
			outputStream = new ObjectOutputStream(
					socket.getOutputStream());
			inStream = new ObjectInputStream(
					socket.getInputStream());
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + host);
			System.exit(1);
		} catch (IOException e) {
			System.err
					.println("Couldn't get I/O for the connection to " + host);
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * @pre The connection is established
	 * @param request
	 *            the request transfer object
	 * @return the response
	 */
	public synchronized Response send(RequestTO request) {
		Response response = null;
		try {
			outputStream.writeObject(request);
			response = (Response) inStream.readObject();
		} catch (IOException e) {
			System.err
					.println("Couldn't get I/O for the connection to " + host);
			e.printStackTrace();
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err.println("Don't now that object!");
			e.printStackTrace();
			System.exit(1);
		}
		return response;
	}

}
