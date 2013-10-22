package client;

import java.io.IOException;
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
		getClientData();

		this.shell = shell;
		this.clientCli = new ClientCli(this);
		this.executor = Executors.newCachedThreadPool();
		this.clientThread = new ClientServerSocketThread(this);

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
		return clientThread.send(request);
	}

}