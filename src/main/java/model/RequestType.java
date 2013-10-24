package model;

import message.Request;
import proxy.Proxy;

/**
 * An enumeration of all the {@link Request} messages
 * 
 * @author David
 */
public enum RequestType {
	/**
	 * For a {@link Client} to login to the {@link Proxy}
	 */
	Login,
	/**
	 * For a {@link Client} to logout of the {@link Proxy}
	 */
	Logout,
	/**
	 * For a {@link Client} to request the number of his credits
	 */
	Credits,
	/**
	 * For a {@link Client} to buy some credits
	 */
	Buy,
	/**
	 * For a {@link Client} to get a {@link Ticket} for a download
	 */
	Ticket,

	/**
	 * For a Client to download a file from a fileserver
	 */
	File
}
