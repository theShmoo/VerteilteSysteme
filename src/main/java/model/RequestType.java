package model;

import message.Request;
import proxy.Proxy;

/**
 * An enumeration of all the {@link Request} messages 
 * @author David
 */
public enum RequestType {
	/**
	 * For a client to login to the {@link Proxy}
	 */
	Login,
	/**
	 * For a client to logout of the {@link Proxy}
	 */
	Logout
}
