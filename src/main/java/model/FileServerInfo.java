package model;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Contains information about a {@link server.IFileServer} and its state.
 */
public class FileServerInfo implements Serializable {
	private static final long serialVersionUID = 5230922478399546921L;

	private InetAddress address;
	private int port;
	private long usage;
	private boolean online;

	public FileServerInfo(InetAddress address, int port, long usage, boolean online) {
		this.address = address;
		this.port = port;
		this.usage = usage;
		this.online = online;
	}

	@Override
	public String toString() {
		return String.format("%1$-15s %2$-5d %3$-7s %4$13d",
				getAddress().getHostAddress(), getPort(),
				isOnline() ? "online" : "offline", getUsage());
	}

	public InetAddress getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public long getUsage() {
		return usage;
	}

	public boolean isOnline() {
		return online;
	}
}
