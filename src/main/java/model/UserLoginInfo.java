package model;

/**
 * The User Info with password
 * 
 * @author David
 */
public class UserLoginInfo {

	private String name;
	private String password;
	private long credits;
	private boolean online;

	/**
	 * Contains information about a user account.
	 * 
	 * @param name
	 *            the name of the user
	 * @param password
	 *            the password of the user
	 * @param credits
	 *            the credits of the user
	 */
	public UserLoginInfo(String name, String password, long credits) {
		this.name = name;
		this.password = password;
		this.credits = credits;
		this.online = false;
	}

	/**
	 * Returns the password of the user
	 * 
	 * @return the password of the user
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Returns the credits of the user
	 * 
	 * @return the credits of the user
	 */
	public long getCredits() {
		return credits;
	}

	/**
	 * Returns the name of the user
	 * 
	 * @return the name of the user
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the Credits of the user completely new
	 * 
	 * @param credits
	 *            the new credits of the user
	 */
	public void setCredits(long credits) {
		this.credits = credits;
	}

	/**
	 * Set the user Online
	 */
	public void setOnline() {
		this.online = true;
	}

	/**
	 * Set the user Offline
	 */
	public void setOffline() {
		this.online = false;
	}

	/**
	 * Returns <code>true</code> if the user is online
	 * 
	 * @return the user online status
	 */
	public boolean isOnline() {
		return online;
	}

	/**
	 * Adds credits to the user
	 * 
	 * @param credits
	 *            the credits
	 */
	public void addCredits(long credits) {
		this.credits += credits;
	}

}
