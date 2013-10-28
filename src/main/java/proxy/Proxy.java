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
import java.util.concurrent.Executors;

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
import cli.Shell;

/**
 * 
 * @author David
 */
public class Proxy implements Runnable {

	private Shell shell;
	private Config proxyConfig;
	private ProxyCli proxyCli;
	private ExecutorService executor;

	// Configuration Parameters
	private int tcpPort, udpPort;
	private long fsTimeout, fsCheckPeriod;

	private Set<UserLoginInfo> users;
	private Map<FileServerInfo, Long> fileservers;

	private boolean running;

	/**
	 * Initialize a new Proxy
	 */
	public Proxy() {
		init(new Shell("Proxy", System.out, System.in));
	}

	/**
	 * Initialize a new Proxy with a {@link Shell}
	 * 
	 * @param shell
	 *            the {@link Shell} of the Proxy
	 */
	public Proxy(Shell shell) {
		init(shell);
	}

	private void init(Shell shell) {
		getProxyData();

		this.shell = shell;
		this.proxyCli = new ProxyCli(this);
		this.executor = Executors.newCachedThreadPool();
		this.running = true;
	}

	private void getProxyData() {
		try {
			this.proxyConfig = new Config("proxy");
			this.tcpPort = proxyConfig.getInt("tcp.port");
			this.udpPort = proxyConfig.getInt("udp.port");
			this.fsTimeout = proxyConfig.getInt("fileserver.timeout");
			this.fsCheckPeriod = proxyConfig.getInt("fileserver.checkPeriod");
		} catch (NumberFormatException e) {
			System.out
					.println("The configuration file \"proxy.properties\" is invalid! \n\r");
			e.printStackTrace();
			try {
				proxyCli.exit();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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
				try {
					proxyCli.exit();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
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
		executor.execute(new ProxyDatagramSocketThread(this));
		// Starting the Garbage Collector for the fileservers
		executor.execute(new FileServerGarbageCollector(fileservers, fsTimeout,
				fsCheckPeriod));
		// Starting the ServerSocket
		try (ServerSocket serverSocket = new ServerSocket(tcpPort)) {
			while (running) {
				executor.execute(new ProxyServerSocketThread(this, serverSocket
						.accept()));
			}
		}

		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
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
		
		for( FileServerInfo f : fileservers){
			SingleServerSocketCommunication sender = new SingleServerSocketCommunication(
					f.getPort(), f.getAddress().getHostAddress());
			connections.add(sender);
			Response response = sender.send(new RequestTO(new VersionRequest(request.getFilename()),
					RequestType.Version));
			if (response instanceof VersionResponse){
				int curVersion = ((VersionResponse) response).getVersion();
				version = curVersion > version ? curVersion : version;
			} else{
				//TODO error handling
			}
		}
		
		RequestTO requestWithVersion = new RequestTO(new UploadRequest(request.getFilename(), version, request.getContent()),RequestType.Upload);
		
		for( SingleServerSocketCommunication s : connections){
			s.send(requestWithVersion);
			s.close();
		}
		
	}
}
