package server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import message.request.UploadRequest;
import model.FileInfo;
import util.Config;
import util.FileUtils;
import util.ThreadUtils;
import cli.Shell;

/**
 * 
 * @author David
 */
public class FileServer implements Runnable {

	private Shell shell;
	private FileServerCli serverCli;
	private ExecutorService executor;

	// FileServer properties
	private long alive;
	private File folder;
	private int tcpPort;
	private int udpPort;
	private String proxyHost = "";

	private FileServerDatagramThread udpHandler;
	private List<FileServerSocketThread> fileServerTcpHandlers;

	private boolean running;

	// Ram data
	private List<FileInfo> files;
	private ServerSocket serverSocket;

	/**
	 * Initialize a new fileserver with a {@link Shell}
	 * 
	 * @param shell
	 * @param config
	 */
	public FileServer(Shell shell, Config config) {
		init(shell, config);
	}

	/**
	 * Initialize a new fileserver
	 */
	public FileServer() {
		init(new Shell("fs1", System.out, System.in), new Config("fs1"));
	}

	private void init(Shell shell, Config config) {
		this.executor = ThreadUtils.getExecutor();

		this.shell = shell;
		this.running = true;

		getServerData(config);
		this.serverCli = new FileServerCli(this);
		this.files = new ArrayList<FileInfo>();
		this.fileServerTcpHandlers = new ArrayList<FileServerSocketThread>();
		updateFiles();
	}

	private void getServerData(Config config) {
		try {
			this.tcpPort = config.getInt("tcp.port");
			this.udpPort = config.getInt("proxy.udp.port");
			this.proxyHost = config.getString("proxy.host");
			this.folder = new File(config.getString("fileserver.dir"));
			this.alive = config.getInt("fileserver.alive");
		} catch (NumberFormatException e) {
			System.out
					.println("The configuration file \"client.properties\" is invalid! \n\r");
			e.printStackTrace();
			close();
		}
	}

	/**
	 * Updates the files
	 */
	private void updateFiles() {

		File[] folderfile = folder.listFiles(FileUtils.TEXTFILTER);

		for (File f : folderfile) {
			FileInfo fi = new FileInfo(f.getName(), f.length());
			if (!files.contains(fi)) {
				files.add(fi);
			} else {
				FileInfo updateable = files.get(files.indexOf(fi));
				if (updateable.getFilesize() != fi.getFilesize()) {
					updateable.setFilesize(fi.getFilesize());
					updateable.increaseVersionNumber();
				}
			}

		}
	}

	/**
	 * Returns the {@link FileInfo} to the given filename
	 * 
	 * @param filename
	 *            the filename
	 * @return the {@link FileInfo} to the given filename
	 */
	public FileInfo getFileInfo(String filename) {
		updateFiles();
		for (FileInfo f : files) {
			if (f.getFilename().equals(filename)) {
				return f;
			}
		}
		return null;
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

	@Override
	public void run() {

		shell.register(serverCli);
		executor.execute(shell);

		// Create the Datagram packet to send via UDP
		String aliveMessage = "!alive " + String.valueOf(tcpPort);
		byte[] buf = aliveMessage.getBytes();
		
		try {
			InetAddress address = InetAddress.getByName(proxyHost);
			DatagramPacket packet = new DatagramPacket(buf, buf.length,
					address, udpPort);

			udpHandler = new FileServerDatagramThread(packet, alive);
			executor.execute(udpHandler); // start the UDP sending Machine!
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Starting the ServerSocket
		try {
			serverSocket = new ServerSocket(tcpPort);
			while (running) {
				FileServerSocketThread newThread = new FileServerSocketThread(
						this, serverSocket.accept());
				fileServerTcpHandlers.add(newThread);
				executor.execute(newThread);
			}
		} catch (IOException e) {
			if (running)
				e.printStackTrace();
		} finally {
			close();
		}
	}

	/**
	 * Returns the names of all files in the directory of the fileserver
	 * 
	 * @return the names of all files in the directory of the fileserver
	 */
	public String[] getFileNames() {
		return folder.list(FileUtils.TEXTFILTER);
	}

	/**
	 * Returns the pathname of the Folder of the FileServer
	 * 
	 * @return the pathname
	 */
	public String getPath() {
		return folder.getPath();
	}

	/**
	 * @param request
	 */
	public void persist(UploadRequest request) {
		FileUtils.write(request.getContent(), getPath(), request.getFilename());
	}

	/**
	 * Closes all open Streams and Sockets
	 */
	public void close() {
		running = false;
		shell.close();
		udpHandler.close();
		for (FileServerSocketThread t : fileServerTcpHandlers) {
			t.close();
		}
		try {
			if (!serverSocket.isClosed())
				serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
