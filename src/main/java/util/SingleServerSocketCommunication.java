package util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
public class SingleServerSocketCommunication implements SecureChannel {

	private Socket socket = null;
	private DataOutputStream outputStream;
	private DataInputStream inStream;
	private String host;
	private int port;
	private boolean running;

	// Security
	private boolean encrypted = false;
	private byte[] key;
	private byte[] IV;

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
			outputStream = new DataOutputStream(socket.getOutputStream());
			inStream = new DataInputStream(socket.getInputStream());
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
		Response response = null;

		try {
			byte[] out = SecurityUtils.serialize(request);
			if (encrypted) {
				out = SecurityUtils.encrypt(key, IV, out);
			}
			outputStream.writeInt(out.length);
			if (out.length > 0) {
				outputStream.write(out, 0, out.length);
			}
			int len = inStream.readInt();
			byte[] data = new byte[len];
			if (len > 0) {
				inStream.readFully(data);
			}
			if (encrypted) {
				data = SecurityUtils.decrypt(key, IV, data);
			}
			response = (Response) SecurityUtils.deserialize(data);
		} catch (IOException e) {
			running = false;
			return new MessageResponse("Could not write to Host \"" + host
					+ "\" on port " + port);
		} catch (ClassNotFoundException e) {
			return new MessageResponse("The Response from the Host \"" + host
					+ "\" on port " + port + " could not get interpreted");
		} finally {
			if (!running)
				close();
		}
		return response;
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

	@Override
	public void setKey(byte[] key) {
		this.key = key;
	}

	@Override
	public void setIV(byte[] iV) {
		this.IV = iV;
	}

	@Override
	public void activateSecureConnection() {
		encrypted = true;
	}

	@Override
	public void deactivateSecureConnection() {
		encrypted = false;
	}

	/**
	 * Kills this process needs to get called
	 * 
	 * @post the process is dead
	 */
	public void close() {
		try {
			if (socket != null && !socket.isClosed()) {
				if (outputStream != null)
					outputStream.write(0);
				socket.close();
			}
		} catch (IOException e) {
			//It was already closed so it threw an error
			//e.printStackTrace();
		}
	}

	/**
	 * Return the status of the connection
	 * 
	 * @return the status of the connection
	 */
	public boolean isActive() {
		return !socket.isClosed() && !socket.isInputShutdown()
				&& !socket.isOutputShutdown() && running;
	}
}
