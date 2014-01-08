/**
 * 
 */
package client;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import cli.Shell;

/**
 * 
 * @author David
 */
public class SubscribeNotifier extends UnicastRemoteObject implements
		SubscribeService {

	private static final long serialVersionUID = 1718715769042624824L;

	private Shell shell;
	private String filename;
	private int count;

	/**
	 * @param filename
	 *            the filename
	 * @param count
	 *            the number of downloads of the file since subscribing
	 * 
	 * @param shell
	 *            a shell for the output
	 * @throws RemoteException
	 */
	public SubscribeNotifier(Shell shell, String filename, int count)
			throws RemoteException {
		super();
		this.shell = shell;
		this.filename = filename;
		this.count = count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.SubscribeService#invoke(java.lang.String, int)
	 */
	@Override
	public void invoke() throws RemoteException {
		try {
			shell.writeLine("Notification: " + filename + " got downloaded "
					+ count + " times!.");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
