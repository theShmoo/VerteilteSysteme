/**
 * 
 */
package proxy;

import java.rmi.RMISecurityManager;
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

	private static final long serialVersionUID = 1L;

	public RMI() throws RemoteException {
		super();
	}

	public static void main(String[] args) {
		
		//set SecurityManager
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
		
		Config rmiConfig = new Config("mc");
		int rmiPort = rmiConfig.getInt("proxy.rmi.port");
		String rmiHost = rmiConfig.getString("proxy.host");
		Registry registry = null;

		try {
			registry = LocateRegistry.createRegistry(rmiPort);
			registry = LocateRegistry.getRegistry(rmiHost, rmiPort);
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}
		
		//register RemoteObject
		try {
			registry.rebind(rmiHost, new RMI());
		} catch (RemoteException ex) {
			ex.printStackTrace();
		} 
	}

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
