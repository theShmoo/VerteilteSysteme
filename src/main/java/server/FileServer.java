package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.Config;
import cli.Shell;

/**
 * 
 * @author David
 */
public class FileServer {

	private Shell shell;
	private Config config;
	private FileServerCli serverCli;
	private ExecutorService executor;

	// FileServer properties
	private String name = "";
	private long alive;
	private String directory = "";
	private int tcpPort;
	private int udpPort;
	private String proxyHost = "";

	private boolean running;
	/**
	 * Initialize a new fileserver with a {@link Shell}
	 * 
	 * @param shell
	 */
	public FileServer(Shell shell) {
		init(shell);
	}

	/**
	 * Initialize a new fileserver
	 */
	public FileServer() {
		// TODO name of fileserver dynamically
		name = "fs1";
		init(new Shell("fs1", System.out, System.in));
	}

	private void init(Shell shell) {
		this.shell = shell;
		this.serverCli = new FileServerCli();
		executor = Executors.newCachedThreadPool();
		
		getServerData();
		this.running = true;
	}

	private void getServerData() {
		try {
			this.config = new Config(name);
			this.tcpPort = config.getInt("tcp.port");
			this.udpPort = config.getInt("proxy.udp.port");
			this.proxyHost = config.getString("proxy.host");
			this.directory = config.getString("fileserver.dir");
			this.alive = config.getInt("fileserver.alive");
		} catch (NumberFormatException e) {
			System.out
					.println("The configuration file \"client.properties\" is invalid! \n\r");
			e.printStackTrace();
			try {
				serverCli.exit();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Starts the FileServer
	 * 
	 * @param args
	 *            no args are used
	 */
	public static void main(String... args) {
		new FileServer().run();
	}

	private void run() {

		shell.register(new FileServerCli());

		try {

			String aliveMessage = "!alive " + String.valueOf(tcpPort);
			System.out.println(aliveMessage);

			// send request
			byte[] buf = aliveMessage.getBytes();
			InetAddress address = InetAddress.getByName(proxyHost);
			DatagramPacket packet = new DatagramPacket(buf, buf.length,
					address, udpPort);

			executor.execute(new FileServerDatagramThread(packet, alive));
			System.out.println("File Server UDP sending machine is running");

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

//		 Starting the ServerSocket
		try (ServerSocket serverSocket = new ServerSocket(tcpPort)) {
			while(running){
				executor.execute(new FileServerSocketThread(this, serverSocket
						.accept()));
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
