/**
 * 
 */
package proxy;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import message.Response;
import message.response.MessageResponse;

/**
 * 
 * @author Astrid
 */
public class RMI extends UnicastRemoteObject implements IRMI {

	private Proxy proxy;

	/**
	 * @throws RemoteException
	 */
	protected RMI(Proxy proxy) throws RemoteException {
		this.proxy = proxy;
	}

	private static final long serialVersionUID = 4392725810478654970L;

	/* (non-Javadoc)
	 * @see proxy.IRMI#readQuorum()
	 */
	@Override
	public Response readQuorum() throws RemoteException {
		if (proxy.getReadQuorums() == 0) {
			return new MessageResponse("No servers are available.");
		}
		return new MessageResponse("Read-Quorum is set to " + proxy.getReadQuorums() + ".");
	}

	/* (non-Javadoc)
	 * @see proxy.IRMI#writeQuorum()
	 */
	@Override
	public Response writeQuorum() throws RemoteException {
		if (proxy.getWriteQuorums() == 0) {
			return new MessageResponse("No servers are available.");
		}
		return new MessageResponse("Write-Quorum is set to " + proxy.getWriteQuorums() + ".");
	}

	/* (non-Javadoc)
	 * @see proxy.IRMI#topThreeDownloads()
	 */
	@Override
	public Response topThreeDownloads() throws RemoteException {
		HashMap<String, Integer> map = proxy.topThreeDownloads();

		if (!map.isEmpty()) {
			Set<String> set = new LinkedHashSet<String>();
			set.addAll(map.keySet());
			String response = "Top Three Downloads:";

			int count = 1;
			while (set.iterator().hasNext()) {
				String file = set.iterator().next();
				response += "\n" + count + ". " + file + " " + map.get(file);
				count++;
			}
			return new MessageResponse(response);
		}
		return new MessageResponse("Files have not yet been downloaded.");
	}

	/* (non-Javadoc)
	 * @see proxy.IRMI#subscribe(java.lang.String, int)
	 */
	@Override
	public Response subscribe(String filename, int number) throws RemoteException {
		//TODO
		return null;
	}

	/* (non-Javadoc)
	 * @see proxy.IRMI#getProxyPublicKey()
	 */
	@Override
	public PublicKey getProxyPublicKey() throws RemoteException {
		return proxy.getProxyPublicKey();
	}

	/* (non-Javadoc)
	 * @see proxy.IRMI#setUserPublicKey(java.lang.String, java.security.PublicKey)
	 */
	@Override
	public Response setUserPublicKey(String username, PublicKey publicKey) throws RemoteException {
		boolean correct = proxy.setUserPublicKey(username, publicKey);
		if (correct) {
			return new MessageResponse("Successfully transmitted public key of user: " + username);
		}
		return new MessageResponse("Exchanging user's public key failed.");
	}
}
