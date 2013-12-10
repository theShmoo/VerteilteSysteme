package model;

import message.Request;
import proxy.Proxy;
import server.FileServer;
import client.Client;


/**
 * An enumeration of all the {@link Request} messages
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
	 * For a {@link Client} to get a {@link DownloadTicket} for a download
	 */
	Ticket,

	/**
	 * For a Client to download a file from a {@link FileServer}
	 */
	File,
	/**
	 * For a Client to get the List of the available files
	 */
	List,
	/**
	 * For a upload from the {@link Client} to the {@link FileServer}s
	 */
	Upload, 
	/**
	 * For a Client to get the List of the read quorums
	 */
	ReadQuorum,
	/**
	 * For a Client to get the List of the write quorums
	 */
	WriteQuorum,

	Info, Version, DetailedList
}
