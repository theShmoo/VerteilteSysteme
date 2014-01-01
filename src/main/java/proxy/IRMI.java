/**
 * 
 */
package proxy;

import java.rmi.Remote;
import java.rmi.RemoteException;

import message.Response;

/**
 * 
 * @author Astrid
 */
public interface IRMI extends Remote {

	/**
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public Response readQuorum() throws RemoteException;
	
	/**
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public Response writeQuorum() throws RemoteException;
	
	/**
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public Response topThreeDownloads() throws RemoteException;
	
	/**
	 * 
	 * @param filename
	 * @param number
	 * @return
	 * @throws RemoteException
	 */
	public Response subscribe(String filename, int number) throws RemoteException;
	
	/**
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public Response getProxyPublicKey() throws RemoteException;
	
	/**
	 * 
	 * @param username
	 * @return
	 * @throws RemoteException
	 */
	public Response setUserPublicKey(String username) throws RemoteException;

}
