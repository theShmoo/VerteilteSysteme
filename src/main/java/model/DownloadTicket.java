package model;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Contains information required to download files from a {@link server.IFileServer}.
 */
public final class DownloadTicket implements Serializable {
	private static final long serialVersionUID = 289413562241940171L;

	private String username;
	private String filename;
	private String checksum;
	private InetAddress address;
	private int port;

	public DownloadTicket() {
	}

	public DownloadTicket(String username, String filename, String checksum, InetAddress address, int port) {
		this.username = username;
		this.filename = filename;
		this.checksum = checksum;
		this.address = address;
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return String.format("%s@%s:%d/%s#%s", getUsername(), getAddress().getHostAddress(),
				getPort(), getFilename(), getChecksum());
	}
}
