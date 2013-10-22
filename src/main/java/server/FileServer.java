package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.Response;
import message.request.DownloadFileRequest;
import message.request.InfoRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.MessageResponse;
import util.Config;
import cli.Shell;

/**
 * 
 * @history 14.10.2013 created
 * @version 14.10.2013 version 0.1
 * @author David
 */
public class FileServer implements IFileServer {

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

	public FileServer(Shell shell) {
		init(shell);
	}

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

		// shell.run();
//		System.out.println(proxyHost + " " + tcpPort);

//		try (Socket socket = new Socket(proxyHost, 10180);
//				ObjectOutputStream outputStream = new ObjectOutputStream(
//						socket.getOutputStream());) {
//
//			FileServerInfo data = new FileServerInfo(
//					InetAddress.getLocalHost(), tcpPort, 0, true);
//			outputStream.writeObject(data);
//			System.out.println("Object sent = " + data);
//			socket.close();
//		} catch (UnknownHostException e) {
//			System.err.println("Don't know about host " + proxyHost);
//			System.exit(1);
//		} catch (IOException e) {
//			System.err.println("Couldn't get I/O for the connection to "
//					+ proxyHost);
//			e.printStackTrace();
//			System.exit(1);
//		}

		try (DatagramSocket socket = new DatagramSocket()) {

			String aliveMessage = "!alive " + String.valueOf(tcpPort);
			System.out.println(aliveMessage);

			// send request
			byte[] buf = aliveMessage.getBytes();
			InetAddress address = InetAddress.getByName(proxyHost);
			DatagramPacket packet = new DatagramPacket(buf, buf.length,
					address, udpPort);

			new FileServerDatagramThread(packet, socket, alive).run();

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public Response list() throws IOException {
		// TODO implement list
		return null;
	}

	@Override
	public Response download(DownloadFileRequest request) throws IOException {
		// TODO implement download
		return null;
	}

	@Override
	public Response info(InfoRequest request) throws IOException {
		// TODO implement info
		return null;
	}

	@Override
	public Response version(VersionRequest request) throws IOException {
		// TODO implement version
		return null;
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		// TODO implement upload
		return null;
	}

}
