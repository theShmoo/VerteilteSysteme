package server;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.request.UploadRequest;
import model.FileInfo;
import util.Config;
import util.FileUtils;
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
	 * 
	 * @param name
	 *            the name of the fileserver
	 */
	public FileServer(String name) {
		init(new Shell(name, System.out, System.in), new Config(name));
	}

	private void init(Shell shell, Config config) {
		this.executor = Executors.newCachedThreadPool();

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
	private synchronized void updateFiles() {

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
	public static void main(String[] args) {
		String serverName = "";
		if(args.length != 1){
			serverName = "fs1";
		}
		else{
			serverName = args[0];
		}
		new FileServer(serverName).run();
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
		} catch (BindException e) {
			try {
				shell.writeLine("The port " + tcpPort + " is already in use!");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			if (running) {
				e.printStackTrace();
			}
		} finally {
			if (running)
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
	 * TODO desription
	 * 
	 * @param request
	 */
	public void persist(UploadRequest request) {
		FileUtils.write(request.getContent(), getPath(), request.getFilename());
		updateFiles();
	}

	/**
	 * Closes all open Streams and Sockets
	 */
	public synchronized void close() {
		// XXX note that the if the fileserver is not closed but started again
		// then the port blocks...
		running = false;
		if (executor != null)
			executor.shutdown();
		if (udpHandler != null)
			udpHandler.close();
		for (FileServerSocketThread t : fileServerTcpHandlers) {
			// t is not null
			t.close();
		}
		try {
			if (serverSocket != null && !serverSocket.isClosed())
				serverSocket.close();
			System.in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (executor != null)
			executor.shutdownNow();
		if (shell != null)
			shell.close();
	}

	/**
	 * Returns the file infos on this fileservers
	 * @return the file infos on this fileservers
	 */
	public synchronized Set<FileInfo> getFiles() {
		return new HashSet<FileInfo>(files);
	}
}
