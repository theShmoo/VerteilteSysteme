package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.Request;
import message.Response;
import model.RequestTO;
import proxy.Proxy;
import util.Config;
import cli.Shell;

/**
 * Implements the interface { {@link IClient}
 * 
 * @history 14.10.2013
 * @version 14.10.2013 version 0.1
 * @author David
 */
public class Client implements IClient {

	private Shell shell;
	private Config config;
	private ClientCli clientCli;
	private ExecutorService executor;
	private ClientServerSocketThread clientThread;

	// Client properties
	private String downloadDir = "";
	private String proxyHost = "";
	private int tcpPort;
	private boolean login;

	/**
	 * Create a new Client with the given {@link Shell} for its commands
	 * 
	 * @param shell
	 *            the {@link Shell}
	 */
	public Client(Shell shell) {
		init(shell);
	}

	/**
	 * Create new Client which creates a new {@link Shell} for its commands
	 */
	public Client() {
		init(new Shell("Client", System.out, System.in));
	}

	private void init(Shell shell) {
		this.shell = shell;
		this.clientCli = new ClientCli(this);
		this.login = false;
		this.executor = Executors.newCachedThreadPool();
		this.clientThread = new ClientServerSocketThread(this);

		getClientData();
	}

	private void getClientData() {
		try {
			this.config = new Config("client");
			this.tcpPort = config.getInt("proxy.tcp.port");
			this.proxyHost = config.getString("proxy.host");
			this.downloadDir = config.getString("download.dir");
		} catch (NumberFormatException e) {
			System.out
					.println("The configuration file \"client.properties\" is invalid! \n\r");
			e.printStackTrace();
			try {
				clientCli.exit();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Starts a new Client
	 * 
	 * @param args
	 *            no args are used
	 */
	public static void main(String... args) {
		new Client().run();
	}

	private void run() {
		shell.register(clientCli);

		executor.execute(shell);

		executor.execute(clientThread);
	}

	/**
	 * Returns <code>true</code> if the user is logged in
	 * 
	 * @return <code>true</code> if the user is logged in
	 */
	public boolean isLogin() {
		return login;
	}

	/**
	 * Returns the hostname of the {@link Proxy}
	 * 
	 * @return the hostname of the {@link Proxy}
	 */
	public String getProxyHost() {
		return proxyHost;
	}

	/**
	 * Returns the TCP port of the {@link Proxy}
	 * 
	 * @return the TCP port of the {@link Proxy}
	 */
	public int getTcpPort() {
		return tcpPort;
	}

	/**
	 * Send a data Package to the {@link Proxy}
	 * 
	 * @param request
	 *            the {@link Request} to send to the {@link Proxy}
	 * @return the {@link Response} Object
	 */
	public Response send(RequestTO request) {
		Response respond = null;
		try (Socket socket = new Socket(proxyHost, tcpPort);
				ObjectOutputStream outputStream = new ObjectOutputStream(
						socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(
						socket.getInputStream());) {
			outputStream.writeObject(request);
			System.out.println("Object sent = " + request);
			respond = (Response) inStream.readObject();
			System.out.println("Object received = " + respond);
			socket.close();
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + proxyHost);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to "
					+ proxyHost);
			e.printStackTrace();
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err
					.println("The received object is unknown to " + proxyHost);
			e.printStackTrace();
			System.exit(1);
		}
		return respond;
	}

	/**
	 * Receive a data package from the {@link Proxy}
	 * 
	 * @return the {@link Response} Object
	 */
	public Response receive() {
		Response respond = null;
		try (Socket socket = new Socket(proxyHost, tcpPort);
				ObjectInputStream inStream = new ObjectInputStream(
						socket.getInputStream());) {

			respond = (Response) inStream.readObject();
			System.out.println("Object received = " + respond);
			socket.close();
			return respond;
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + proxyHost);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to "
					+ proxyHost);
			e.printStackTrace();
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err
					.println("The received object is unknown to " + proxyHost);
			e.printStackTrace();
			System.exit(1);
		}
		return respond;
	}

	/**
	 * Set the login status 
	 * @param login <code>true</code> if the client is logged in
	 */
	public void setLogin(boolean login) {
		this.login = login;
	}

}