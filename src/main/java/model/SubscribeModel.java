/**
 * 
 */
package model;

import java.rmi.RemoteException;

import client.SubscribeService;

/**
 * 
 * @author David
 */
public class SubscribeModel {

	private String filename;
	private int number, currentNumber;
	private SubscribeService subscribe;
	
	/**
	 * Creates new Subscribe Model that counts for the proxy the downloads
	 * @param subscribe 
	 * @param filename
	 * @param number
	 * @param currentNumber
	 */
	public SubscribeModel(SubscribeService subscribe, String filename, int number) {
		this.filename = filename;
		this.number = number;
		this.subscribe = subscribe;
		this.currentNumber = 0;
	}
	
	public void addDownload(){
		currentNumber++;
		if(currentNumber >= number){
			try {
				subscribe.invoke();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the filename of the model
	 */
	public Object getFileName() {
		return filename;
	}

}
