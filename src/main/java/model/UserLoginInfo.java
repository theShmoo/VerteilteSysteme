package model;

import message.request.UploadRequest;

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
	public synchronized long getCredits() {
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
	public synchronized void setCredits(long credits) {
		this.credits = credits;
	}

	/**
	 * Set the user Online
	 */
	public synchronized void setOnline() {
		this.online = true;
	}

	/**
	 * Set the user Offline
	 */
	public synchronized void setOffline() {
		this.online = false;
	}

	/**
	 * Returns <code>true</code> if the user is online
	 * 
	 * @return the user online status
	 */
	public synchronized boolean isOnline() {
		return online;
	}

	/**
	 * Adds credits to the user
	 * 
	 * @param credits
	 *            the credits
	 */
	public synchronized void addCredits(long credits) {
		this.credits += credits;
	}

	/**
	 * Adds credits to the user
	 * 
	 * @param request
	 *            the upload request credits
	 */
	public synchronized void addCredits(UploadRequest request) {
		this.credits += request.getContent().length*2;
	}

	/**
	 * Removes Credits from the user
	 * 
	 * @param credits
	 *            the credits to remove
	 */
	public synchronized void removeCredits(long credits) {
		this.credits -= credits;
	}

	/**
	 * @param size
	 * @return TODO
	 */
	public synchronized boolean hasEnoughCredits(long size) {
		return size < credits;
	}

}
