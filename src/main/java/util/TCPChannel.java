package util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import proxy.ProxyTCPChannel;
import server.FileServerSocketThread;

/**
 * This abstract class provides functionality on a specific port for incoming
 * messages
 * 
 * @author David
 * @see ProxyTCPChannel
 * @see FileServerSocketThread
 */
public abstract class TCPChannel implements Runnable, Channel, SecureChannel {

	private Socket socket = null;
	private DataInputStream inStream = null;
	private DataOutputStream outputStream = null;
	protected boolean running;
	//Security
	private boolean encrypted = false;
	private byte[] key;
	private byte[] IV;

	/**
	 * Initialize a new SocketThread
	 * 
	 * @param socket
	 *            the socket
	 */
	public TCPChannel(Socket socket) {
		this.running = true;
		this.socket = socket;

		try {
			outputStream = new DataOutputStream(socket.getOutputStream());
			inStream = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Object receive() throws UnexpectedCloseException {
		Object input = null;
		Object response = null;
		try {
			int len = inStream.readInt();
			byte[] data = new byte[len];
			if (len > 0) {
				inStream.readFully(data);
				
				if(encrypted){
					data = SecurityUtils.decrypt(key,IV,data);
				}
				input = SecurityUtils.deserialize(data);
				response = handleInput(input);
			} else{
				close();
			}
		} catch (IOException e) {
			if (running)
				throw new UnexpectedCloseException("Connection reset");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;
	}

	public void send(Object response) {
		try {
			byte[] out = SecurityUtils.serialize(response);
			if(encrypted){
				out = SecurityUtils.encrypt(key,IV,out);
			}
			outputStream.writeInt(out.length);
			if (out.length > 0) {
				outputStream.write(out, 0, out.length);
			}
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
		deactivateSecureConnection();
		try {
			if (!socket.isClosed()) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public synchronized void setKey(byte[] key) {
		this.key = key;
	}
	
	@Override
	public synchronized void setIV(byte[] iV) {
		this.IV = iV;
	}
	
	@Override
	public synchronized void activateSecureConnection() {
		encrypted = true;
	}
	
	@Override
	public synchronized void deactivateSecureConnection() {
		encrypted = false;
	}

}
