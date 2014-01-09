package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import message.request.UploadRequest;
import model.FileInfo;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import util.Config;
import util.FileUtils;
import cli.Shell;

/**
 * 
 * @author Group 66 based upon Lab1 version of David
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
	private final String B64 = "a-zA-Z0-9/+";

	private FileServerDatagramThread udpHandler;
	private List<FileServerSocketThread> fileServerTcpHandlers;

	private boolean running;

	// Ram data
	private List<FileInfo> files;
	private ServerSocket serverSocket;
	private Mac hMac;
	private Key key;

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

		this.hMac = null;
		this.shell = shell;
		this.running = true;

		getServerData(config);

		createhMAC(key);
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

			byte[] keyBytes = new byte[1024];
			try {
				File hmackey = new File(config.getString("hmac.key"));
				FileInputStream fis = new FileInputStream(hmackey);
				fis.read(keyBytes);
				fis.close();
				byte[] input = Hex.decode(keyBytes);
				this.key = new SecretKeySpec(input, "HmacSHA256");
			} catch (FileNotFoundException e) {
				System.out.println("Error in getServerData: Keyloading 1");
			} catch (IOException e) {
				System.out.println("Error in getServerData: Keyloading 2");
			}
		} catch (NumberFormatException e) {
			System.out
					.println("The configuration file \"client.properties\" is invalid! \n\r");
			e.printStackTrace();
			close();
		}
	}

	/**
	 * inits hMac with key
	 * 
	 * @param key
	 */
	public void createhMAC(Key key) {
		try {
			this.hMac = Mac.getInstance("HmacSHA256");
			this.hMac.init(key);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("createhashmacerror 1");
		} catch (InvalidKeyException e) {
			System.out.println("createhashmacerror 2");
		}
	}

	/**
	 * creates a hash for a given message
	 * 
	 * @param message
	 * @return hash
	 */
	public byte[] createHashforMessage(String message) {
		hMac.update(message.getBytes());
		byte[] hash = hMac.doFinal(message.getBytes());
		return hash;
	}

	/**
	 * prepends a message with a base64 encoded hash
	 * 
	 * @param hash
	 * @param message
	 * @return prepended message
	 */
	public String prependmessage(byte[] hash, String message) {
		message = new String(Base64.encode(hash)) + " " + message;
		return message;
	}

	/**
	 * verifies a message
	 * 
	 * @param message
	 * @return null if an error occurred or the message without hash
	 */
	public String verify(String message) {

		if (message == null) {
			System.out.println("Error in verify message is null");
			return null;
		}
		if (message.charAt(0) == '!')
			return message;

		assert message.matches("[" + B64 + "]{43}= [\\s[^\\s]]+");
		System.out.println("Base64 Encoding Error");

		int index = message.indexOf(' ');

		if (index == -1) {
			System.out.println("Error message format error");
			System.out.println(message);
			return null;
		}

		// verify
		String hashFM = message.substring(0, index);
		String messageWithoutHash = message.substring(index + 1);
		String hashNG = new String(Base64.encode(this
				.createHashforMessage(messageWithoutHash)));
		if (!hashFM.equals(hashNG)) {
			System.out.println("Error: invalid MAC:");
			System.out.println(message);
			return null;
		}
		message = messageWithoutHash;

		return message;
	}

	/**
	 * Updates the files
	 * 
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
	 * updates the file
	 * 
	 * @param filename
	 *            the name of the file
	 * @param version
	 *            the new version of the file
	 * @param filesize
	 *            the new size of the file
	 */
	private synchronized void updateFile(String filename, float version,
			int filesize) {

		boolean exists = false;
		for (int i = 0; i < files.size(); i++) {
			FileInfo updateable = files.get(i);
			if (filename.equals(updateable.getFilename())) {
				updateable.setVersion((int) version);
				updateable.setFilesize(filesize);
				exists = true;
			}
		}
		if (!exists) {
			FileInfo info = new FileInfo(filename, filesize);
			info.setVersion((int) version);
			files.add(info);
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
		if (args.length != 1) {
			serverName = "fs1";
		} else {
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
	 * Persist a file received from a Upload Request
	 * 
	 * @param request
	 *            the {@link UploadRequest}
	 */
	public void persist(UploadRequest request) {
		FileUtils.write(request.getContent(), getPath(), request.getFilename());
		updateFile(request.getFilename(), request.getVersion(),
				request.getContent().length);
	}

	/**
	 * Returns the file infos on this fileservers
	 * 
	 * @return the file infos on this fileservers
	 */
	public synchronized Set<FileInfo> getFiles() {
		return new HashSet<FileInfo>(files);
	}

	/**
	 * Closes all open Streams and Sockets
	 */
	public void close() {
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
	 * @return the hmac of the fileserver
	 */
	public Mac getHMac() {
		return this.hMac;
	}

	/**
	 * @param directory
	 */
	public void changeDirectory(String directory) {
		this.folder = new File(directory);
	}
}
