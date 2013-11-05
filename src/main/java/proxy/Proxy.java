package proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import message.Response;
import message.request.ListRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.ListResponse;
import message.response.VersionResponse;
import model.FileServerInfo;
import model.RequestTO;
import model.RequestType;
import model.UserInfo;
import model.UserLoginInfo;
import server.FileServer;
import util.Config;
import util.SingleServerSocketCommunication;
import util.ThreadUtils;
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
	private Map<FileServerInfo, Long> fileservers;

	// Threads
	private ProxyDatagramSocketThread udpHandler;
	private FileServerGarbageCollector fsChecker;
	private ServerSocket serverSocket;
	private List<ProxyServerSocketThread> proxyTcpHandlers;
	private boolean running;

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
		this.executor = ThreadUtils.getExecutor();
		this.shell = shell;
		this.running = true;

		getProxyData(config);
		this.proxyCli = new ProxyCli(this);
		this.proxyTcpHandlers = new ArrayList<ProxyServerSocketThread>();
	}

	private void getProxyData(Config config) {
		try {
			this.tcpPort = config.getInt("tcp.port");
			this.udpPort = config.getInt("udp.port");
			this.fsTimeout = config.getInt("fileserver.timeout");
			this.fsCheckPeriod = config.getInt("fileserver.checkPeriod");
		} catch (NumberFormatException e) {
			System.out
					.println("The configuration file \"proxy.properties\" is invalid! \n\r");
			e.printStackTrace();
			close();
		}
		this.fileservers = new HashMap<FileServerInfo, Long>();
		this.users = getUsers();
	}

	private Set<UserLoginInfo> getUsers() {

		Set<UserLoginInfo> users = new LinkedHashSet<UserLoginInfo>();

		Config userConfig = new Config("user");

		// XXX maybe get them dynamically
		List<String> usernames = new ArrayList<String>();
		usernames.add("alice");
		usernames.add("bill");

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
				e.printStackTrace();
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
	public static void main(String... args) {
		new Proxy().run();
	}

	@Override
	public void run() {

		shell.register(proxyCli);

		executor.execute(shell);
		// Starting the DatagramSocket
		udpHandler = new ProxyDatagramSocketThread(this);
		executor.execute(udpHandler);
		// Starting the Garbage Collector for the fileservers
		fsChecker = new FileServerGarbageCollector(fileservers, fsTimeout,
				fsCheckPeriod);
		executor.execute(fsChecker);
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
			close();
		}

	}

	/**
	 * Returns all fileservers that are registered on the Proxy
	 * 
	 * @return all fileservers
	 */
	public Set<FileServerInfo> getFileServerInfos() {
		return fileservers.keySet();
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
		for (FileServerInfo f : fileservers.keySet()) {
			if (f.getPort() == fileServerTCPPort) {
				fileservers.put(f, System.currentTimeMillis());
				newServer = false;
				break;
			}
		}
		if (newServer) {
			fileservers.put(new FileServerInfo(adress, fileServerTCPPort, 0,
					true), System.currentTimeMillis());
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
	public FileServerInfo getFileserver() {
		long usage = Long.MAX_VALUE;
		FileServerInfo best = null;
		for (FileServerInfo f : getOnlineFileservers()) {
			usage = f.getUsage() < usage ? f.getUsage() : usage;
			best = f;
		}
		return best;
	}

	/**
	 * Returns all online {@link FileServer}s
	 * 
	 * @return all online {@link FileServer}s
	 */
	public Set<FileServerInfo> getOnlineFileservers() {
		Set<FileServerInfo> onlineServers = new LinkedHashSet<FileServerInfo>();
		for (FileServerInfo f : fileservers.keySet()) {
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
	 */
	public Set<String> getFiles() {
		FileServerInfo server = getFileserver();
		SingleServerSocketCommunication sender = new SingleServerSocketCommunication(
				server.getPort(), server.getAddress().getHostAddress());
		Response response = sender.send(new RequestTO(new ListRequest(),
				RequestType.List));
		sender.close();
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
	 */
	public void distributeFile(UploadRequest request) {

		int version = 0;
		Set<FileServerInfo> fileservers = getOnlineFileservers();
		Set<SingleServerSocketCommunication> connections = new LinkedHashSet<SingleServerSocketCommunication>();

		for (FileServerInfo f : fileservers) {
			SingleServerSocketCommunication sender = new SingleServerSocketCommunication(
					f.getPort(), f.getAddress().getHostAddress());
			connections.add(sender);
			Response response = sender.send(new RequestTO(new VersionRequest(
					request.getFilename()), RequestType.Version));
			if (response instanceof VersionResponse) {
				int curVersion = ((VersionResponse) response).getVersion();
				version = curVersion > version ? curVersion : version;
			} else {
				// TODO error handling
			}
		}

		RequestTO requestWithVersion = new RequestTO(new UploadRequest(
				request.getFilename(), version, request.getContent()),
				RequestType.Upload);

		for (SingleServerSocketCommunication s : connections) {
			s.send(requestWithVersion);
			s.close();
		}

	}

	/**
	 * Closes all Sockets and Streams
	 */
	public void close() {
		running = false;
		shell.close();
		udpHandler.close();
		fsChecker.close();
		for (ProxyServerSocketThread t : proxyTcpHandlers) {
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
