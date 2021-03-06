package proxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.login.LoginException;

import message.Response;
import message.request.InfoRequest;
import message.request.ListRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.ListResponse;
import message.response.VersionResponse;
import model.FileServerInfo;
import model.FileServerStatusInfo;
import model.RequestTO;
import model.RequestType;
import model.SubscribeModel;
import model.UserInfo;
import model.UserLoginInfo;

import org.bouncycastle.util.encoders.Hex;

import server.FileServer;
import util.Config;
import util.FileUtils;
import util.SecurityUtils;
import util.SingleServerSocketCommunication;
import util.UserLoader;
import cli.Shell;
import client.Client;
import client.SubscribeService;

/**
 * 
 * @author Group 66 based upon Lab1 version of David
 */
public class Proxy implements Runnable {

	private Shell shell;
	private ProxyCli proxyCli;
	private ExecutorService executor;

	// Configuration Parameters
	private int tcpPort, udpPort;
	private long fsTimeout, fsCheckPeriod;
	private Mac hMac;
	private Key hMacKey;
	private PrivateKey privateKey;
	private String keyDir;

	private Set<UserLoginInfo> users;
	private Set<FileServerStatusInfo> fileservers;

	// Threads
	private ProxyDatagramSocketThread udpHandler;
	private Timer fsChecker;
	private ServerSocket serverSocket;
	private List<ProxyTCPChannel> proxyTcpHandlers;
	private boolean running;

	// Replication Parameters
	private List<FileServerStatusInfo> serverList;

	// DownloadMap
	private Map<String, Integer> downloadMap;
	// subscribe to files
	private List<SubscribeModel> subscribeList;

	// RMI
	private IRMI rmi;
	private Registry registry;
	private String rmiBindingName;

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
	 *            the config containing the infos from the proxy
	 */
	public Proxy(Shell shell, Config config) {
		init(shell, config);
	}

	/**
	 * initializes all variables
	 * 
	 * @param shell
	 *            the shell
	 * @param config
	 *            the config
	 */
	private void init(Shell shell, Config config) {
		this.executor = Executors.newCachedThreadPool();
		this.hMac = null;
		this.proxyCli = new ProxyCli(this);

		this.shell = shell;
		shell.register(proxyCli);
		executor.execute(shell);

		this.running = true;
		getProxyData(config);

		createhMAC(hMacKey);

		this.proxyTcpHandlers = new ArrayList<ProxyTCPChannel>();
		// this.fileVersionMap = new ConcurrentHashMap<String, Integer>();

		// RMI
		Config configRMI = new Config("mc");
		int rmiPort = configRMI.getInt("proxy.rmi.port");
		rmiBindingName = configRMI.getString("binding.name");

		try {
			registry = LocateRegistry.getRegistry(rmiPort);
			rmi = new RMI(this);
			registry.rebind(rmiBindingName, rmi);
		} catch (RemoteException e) {
			try {
				registry = LocateRegistry.createRegistry(rmiPort);
				rmi = new RMI(this);
				registry.rebind(rmiBindingName, rmi);
			} catch (RemoteException e1) {
				try {
					shell.writeLine(e.getMessage());
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			}
		}
	}

	/**
	 * Get the required data from the {@link Config} class
	 * 
	 * @param config
	 *            the config containing infos from the proxy
	 */
	private void getProxyData(Config config) {
		try {
			this.tcpPort = config.getInt("tcp.port");
			this.udpPort = config.getInt("udp.port");
			this.fsTimeout = config.getInt("fileserver.timeout");
			this.fsCheckPeriod = config.getInt("fileserver.checkPeriod");
			this.privateKey = SecurityUtils.readPrivateKey(
					config.getString("key"), "12345");
			this.keyDir = config.getString("keys.dir");
			byte[] keyBytes = new byte[1024];
			try {
				File hmackey = new File(config.getString("hmac.key"));
				FileInputStream fis = new FileInputStream(hmackey);
				fis.read(keyBytes);
				fis.close();
				byte[] input = Hex.decode(keyBytes);
				this.hMacKey = new SecretKeySpec(input, "HmacSHA256");

			} catch (FileNotFoundException e) {
				System.out.println("Error in getProxyData: Keyloading 1");
			} catch (IOException e) {
				System.out.println("Error in getProxyData: Keyloading 2");
			}
		} catch (NumberFormatException e) {
			try {
				shell.writeLine("The configuration file \"proxy.properties\" is invalid! \n\r");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			close();
		} catch (LoginException e1) {
			try {
				shell.writeLine(e1.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
			}
			close();
		}
		// synchronized Set
		this.fileservers = Collections
				.synchronizedSet(new HashSet<FileServerStatusInfo>());
		this.users = getUsers();
		this.serverList = new ArrayList<FileServerStatusInfo>();
		this.downloadMap = new HashMap<String, Integer>();
		this.subscribeList = new LinkedList<SubscribeModel>();
	}

	/**
	 * Returns all registered users from the users.properties file
	 * 
	 * @return all registered users from the users.properties file
	 */
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
				ProxyTCPChannel newest = new ProxyTCPChannel(this,
						serverSocket.accept());
				proxyTcpHandlers.add(newest);
				executor.execute(newest);
			}
		} catch (IOException e) {
			if (running)
				System.out.println(e.getMessage());
			// else it is ok
		} finally {
			if (running)
				close();
			// else it is already closed
		}

	}

