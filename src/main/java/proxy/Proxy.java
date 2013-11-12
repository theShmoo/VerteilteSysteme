package proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.Response;
import message.request.DownloadFileRequest;
import message.request.InfoRequest;
import message.request.ListRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.DownloadFileResponse;
import message.response.ListResponse;
import message.response.VersionResponse;
import model.DownloadTicket;
import model.FileServerInfo;
import model.FileServerStatusInfo;
import model.RequestTO;
import model.RequestType;
import model.UserInfo;
import model.UserLoginInfo;
import server.FileServer;
import util.Config;
import util.SingleServerSocketCommunication;
import util.UserLoader;
import cli.Shell;

/**
 * 
 * @author David
 */
public class Proxy implements Runnable {

	private Shell shell;
	private ProxyCli proxyCli;
	private ExecutorService executor;

	// Configuration Parameters
	private int tcpPort, udpPort;
	private long fsTimeout, fsCheckPeriod;

	private Set<UserLoginInfo> users;
	private Set<FileServerStatusInfo> fileservers;

	// Threads
	private ProxyDatagramSocketThread udpHandler;
	private Timer fsChecker;
	private ServerSocket serverSocket;
	private List<ProxyServerSocketThread> proxyTcpHandlers;
	private boolean running;

	// Filesyncing
	private Map<String, Integer> fileVersionMap;

	/**
	 * Initialize a new Proxy
	 */
	public Proxy() {
		init(new Shell("Proxy", System.out, System.in), new Config("proxy"));
	}

	/**
	 * Initialize a new Proxy with a {@link Shell}
	 * 
	 * @param shell
	 *            the {@link Shell} of the Proxy
	 * @param config
	 */
	public Proxy(Shell shell, Config config) {
		init(shell, config);
	}

	private void init(Shell shell, Config config) {
		this.executor = Executors.newCachedThreadPool();

		this.proxyCli = new ProxyCli(this);

		this.shell = shell;
		shell.register(proxyCli);
		executor.execute(shell);

		this.running = true;

		getProxyData(config);

		this.proxyTcpHandlers = new ArrayList<ProxyServerSocketThread>();
		this.fileVersionMap = new ConcurrentHashMap<String, Integer>();
	}

