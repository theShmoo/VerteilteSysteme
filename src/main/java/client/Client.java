package client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import message.Request;
import message.Response;
import message.request.DownloadFileRequest;
import message.response.DownloadFileResponse;
import message.response.MessageResponse;
import model.DownloadTicket;
import model.FileInfo;
import model.RequestTO;
import model.RequestType;
import proxy.Proxy;
import server.FileServer;
import util.Config;
import util.FileUtils;
import util.SingleServerSocketCommunication;
import cli.Shell;

/**
 * Implements the interface {@link IClient}
 * 
 * @author David
 */
public class Client implements IClient, Runnable {

	private Shell shell;
	private Thread shellThread;
	private ClientCli clientCli;
	private SingleServerSocketCommunication clientThread;

	// Client properties
	private String downloadDir = "";
	private File folder;
	private String proxyHost = "";
	private int tcpPort;

	// Ram data
	private List<FileInfo> files;

	/**
	 * Create a new Client with the given {@link Shell} for its commands
	 * 
	 * @param shell
	 *            the {@link Shell}
	 * @param config
	 */
	public Client(Shell shell, Config config) {
		init(shell, config);
	}

	/**
	 * Create new Client which creates a new {@link Shell} for its commands
	 */
	public Client() {
		init(new Shell("Client", System.out, System.in), new Config("client"));
	}

	private void init(Shell shell, Config config) {
		getClientData(config);
		this.shell = shell;
		this.clientCli = new ClientCli(this);
		shell.register(clientCli);
		shellThread = new Thread(shell);

		this.clientThread = new SingleServerSocketCommunication(tcpPort,
				proxyHost);
		this.files = new ArrayList<FileInfo>();
		updateFiles();
	}

	private void getClientData(Config config) {
		try {
			this.tcpPort = config.getInt("proxy.tcp.port");
			this.proxyHost = config.getString("proxy.host");
			this.downloadDir = config.getString("download.dir");
			this.folder = new File(downloadDir);
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

	private FileInfo getFile(String filename) {
		for (FileInfo f : files) {
			if (f.getFilename().equals(filename)) {
				return f;
			}
		}
		return null;

	}

	/**
	 * Starts a new Client
	 * 
	 * @param args
	 *            no args are used
	 */
	public static void main(String[] args) {
		new Client().run();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		shellThread.start();
		try {
			clientThread.holdConnectionOpen();
		} catch (IOException e) {
			try {
				if(shell != null)
					shell.writeLine("The System is offline! Please try again later");
				exit();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		
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

	/**
	 * Returns the {@link DownloadFileRequest} from the {@link FileServer} and
	 * saves the File containing in the DownloadFileRequest in the directory of
	 * the client
	 * 
	 * @param ticket
	 *            the {@link DownloadTicket} from the {@link Proxy}
	 * @return <li>a {@link DownloadFileResponse} from the {@link FileServer} if
	 *         everything worked out well <li>a {@link MessageResponse} with the
	 *         error Message if something went wrong
	 */
	public Response download(DownloadTicket ticket) {
		SingleServerSocketCommunication clientToFileServer = new SingleServerSocketCommunication(
				ticket.getPort(), proxyHost);
		Response response = clientToFileServer.send(new RequestTO(
				new DownloadFileRequest(ticket), RequestType.File));

		if (response instanceof DownloadFileResponse) {
			DownloadFileResponse download = (DownloadFileResponse) response;
			FileUtils.write(download.getContent(), downloadDir,
					ticket.getFilename());
			updateFiles();
		}
		return response;
	}

	/**
	 * returns the path of the Clients Download directory
	 * 
	 * @return the path of the Clients Download directory
	 */
	public String getPath() {
		return downloadDir;
	}

	/**
	 * @return the file infos
	 */
	public List<FileInfo> getFiles() {
		updateFiles();
		return files;
	}

	/**
	 * Returns the version of the file with the given filename
	 * 
	 * @param filename
	 *            the filename
	 * @return the version of the file with the given filename or -1 if the file
	 *         does not exist
	 */
	public int getVersion(String filename) {
		FileInfo file = getFile(filename);
		if (file == null) {
			return -1;
		}
		return file.getVersion();
	}

	/**
	 * Closes all Threads and shuts down the client
	 */
	public void exit() {
		if (clientThread != null)
			clientThread.close();
		try {
			System.in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(shellThread != null)
			shellThread.interrupt();
		if (shell != null)
			shell.close();
	}
}