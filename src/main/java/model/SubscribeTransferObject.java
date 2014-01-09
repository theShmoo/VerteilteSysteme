/**
 * 
 */
package model;

import java.io.Serializable;

import client.SubscribeService;

/**
 * 
 * @author David
 */
public class SubscribeTransferObject implements Serializable{


	private static final long serialVersionUID = -1761411907532049919L;
	
	private SubscribeService ss;
	private String filename;
	private int number;
	
	/**
	 * @param ss
	 * @param filename
	 * @param number
	 */
	public SubscribeTransferObject(SubscribeService ss, String filename,
			int number) {
		this.ss = ss;
		this.filename = filename;
		this.number = number;
	}
	
	

}