	private void getProxyData(Config config) {
		try {
			this.tcpPort = config.getInt("tcp.port");
			this.udpPort = config.getInt("udp.port");
			this.fsTimeout = config.getInt("fileserver.timeout");
			this.fsCheckPeriod = config.getInt("fileserver.checkPeriod");
		} catch (NumberFormatException e) {
			try {
				shell.writeLine("The configuration file \"proxy.properties\" is invalid! \n\r");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			close();
		}
		this.fileservers = Collections
				.synchronizedSet(new HashSet<FileServerStatusInfo>());
		this.users = getUsers();
	}

	private Set<UserLoginInfo> getUsers() {

		Set<UserLoginInfo> users = new LinkedHashSet<UserLoginInfo>();

		Config userConfig = new Config("user");

		Set<String> usernames = UserLoader.load();

		for (String username : usernames) {
			try {
				int credits = userConfig.getInt(username + ".credits");
				String password = userConfig.getString(username + ".password");
				users.add(new UserLoginInfo(username, password, credits));
			} catch (NumberFormatException e) {
				System.out
						.println("The configutation of "
								+ username
								+ " of the configuration file \"user.properties\" is invalid! \n\r");
				close();
			}
		}
		return users;
	}

	/**
	 * Starts the Proxy
	 * 
	 * @param args
	 *            no args are used
	 */
	public static void main(String[] args) {
		new Proxy().run();
	}

	@Override
	public void run() {
		// Starting the DatagramSocket
		udpHandler = new ProxyDatagramSocketThread(this);
		executor.execute(udpHandler);
		// Starting the Garbage Collector for the fileservers
		TimerTask action = new TimerTask() {
			public void run() {
				checkOnline();
			}
		};

		fsChecker = new Timer();
		fsChecker.schedule(action, 0, fsTimeout);
		
		// Starting the ServerSocket
		try {
			serverSocket = new ServerSocket(tcpPort);
			while (running) {
				ProxyServerSocketThread newest = new ProxyServerSocketThread(
						this, serverSocket.accept());
				proxyTcpHandlers.add(newest);
				executor.execute(newest);
			}
		} catch (IOException e) {
			if (running)
				e.printStackTrace();
			// else it is ok
		} finally {
			if (running)
				close();
		}

	}

	/**
	 * Returns all fileservers that are registered on the Proxy
	 * 
	 * @return all fileservers
	 */
	public synchronized Set<FileServerInfo> getFileServerInfos() {
		checkOnline();
		Set<FileServerInfo> set = new HashSet<FileServerInfo>();
		for (FileServerStatusInfo f : fileservers) {
			set.add(f.getModel());
		}
		return set;
	}

	/**
	 * Returns all users that are registered on the Proxy and their status
	 * 
	 * @return all users
	 */
	public List<UserInfo> getUserInfos() {
		List<UserInfo> userinfos = new ArrayList<UserInfo>();
		for (UserLoginInfo u : users) {
			userinfos.add(new UserInfo(u.getName(), u.getCredits(), u
					.isOnline()));
		}
		return userinfos;
	}

	/**
	 * Returns all users that are registered on the Proxy and their status
	 * 
	 * @return all users
	 */
	public Set<UserLoginInfo> getUserLoginInfos() {
		return users;
	}

	/**
	 * This method gets called when a new isAlive packet is send from a
	 * fileserver the fileserver gets registered as active for the next time
	 * interval
	 * 
	 * @param fileServerTCPPort
	 *            the TCP port of the fileserver
	 * @param adress
	 *            the address of the fileserver
	 */
	public void isAlive(int fileServerTCPPort, InetAddress adress) {
		boolean newServer = true;
		for (FileServerStatusInfo f : fileservers) {
			if (f.getPort() == fileServerTCPPort) {
				f.setActive();
				newServer = false;
				break;
			}
		}
		if (newServer) {
			fileservers.add(new FileServerStatusInfo(adress, fileServerTCPPort,
					0, true));
			try {
				syncFileservers();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Returns the TCP port number of the Proxy
	 * 
	 * @return the TCP port number of the Proxy
	 */
	public int getTcpPort() {
		return tcpPort;
	}

	/**
	 * Returns the UDP port number of the Proxy
	 * 
	 * @return the UDP port number of the Proxy
	 */
	public int getUdpPort() {
		return udpPort;
	}

	/**
	 * Returns the fileserver with the lowest latency
	 * 
	 * @pre minimum one fileserver is connected
	 * @return the fileserver with the lowest latency
	 */
	public synchronized FileServerInfo getFileserver() {
		FileServerStatusInfo f = getFileserverWithStatus();
		if (f != null) {
			return f.getModel();
		}
		return null;
	}

	/**
	 * @return
	 */
	private FileServerStatusInfo getFileserverWithStatus() {
		long usage = Long.MAX_VALUE;
		FileServerStatusInfo best = null;
		for (FileServerStatusInfo f : getOnlineFileservers()) {
			if (f.getUsage() < usage) {
				usage = f.getUsage();
				best = f;
			}
		}
		return best;
	}
	
	/**
	 * Checks all fileservers if they are online
	 */
	public void checkOnline(){
		for (FileServerStatusInfo f : fileservers) {
			if (f.isOnline()
					&& System.currentTimeMillis() - f.getActive() > fsCheckPeriod) {
				f.setOffline();
			} else {
				f.setOnline();
			}
		}
	}

	/**
	 * Returns all online {@link FileServer}s
	 * 
	 * @return all online {@link FileServer}s
	 */
	public Set<FileServerStatusInfo> getOnlineFileservers() {
		Set<FileServerStatusInfo> onlineServers = new LinkedHashSet<FileServerStatusInfo>();
		checkOnline();
		for (FileServerStatusInfo f : fileservers) {
			if (f.isOnline()) {
				onlineServers.add(f);
			}
		}
		return onlineServers;
	}

	/**
	 * Returns all files that are available for download as a list of Strings
	 * 
	 * @return all files that are available for download as a list of Strings
	 * @throws IOException
	 */
	public Set<String> getFiles() throws IOException {
		syncFileservers();
		return getFiles(getFileserverWithStatus());
	}

	/**
	 * Returns all files that are available for download as a list of Strings
	 * 
	 * @return all files that are available for download as a list of Strings
	 * @throws IOException
	 */
	private Set<String> getFiles(FileServerStatusInfo server)
			throws IOException {
		return getFiles(server.getSender());
	}

	/**
	 * Returns all files that are available for download as a list of Strings
	 * 
	 * @return all files that are available for download as a list of Strings
	 * @throws IOException
	 */
	private Set<String> getFiles(SingleServerSocketCommunication sender)
			throws IOException {
		Response response = sender.send(new RequestTO(new ListRequest(),
				RequestType.List));
		if (response instanceof ListResponse) {
			return ((ListResponse) response).getFileNames();
		}

		return null; // TODO error handling
	}

	/**
	 * Send the requested upload to all {@link FileServer}s
	 * 
	 * @param request
	 *            the UploadRequest from a clients
	 * @throws IOException
	 */
	public void distributeFile(UploadRequest request) throws IOException {

		int version = 0;
		Set<FileServerStatusInfo> fileservers = getOnlineFileservers();

		for (FileServerStatusInfo f : fileservers) {
			int curVersion = getVersion(f.getSender(), request.getFilename());
			version = curVersion > version ? curVersion : version;
		}

		RequestTO requestWithVersion = new RequestTO(new UploadRequest(
				request.getFilename(), version, request.getContent()),
				RequestType.Upload);

		for (FileServerStatusInfo f : fileservers) {
			f.getSender().send(requestWithVersion);
		}
	}

	/**
	 * Synchronize all online file servers that every file server has all newest
	 * files
	 * 
	 * @throws IOException
	 */
	public synchronized void syncFileservers() throws IOException {
		Set<FileServerStatusInfo> fileservers = getOnlineFileservers();
		Map<String, FileServerStatusInfo> updateFiles = new HashMap<String, FileServerStatusInfo>();

		for (FileServerStatusInfo f : fileservers) {

			Set<String> files = getFiles(f.getSender());

			for (String file : files) {
				int version = getVersion(f, file);
				if (fileVersionMap.containsKey(file)) {
					if (fileVersionMap.get(file) < version) {
						fileVersionMap.put(file, version);
						updateFiles.put(file, f);
					}
				} else {
					fileVersionMap.put(file, version);
					updateFiles.put(file, f);
				}
			}
		}

		for (String file : updateFiles.keySet()) {
			UploadRequest request = new UploadRequest(file,
					fileVersionMap.get(file), getFileContent(file,
							updateFiles.get(file)));
			distributeFile(request);
		}
	}

	/**
	 * @param file
	 * @param fileServerStatusInfo
	 * @return
	 * @throws IOException
	 */
	private byte[] getFileContent(String file,
			FileServerStatusInfo fileServerStatusInfo) throws IOException {
		Response response = fileServerStatusInfo
				.getSender()
				.send(new RequestTO(new DownloadFileRequest(new DownloadTicket(
						"proxy", file, "checksum", serverSocket
								.getInetAddress(), tcpPort)), RequestType.File));
		if (response instanceof DownloadFileResponse) {
			DownloadFileResponse download = (DownloadFileResponse) response;
			return download.getContent();
		}
		return null; // TODO error handling
	}

	private int getVersion(FileServerStatusInfo f, String file)
			throws IOException {
		return getVersion(f.getSender(), file);
	}

	/**
	 * Returns the Version of the file on the fileserver
	 * 
	 * @param server
	 *            the fileserver info
	 * @param filename
	 *            the filename to check the version
	 * @return the version of the file on the server and -1 if the file does not
	 *         exist
	 * @throws IOException
	 */
	private int getVersion(SingleServerSocketCommunication sender,
			String filename) throws IOException {
		int version = -1;
		Response response = sender.send(new RequestTO(new VersionRequest(
				filename), RequestType.Version));
		if (response instanceof VersionResponse) {
			version = ((VersionResponse) response).getVersion();
		}
		return version;
	}

	/**
	 * Returns the filesize of the file with the specified filename that is
	 * located on the specified server
	 * 
	 * @param server
	 *            the FileServer
	 * @param filename
	 *            the filename
	 * @return the size of the file or null if the file does not exist
	 */
	public Response getFileInfo(FileServerInfo server, String filename) {
		SingleServerSocketCommunication sender = new SingleServerSocketCommunication(server.getPort(),
					server.getAddress().getHostAddress());		
		return sender.send(new RequestTO(
				new InfoRequest(filename), RequestType.Info));
	}

	/**
	 * Closes all Sockets and Streams
	 */
	public synchronized void close() {
			running = false;
			if (executor != null)
				executor.shutdown();
			if (udpHandler != null)
				udpHandler.close();
			if (fsChecker != null)
				fsChecker.cancel();
			for (ProxyServerSocketThread t : proxyTcpHandlers) {
				// t != null
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
	 * Adds the usage to the specified server
	 * 
	 * @param server
	 *            the {@link FileServer} as info model
	 * @param usage
	 *            the usage or the size of file
	 */
	public void addServerUsage(FileServerInfo server, long usage) {
		FileServerStatusInfo f = getFileServer(server);
		if (f != null) {
			getFileServer(server).addUsage(usage);
		}
	}

	/**
	 * @param server
	 */
	private FileServerStatusInfo getFileServer(FileServerInfo server) {
		for (FileServerStatusInfo f : fileservers) {
			if (f.equalsFileServerInfo(server)) {
				return f;
			}
		}
		return null;
	}
}