	/**
	 * Returns all {@link FileServer}s that are registered on the Proxy This is
	 * synchronized because the fileservers may change while running through
	 * 
	 * @return all fileservers that are registered on the Proxy
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
	 * Returns all users that are registered on the Proxy and their status This
	 * Method is called by {@link ProxyCli} to send them to the {@link Client}
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
	 * {@link FileServer} the fileserver gets registered as active for the next
	 * time interval
	 * 
	 * @param fileServerTCPPort
	 *            the TCP port of the fileserver
	 * @param address
	 *            the address of the fileserver
	 */
	public void isAlive(int fileServerTCPPort, InetAddress address) {
		boolean newServer = true;
		for (FileServerStatusInfo f : fileservers) {
			if (f.getPort() == fileServerTCPPort && f.getAddress() == address) {
				f.setActive();
				newServer = false;
				break;
			}
		}
		if (newServer) {
			// if the port is not known the fileserver gets registered
			fileservers.add(new FileServerStatusInfo(address,
					fileServerTCPPort, 0, true));
		}
		serverList = changeServerSetToServerList(fileservers);
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
		FileServerStatusInfo f = getFileserverWithStatus();
		if (f != null) {
			return f.getModel();
		}
		return null;
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
	 * Iterate through all {@link FileServer}s and get the one with the lowest
	 * usage. This is synchronized because the fileservers may change
	 * 
	 * @return the fileserver with the lowest usage
	 */
	private synchronized FileServerStatusInfo getFileserverWithStatus() {
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
	 * Checks all fileservers if they are online.
	 * 
	 */
	public void checkOnline() {
		for (FileServerStatusInfo f : fileservers) {
			if (System.currentTimeMillis() - f.getActive() > fsCheckPeriod) {
				if (f.isOnline()) {
					f.setOffline();
				}
			} else if (!f.isOnline()) {
				f.setOnline();
			}
		}
		serverList = changeServerSetToServerList(fileservers);
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
	 *             if the connection does not work
	 */
	public Set<String> getFiles() throws IOException {
		List<FileServerStatusInfo> list = getGiffordsLists().get(0);

		if (list == null) {
			// TODO maybe not null but Exception
			return null;
		}

		Set<String> filenames = new LinkedHashSet<String>();
		for (int i = 0; i < list.size(); i++) {
			SingleServerSocketCommunication sender = list.get(i).getSender();

			sender.activateIntegrity(this.hMac); // XXX integrity
			Response response = sender.send(new RequestTO(new ListRequest(),
					RequestType.List));
			filenames.addAll(((ListResponse) response).getFileNames());
		}
		return filenames;
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
	public int getVersion(FileServerInfo server, String filename)
			throws IOException {
		return getVersion(getFileServer(server).getSender(), filename);
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
		sender.activateIntegrity(hMac);
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
	public synchronized Response getFileInfo(FileServerInfo server,
			String filename) {
		SingleServerSocketCommunication sender = new SingleServerSocketCommunication(
				server.getPort(), server.getAddress().getHostAddress());
		return getFileInfo(sender, filename);
	}

	/**
	 * Returns the filesize of the file with the specified filename that is
	 * located on the specified server
	 * 
	 * @param sender
	 *            the connection
	 * @param filename
	 *            the filename
	 * @return the size of the file or null if the file does not exist
	 */
	private Response getFileInfo(SingleServerSocketCommunication sender,
			String filename) {
		sender.activateIntegrity(hMac);
		return sender.send(new RequestTO(new InfoRequest(filename),
				RequestType.Info));
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
			f.addUsage(usage);
		}
	}

	/**
	 * This method returns the {@link FileServerStatusInfo} from the
	 * {@link FileServerInfo}
	 * 
	 * @param server
	 *            the model {@link FileServerInfo}
	 * @returns the info {@link FileServerStatusInfo}
	 */
	private FileServerStatusInfo getFileServer(FileServerInfo server) {
		for (FileServerStatusInfo f : fileservers) {
			if (f.equalsFileServerInfo(server)) {
				return f;
			}
		}
		// no fileserver found
		return null;
	}

	/**
	 * 
	 * @param request
	 * @throws IOException
	 */
	public void uploadFile(UploadRequest request) throws IOException {
		ArrayList<List<FileServerStatusInfo>> list = getGiffordsLists();
		List<FileServerStatusInfo> fnr = list.get(0);
		List<FileServerStatusInfo> fnw = list.get(1);

		boolean exists = false;
		Set<String> files = getFiles();
		if (files.contains(request.getFilename())) {
			exists = true;
		}

		// if the file exists
		int currentVersion = 0;
		if (exists) {
			// get highest version
			for (int i = 0; i < fnr.size(); i++) {
				if (currentVersion < getVersion(fnr.get(i).getModel(),
						request.getFilename())) {
					currentVersion = getVersion(fnr.get(i).getModel(),
							request.getFilename());
				}
			}
		}
		currentVersion++;

		RequestTO requestWithVersion = new RequestTO(new UploadRequest(
				request.getFilename(), currentVersion, request.getContent()),
				RequestType.Upload);

		for (FileServerStatusInfo f : fnw) {
			f.getSender().activateIntegrity(this.hMac);
			f.getSender().send(requestWithVersion);
			f.getSender().close();
			f.addUsage(request.getContent().length);
		}
	}

	/**
	 * Returns the nr and nw quorum lists of servers
	 * 
	 * @return arrayList of list of fileServerStatusInfo on postion 0 is the nr
	 *         list and on position 1 the nw list
	 */
	public ArrayList<List<FileServerStatusInfo>> getGiffordsLists() {
		if (serverList.size() != 0) {
			List<FileServerStatusInfo> fnr = getServerWithLowestUsage(serverList);
			List<FileServerStatusInfo> fnw = getServerWithLowestUsage(serverList);

			// if there not enough servers in the list to fulfil the giffords
			// scheme
			if (fnw.size() + fnr.size() <= serverList.size()) {
				int missingServers = serverList.size() + 1 - fnw.size()
						- fnr.size();
				int count = 0;
				float currentUsage = 0;
				for (int i = 0; i < serverList.size(); i++) {
					if (serverList.get(i).getUsage() == fnw.get(0).getUsage()) {
						currentUsage = serverList.get(i).getUsage();
						break;
					}
				}

				// add the servers with the second, third, ... lowest usage to
				// the
				// list until we have enough servers
				while (count < missingServers) {
					for (int i = 0; i < serverList.size(); i++) {
						if (fnw.get(0).getUsage() <= serverList.get(i)
								.getUsage()
								&& serverList.get(i).getUsage() <= currentUsage
								&& count < missingServers) {
							fnw.add(serverList.get(i));
							count++;
						}
					}
				}
			}
			ArrayList<List<FileServerStatusInfo>> list = new ArrayList<List<FileServerStatusInfo>>();
			list.add(fnr);
			list.add(fnw);
			return list;
		}
		return null;
	}

	/**
	 * Returns a list of servers, which have the lowest usage
	 * 
	 * @param quorums
	 *            the list of fileServerStatusInfos, either nr or nw
	 * @return list of fileServerStatusInfo
	 */
	private List<FileServerStatusInfo> getServerWithLowestUsage(
			List<FileServerStatusInfo> quorums) {
		// get lowest usage
		if (quorums.size() != 0) {
			long usage = quorums.get(0).getUsage();
			for (int i = 0; i < quorums.size(); i++) {
				if (usage > quorums.get(i).getUsage()) {
					usage = quorums.get(i).getUsage();
				}
			}

			// get list of servers with lowest usage
			int full = serverList.size() / 2;
			if (serverList.size() == 1) {
				full = serverList.size();
			}
			int count = 1;
			List<FileServerStatusInfo> list = new ArrayList<FileServerStatusInfo>();
			for (int i = 0; i < quorums.size(); i++) {
				if (usage == quorums.get(i).getUsage() && full >= count) {
					list.add(quorums.get(i));
					count++;
				}
			}
			return list;
		}
		return null;
	}

	/**
	 * Changes the ServerSet to a ServerList
	 * 
	 * @param set
	 *            of FileServerStatusInfo
	 * @return a list of FileServerStatusInfo
	 */
	private List<FileServerStatusInfo> changeServerSetToServerList(
			Set<FileServerStatusInfo> set) {
		List<FileServerStatusInfo> list = new ArrayList<FileServerStatusInfo>();
		Iterator<FileServerStatusInfo> it = set.iterator();
		while (it.hasNext()) {
			list.add(it.next());
		}
		return list;
	}

	/**
	 * Returns the number of read quorums
	 * 
	 * @return number
	 */
	public int getReadQuorums() {
		if (getGiffordsLists() != null) {
			List<FileServerStatusInfo> list = getGiffordsLists().get(0);
			return list.size();
		}
		return 0;
	}

	/**
	 * Returns the number of write quorums
	 * 
	 * @return number
	 */
	public int getWriteQuorums() {
		if (getGiffordsLists() != null) {
			List<FileServerStatusInfo> list = getGiffordsLists().get(1);
			return list.size();
		}
		return 0;
	}

	/**
	 * Returns the private key from the proxy
	 * 
	 * @return the private key from the proxy
	 */
	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	/**
	 * Returns the Public Key from the specified user
	 * 
	 * @param username
	 *            the username of the user
	 * @return the Public Key from the specified user
	 */
	public PublicKey getUserPublicKey(String username) {
		if (FileUtils.check(keyDir, username + ".pub.pem")) {
			return SecurityUtils.readPublicKey(keyDir + File.separator
					+ username + ".pub.pem");
		}
		return null;
	}

	/**
	 * Returns the public key of the Proxy.
	 * 
	 * @return PublicKey of the Proxy.
	 */
	public PublicKey getProxyPublicKey() {
		return SecurityUtils.readPublicKey(keyDir + File.separator
				+ "proxy.pub.pem");
	}

	/**
	 * Sets the public key of the user.
	 * 
	 * @param username
	 *            name of the user
	 * @param publicKey
	 *            public key of the user
	 * @return true, if the public key of the user was correctly stored, else
	 *         false
	 */
	public boolean setUserPublicKey(String username, PublicKey publicKey) {
		return SecurityUtils.storePublicKey(publicKey, keyDir + File.separator
				+ username + ".pub.pem");
	}

	/**
	 * Return a Map of 3 files and the number of their downloads, which were
	 * downloaded the most.
	 * 
	 * @return a HashMap of String and Integer
	 */
	public LinkedHashMap<String, Integer> topThreeDownloads() {
		LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
		Set<String> set = new LinkedHashSet<String>();
		set.addAll(downloadMap.keySet());

		if (!set.isEmpty()) {
			String file = set.iterator().next();
			int top = downloadMap.get(file);
			while (map.size() < 3) {
				for (String s : set) {
					int value = downloadMap.get(s);
					if (value > top) {
						top = value;
						file = s;
					}
				}
				map.put(file, top);
				set.remove(file);
				if (set.size() == 0) {
					return map;
				}
				file = set.iterator().next();
				top = downloadMap.get(file);
			}
		}
		return map;
	}

	/**
	 * Increases the number of downloads of the file
	 * 
	 * @param filename
	 *            the name of the file
	 */
	public void increaseDownloadNumber(String filename) {
		if (!downloadMap.containsKey(filename)) {
			downloadMap.put(filename, 1);
		} else {
			int count = downloadMap.get(filename);
			downloadMap.put(filename, ++count);
		}
		for (SubscribeModel m : subscribeList) {
			if (m.getFileName().equals(filename)) {
				m.addDownload();
			}
		}
	}

	/**
	 * Closes all Sockets and Streams
	 */
	public void close() {
		running = false;
		// shut down RMI
		try {
			registry.unbind(rmiBindingName);
			UnicastRemoteObject.unexportObject(rmi, true);
		} catch (NoSuchObjectException e1) {
			e1.printStackTrace();
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (executor != null)
			executor.shutdown();
		if (udpHandler != null)
			udpHandler.close();
		if (fsChecker != null)
			fsChecker.cancel();
		for (ProxyTCPChannel t : proxyTcpHandlers) {
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
	 * @param subscribe
	 * @param filename
	 * @param number
	 */
	public void startCountDownloads(SubscribeService subscribe,
			String filename, int number) {
		subscribeList.add(new SubscribeModel(subscribe, filename, number));
	}

	/**
	 * @return the hmac
	 */
	public Mac getHMac() {
		return hMac;
	}
}
