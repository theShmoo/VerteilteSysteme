/**
 * 
 */
package model;

import java.io.IOException;
import java.net.InetAddress;

import server.FileServer;
import util.SingleServerSocketCommunication;

/**
 * This class represents a registered {@link FileServer}
 */
public class FileServerStatusInfo {

	private InetAddress address;
	private int port;
	private long usage;
	private long active;
	private boolean online;
//	private boolean isNr;
//	private boolean isNw;
	private SingleServerSocketCommunication sender;

	/**
	 * Initializes a new FileSeverStatusInfo
	 * 
	 * @param address
	 *            the address of the server
	 * @param port
	 *            the TCP port of the server
	 * @param usage
	 *            the usage of the server
	 * @param online
	 *            the online status of the server
	 * @param isNr
	 * 			  the nr status of the server
	 * @param isNW 
	 * 			  the nw status of the server
	 */
	public FileServerStatusInfo(InetAddress address, int port, long usage,
			boolean online) {
		this.address = address;
		this.port = port;
		this.usage = usage;
		this.active = System.currentTimeMillis();
		this.online = online;
//		this.isNr = isNr;
//		this.isNw = isNw;
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
	public synchronized long getUsage() {
		return usage;
	}

	/**
	 * Returns the time the proxy last got an isActive packet from the
	 * fileserver
	 * 
	 * @return the active
	 */
	public synchronized long getActive() {
		return active;
	}

	/**
	 * @return the online
	 */
	public synchronized boolean isOnline() {
		return online;
	}

	/**
	 * Sets the Fileserver status to onle
	 */
	public void setOnline() {
		this.online = true;
	}

	/**
	 * Sets the Fileserver status to offline
	 */
	public void setOffline() {
		this.online = false;
	}

	/**
	 * @return the sender
	 * @throws IOException
	 */
	public synchronized SingleServerSocketCommunication getSender()
			throws IOException {
		if (sender == null) {
			sender = new SingleServerSocketCommunication(port,
					address.getHostAddress());
		}
		return sender;
	}

	/**
	 * Set the FileServerActive for some time
	 */
	public synchronized void setActive() {
		this.active = System.currentTimeMillis();
	}

	/**
	 * @param usage
	 *            the usage to set
	 */
	public synchronized void setUsage(long usage) {
		this.usage = usage;
	}
	
//	/**
//	 * @param isNr the isNr to set
//	 */
//	public void setNr(boolean isNr) {
//		this.isNr = isNr;
//	}
//
//	/**
//	 * @return the isNr
//	 */
//	public boolean isNr() {
//		return isNr;
//	}
//
//	/**
//	 * @param isNw the isNw to set
//	 */
//	public void setNw(boolean isNw) {
//		this.isNw = isNw;
//	}
//	
//	/**
//	 * @return the isNw
//	 */
//	public boolean isNw() {
//		return isNw;
//	}

	/**
	 * Returns the FileServerInfo model
	 * 
	 * @return the FileServerInfo model
	 */
	public synchronized FileServerInfo getModel() {
		return new FileServerInfo(address, port, usage, online);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + (online ? 1231 : 1237);
		result = prime * result + port;
		result = prime * result + (int) (usage ^ (usage >>> 32));
		//TODO add nr and nw
		return result;
	}

	/**
	 * Returns <code>true</code> if the FileServerStatusInfo equals the
	 * FileServerInfo model
	 * 
	 * @param f
	 *            the FileServerInfo
	 * @return <code>true</code> if the FileServerStatusInfo equals the
	 *         FileServerInfo model
	 */
	public synchronized boolean equalsFileServerInfo(FileServerInfo f) {
		if (f == null) {
			return false;
		}
		if (address == null) {
			if (f.getAddress() != null) {
				return false;
			}
		} else if (!address.equals(f.getAddress())) {
			return false;
		}
		if (port != f.getPort()) {
			return false;
		}
		if (usage != f.getUsage()) {
			return false;
		}
		return true;
	}

	/**
	 * Adds usage
	 * 
	 * @param usage
	 */
	public synchronized void addUsage(long usage) {
		this.usage += usage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FileServerStatusInfo [address=" + address + ", port=" + port
				+ ", usage=" + usage + ", active=" + active + ", online="
				+ online + "]";
	}
}
