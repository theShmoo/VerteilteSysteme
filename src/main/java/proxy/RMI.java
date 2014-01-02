/**
 * 
 */
package proxy;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import message.Response;
import util.Config;

/**
 * 
 * @author Astrid
 */
public class RMI extends UnicastRemoteObject implements IRMI {

	/**
	 * @throws RemoteException
	 */
	protected RMI() throws RemoteException {
		super();
	}

	private static final long serialVersionUID = 4392725810478654970L;

	@Override
	public Response readQuorum() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response writeQuorum() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response topThreeDownloads() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response subscribe(String filename, int number) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response getProxyPublicKey() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response setUserPublicKey(String username) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
}
