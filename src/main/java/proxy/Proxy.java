package proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.Response;
import message.request.BuyRequest;
import message.request.DownloadTicketRequest;
import message.request.LoginRequest;
import message.request.UploadRequest;
import message.response.LoginResponse;
import message.response.LoginResponse.Type;
import message.response.MessageResponse;
import model.FileServerInfo;
import model.UserInfo;
import model.UserLoginInfo;
import util.Config;
import cli.Shell;

/**
 * 
 * @history 14.10.2013 created
 * @version 14.10.2013 version 0.1
 * @author David
 */
public class Proxy implements IProxy {

	private Shell shell;
	private Config proxyConfig;
	private ProxyCli proxyCli;
	private ExecutorService executor;

	// Configuration Parameters
	private int tcpPort, udpPort;
	private long fsTimeout, fsCheckPeriod;

	private List<UserLoginInfo> users;
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
		this.shell = shell;
		this.proxyCli = new ProxyCli(this);
		this.executor = Executors.newCachedThreadPool();
		this.running=true;

		getProxyData();
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

	private List<UserLoginInfo> getUsers() {

		List<UserLoginInfo> users = new ArrayList<UserLoginInfo>();

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

	private void run() {
		shell.register(proxyCli);

		executor.execute(shell);
		// Starting the DatagramSocket
		executor.execute(new ProxyDatagramSocketThread(this));
		// Starting the Garbage Collector for the fileservers
		executor.execute(new FileServerGarbageCollector(fileservers, fsTimeout,
				fsCheckPeriod));
		try (ServerSocket serverSocket = new ServerSocket(tcpPort)) {
			while(running){
				executor.execute(new ProxyServerSocketThread(this,serverSocket.accept()));
			}
		}
		// Starting the ServerSocket
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		

	}

	@Override
	public LoginResponse login(LoginRequest request) throws IOException {

		for (UserLoginInfo u : users) {
			if (u.getName().equals(request.getUsername())
					&& u.getPassword().equals(request.getPassword())) {
				u.setOnline();
				return new LoginResponse(Type.SUCCESS);
			}
		}

		return new LoginResponse(Type.WRONG_CREDENTIALS);
	}

	@Override
	public Response credits() throws IOException {
		// TODO implement credits
		return null;
	}

	@Override
	public Response buy(BuyRequest credits) throws IOException {
		// TODO implement buy
		return null;
	}

	@Override
	public Response list() throws IOException {
		// TODO implement list
		return null;
	}

	@Override
	public Response download(DownloadTicketRequest request) throws IOException {
		// TODO implement download
		return null;
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		// TODO implement upload
		return null;
	}

	@Override
	public MessageResponse logout() throws IOException {
		return new MessageResponse("Logged out!");
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
	public List<UserLoginInfo> getUserLoginInfos() {
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

	public int getTcpPort() {
		return tcpPort;
	}

	public int getUdpPort() {
		return udpPort;
	}

}
