package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

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
		this.shell = shell;
		this.clientCli = new ClientCli();

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
		shell.register(new ClientCli());

		//shell.run(); TODO
		try (
	            Socket kkSocket = new Socket(proxyHost, tcpPort);
	            PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
	            BufferedReader in = new BufferedReader(
	                new InputStreamReader(kkSocket.getInputStream()));
	        ) {
	            BufferedReader stdIn =
	                new BufferedReader(new InputStreamReader(System.in));
	            String fromServer;
	            String fromUser;
	 
	            while ((fromServer = in.readLine()) != null) {
	                System.out.println("Server: " + fromServer);
	                if (fromServer.equals("Bye."))
	                    break;
	                 
	                fromUser = stdIn.readLine();
	                if (fromUser != null) {
	                    System.out.println("Client: " + fromUser);
	                    out.println(fromUser);
	                }
	            }
	        } catch (UnknownHostException e) {
	            System.err.println("Don't know about host " + proxyHost);
	            System.exit(1);
	        } catch (IOException e) {
	            System.err.println("Couldn't get I/O for the connection to " +
	            		proxyHost);
	            e.printStackTrace();
	            System.exit(1);
	        }
	}

}