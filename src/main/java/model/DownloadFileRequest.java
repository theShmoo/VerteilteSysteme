package model;

import message.Request;

/**
 * 
 * @author David
 */
public class DownloadFileRequest implements Request{

	private int port;
	private String filename;

	private static final long serialVersionUID = 7146664320585820094L;
	
	/**
	 * @param port
	 * @param filename
	 */
	public DownloadFileRequest(int port, String filename) {
		this.port = port;
		this.filename = filename;
	}
	
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}
	
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	
	

}
