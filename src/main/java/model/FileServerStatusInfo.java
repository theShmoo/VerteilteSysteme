/**
 * 
 */
package model;

import java.io.IOException;
import java.net.InetAddress;

import util.SingleServerSocketCommunication;

/**
 * 
 * @author David
 */
public class FileServerStatusInfo {

	private InetAddress address;
	private int port;
	private long usage;
	private long active;
	private boolean online;
	private SingleServerSocketCommunication sender;

	/**
	 * @param address
	 * @param port
	 * @param usage
	 * @param online
	 */
	public FileServerStatusInfo(InetAddress address, int port, long usage,
			boolean online) {
		this.address = address;
		this.port = port;
		this.usage = usage;
		this.active = System.currentTimeMillis();
		this.online = online;
	}

	/**
	 * @return the address
	 */
	public InetAddress getAddress() {
		return address;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return the usage
	 */
	public long getUsage() {
		return usage;
	}

	/**
	 * @return the active
	 */
	public long getActive() {
		return active;
	}

	/**
	 * @return the online
	 */
	public boolean isOnline() {
		return online;
	}

	/**
	 * @param online
	 *            the online to set
	 */
	public void setOnline(boolean online) {
		this.online = online;
	}

	/**
	 * @return the sender
	 * @throws IOException
	 */
	public SingleServerSocketCommunication getSender() throws IOException {
		if (sender == null) {
			sender = new SingleServerSocketCommunication(port,
					address.getHostAddress());
		}
		return sender;
	}

	/**
	 * Set the FileServerActive for some time
	 */
	public void setActive() {
		this.active = System.currentTimeMillis();
	}

	/**
	 * Returns the FileServerInfo model
	 * 
	 * @return the FileServerInfo model
	 */
	public FileServerInfo getModel() {
		return new FileServerInfo(address, port, usage, online);
	}

}
