package proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.Response;
import message.request.BuyRequest;
import message.request.DownloadTicketRequest;
import message.request.LoginRequest;
import message.request.UploadRequest;
import message.response.LoginResponse;
import message.response.MessageResponse;
import model.FileServerInfo;
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
	private List<FileServerInfo> fileservers;

	public Proxy() {
		init(new Shell("Proxy", System.out, System.in));
	}

	public Proxy(Shell shell) {
		init(shell);
	}

	private void init(Shell shell) {
		this.shell = shell;
		this.proxyCli = new ProxyCli(this);
		this.executor = Executors.newCachedThreadPool();

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
		this.fileservers = new ArrayList<FileServerInfo>();
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
		shell.run();

		boolean listening = true;

		try (ServerSocket serverSocket = new ServerSocket(tcpPort)) {
			while (listening) {
				// Starting the DatagramSocket
				executor.execute(new ProxyDatagramSocketThread(udpPort));
				System.out.println("UDP running?");
				// Starting the ServerSocket
				executor.execute(new ProxyServerSocketThread(serverSocket
						.accept()));
			}
			executor.shutdown();
		} catch (IOException e) {
			System.err.println("Could not listen on port " + tcpPort);
			System.exit(-1);
		}
	}

	@Override
	public LoginResponse login(LoginRequest request) throws IOException {
		// TODO implement login
		return null;
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
		// TODO implement logout
		return null;
	}

	public List<FileServerInfo> getFileServerInfos() {
		return fileservers;
	}

}
