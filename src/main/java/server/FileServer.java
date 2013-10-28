package server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.Response;
import message.request.UploadRequest;
import model.FileInfo;
import util.Config;
import util.FileUtils;
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
	private File folder;
	private int tcpPort;
	private int udpPort;
	private String proxyHost = "";

	private boolean running;

	// Ram data
	List<FileInfo> files;

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
		this.files = new ArrayList<FileInfo>();
		updateFiles();
	}

	private void getServerData() {
		try {
			this.config = new Config(name);
			this.tcpPort = config.getInt("tcp.port");
			this.udpPort = config.getInt("proxy.udp.port");
			this.proxyHost = config.getString("proxy.host");
			this.folder = new File(config.getString("fileserver.dir"));
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

		// Starting the ServerSocket
		try (ServerSocket serverSocket = new ServerSocket(tcpPort)) {
			while (running) {
				executor.execute(new FileServerSocketThread(this, serverSocket
						.accept()));
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
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
}
