/**
 * 
 */
package proxy;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

import message.Response;
import client.SubscribeService;

/**
 * 
 * @author Astrid
 */
public interface IRMI extends Remote {

	/**
	 * Returns the number of Read-Quorums that are currently used for the
	 * replication mechanism.
	 * 
	 * @return Response with the number of Read-Quorums
	 * @throws RemoteException
	 */
	public Response readQuorum() throws RemoteException;

	/**
	 * Returns the number of Write-Quorums that are currently used for the
	 * replication mechanism.
	 * 
	 * @return Response with the number of Write-Quorums
	 * @throws RemoteException
	 */
	public Response writeQuorum() throws RemoteException;

	/**
	 * Returns a sorted list that contains the 3 files that got downloaded the
	 * most. Where the first file in the list, represents the file that got
	 * downloaded the most.
	 * 
	 * @return a sorted list with Strings
	 * @throws RemoteException
	 */
	public Response topThreeDownloads() throws RemoteException;

	/**
	 * Subscribes for the given file.
	 * 
	 * @param subscribe
	 *            the callback object to the client
	 * @param filename
	 *            name of the file
	 * @param number
	 *            number of times, how often the file should be downloaded
	 * @return Response that the file was downloaded x times
	 * @throws RemoteException
	 */
	public Response subscribe(SubscribeService subscribe, String filename,
			int number) throws RemoteException;

	/**
	 * Returns the Proxy's public key.
	 * 
	 * @return PublicKey of the Proxy
	 * @throws RemoteException
	 */
	public PublicKey getProxyPublicKey() throws RemoteException;

	/**
	 * Exchanges the user's public key with the Proxy.
	 * 
	 * @param username
	 *            the name of the user
	 * @param publicKey
	 *            the public key of the user
	 * @return Response if the key was set
	 * @throws RemoteException
	 */
	public Response setUserPublicKey(String username, PublicKey publicKey)
			throws RemoteException;

}
