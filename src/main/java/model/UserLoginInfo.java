package model;

/**
 * The User Info with password
 * 
 * @history 17.10.2013
 * @version 17.10.2013 version 0.1
 * @author David
 */
public class UserLoginInfo {

	private String name;
	private String password;
	private long credits;

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
	 * @return the credits of the user
	 */
	public long getCredits() {
		return credits;
	}
	
	/**
	 * Returns the name of the user
	 * @return the name of the user
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the Credits of the user completely new
	 * @param credits the new credits of the user
	 */
	public void setCredits(long credits) {
		this.credits = credits;
	}
}
