package model;

import java.io.Serializable;

/**
 * Contains information about a user account.
 */
public class UserInfo implements Comparable<UserInfo>, Serializable {
	private static final long serialVersionUID = 1231468054964602727L;

	private String name;
	private long credits;
	private boolean online;

	public UserInfo(String name, long credits, boolean online) {
		this.name = name;
		this.credits = credits;
		this.online = online;
	}

	@Override
	public String toString() {
		return String.format("%1$-15s %2$-7s %3$13d", name, isOnline() ? "online" : "offline", credits);
	}

	@Override
	public int compareTo(UserInfo o) {
		return getName().compareTo(o.getName());
	}

	public String getName() {
		return name;
	}

	public long getCredits() {
		return credits;
	}

	public boolean isOnline() {
		return online;
	}
}
