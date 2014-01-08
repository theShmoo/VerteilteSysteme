/**
 * 
 */
package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 
 * @author David
 */
public interface SubscribeService extends Remote {

	/**
	 * Method to inform the client that a subscribed file has been downloaded
	 * multible times.
	 * 
	 * @throws RemoteException
	 */
	public void invoke() throws RemoteException;

}
