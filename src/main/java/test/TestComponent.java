/**
 * 
 */
package test;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.Response;
import message.response.MessageResponse;
import model.UserLoginInfo;
import proxy.IProxyCli;
import server.IFileServerCli;
import util.ComponentFactory;
import util.Config;
import util.UserLoader;
import cli.Shell;
import cli.TestInputStream;
import cli.TestOutputStream;
import client.ClientCli;
import client.IClientCli;

/**
 * 
 * @author Astrid
 */
public class TestComponent {

	private ExecutorService executor;
	private Shell shell;
	private TestComponentCli testComponentCli;
	private ComponentFactory factory;
	private Timer timer;

	private int clients;
	private int uploadsPerMin;
	private int downloadsPerMin;
	private int fileSizeKB;
	private double overwriteRatio;

	private IProxyCli proxy;
	private Set<IFileServerCli> serverSet;
	private Set<ClientCli> clientSet;
	private Set<UserLoginInfo> users;

	/**
	 * Initialize a new TestComponent
	 */
	public TestComponent() {
		init(new Shell("TestComponent", System.out, System.in), new Config("loadtest"));
	}

	/**
	 * Initialize a new TestComponent with a {@link Shell}
	 * 
	 * @param shell
	 *            the {@link Shell} of the TestComponent
	 * @param config
	 *            the config containing the infos from the TestComponent
	 */
	public TestComponent(Shell shell, Config config) {
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
		this.testComponentCli = new TestComponentCli(this);
		this.factory = new ComponentFactory();
		this.timer = new Timer();

		this.shell = shell;
		shell.register(testComponentCli);
		executor.execute(shell);

		getTestComponentData(config);
		this.users = getUsers();

		//starting the components
		startProxy();
		createClients(clients);
		startFileServers();
		loginUser(clients);
		
		//downloads and uploads
		uploadFiles("long.txt");
		downloadFiles("data.txt");
		
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				close();
			}
		}, 300000);
	}

	/**
	 * Get the required data from the {@link Config} class
	 * 
	 * @param config
	 *            the config containing infos from the testComponent
	 */
	private void getTestComponentData(Config config) {
		try {
			this.clients = config.getInt("clients");
			this.uploadsPerMin = config.getInt("uploadsPerMin");
			this.downloadsPerMin = config.getInt("downloadsPerMin");
			this.fileSizeKB = config.getInt("fileSizeKB");
			this.overwriteRatio = Double.parseDouble(config.getString("overwriteRatio"));
		} catch (NumberFormatException e) {
			try {
				shell.writeLine("The configuration file \"loadtest.properties\" is invalid! \n\r");
				System.out.println("The configuration file \"loadtest.properties\" is invalid! \n\r");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			close();
		}
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
	 * Starts the TestComponent
	 * 
	 * @param args
	 *            no args are used
	 */
	public static void main(String[] args) {
		new TestComponent();
	}

	/**
	 * Starts the Proxy.
	 */
	private void startProxy() {
		try {
			proxy = factory.startProxy(new Config("proxy"), new Shell("proxy", new TestOutputStream(System.out), new TestInputStream()));
		} catch (Exception e) {
			System.out.println("Starting the proxy failed.");
		}
	}

	/**
	 * Starts the Clients.
	 * 
	 * @param clients the number of clients, which should be started.
	 */
	private void createClients(int clients) {
		int count = 1;
		clientSet = new HashSet<ClientCli>();
		while (count <= clients) {
			try {
				clientSet.add((ClientCli)factory.startClient(new Config("client"), new Shell("client", new TestOutputStream(System.out), new TestInputStream())));
				count++;
			} catch (Exception e) {
				System.out.println("Creating the clients failed.");
			}
		}
	}

	/**
	 * Starts all Fileservers, which are registered at the system.
	 */
	private void startFileServers() {
		serverSet = new HashSet<IFileServerCli>();
		try {
			serverSet.add(factory.startFileServer(new Config("fs1"), new Shell("fs1", new TestOutputStream(System.out), new TestInputStream())));
			serverSet.add(factory.startFileServer(new Config("fs2"), new Shell("fs2", new TestOutputStream(System.out), new TestInputStream())));
			serverSet.add(factory.startFileServer(new Config("fs3"), new Shell("fs3", new TestOutputStream(System.out), new TestInputStream())));
			serverSet.add(factory.startFileServer(new Config("fs4"), new Shell("fs4", new TestOutputStream(System.out), new TestInputStream())));
			serverSet.add(factory.startFileServer(new Config("fs5"), new Shell("fs5", new TestOutputStream(System.out), new TestInputStream())));
		} catch (Exception e) {
			System.out.println("Starting the fileservers failed.");
		}
	}

	/**
	 * Logins so many user, which there are clients.
	 * 
	 * @param clients number of clients, which uses a user, which is logged-in.
	 */
	private void loginUser(int clients) {
		for (ClientCli cli : clientSet) {
			UserLoginInfo info = users.iterator().next();
			cli.login(info.getName(), info.getPassword());
		}
	}
	
	/**
	 * Uploads the files for every user, which is logged-in, in a specific time.
	 * 
	 * @param filename name of the file, which should be uploaded.
	 */
	public synchronized void uploadFiles(final String filename) {
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				for (ClientCli cli : clientSet) {
					try {
						System.out.println(cli.upload(filename));
					} catch (IOException e) {
						System.out.println("Uploading the files failed.");
					}
				}
			}
		}, 0, uploadsPerMin * 100000 / 60);
	}
	
	/**
	 * Downloads the file for every user, which is logged-in, in a specific time.
	 * 
	 * @param filename name of the file, which should be downloaded.
	 */
	public synchronized void downloadFiles(final String filename) {
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				for (IClientCli cli : clientSet) {
					try {
						System.out.println(cli.download(filename));
					} catch (IOException e) {
						System.out.println("Downloading the files failed.");
					}
				}
			}
		}, 0, downloadsPerMin * 100000 / 60);
	}

	/**
	 * Subscribes for a specific file.
	 * 
	 * @param filename
	 * @param downloadFileNr
	 * @return a Message, when it downloadFileNr files has downloaded
	 */
	public synchronized Response subscribe(String filename, int downloadFileNr) {
		try {
			return clientSet.iterator().next().subscribe(filename, downloadFileNr);
		} catch (RemoteException e) {
			System.out.println("Subscribing failed.");
		}
		return new MessageResponse("Subscribe failed.");
	}

	/**
	 * Closes all Sockets and Streams
	 */
	public void close() {
		if (executor != null)  {
			executor.shutdown();
		} 
		if (timer != null) {
			timer.cancel();
		}
		if (executor != null) {
			executor.shutdownNow();
		}
		if (shell != null) {
			shell.close();
		}
		if (proxy != null) {
			try {
				proxy.exit();
			} catch (IOException e) {
				System.out.println("Closing the proxy failed.");
			}
		}
		if (clientSet != null) {
			for (ClientCli client : clientSet) {
				try {
					client.exit();
				} catch (IOException e) {
					System.out.println("Closing the client failed.");
				}
			}
		}
		if (serverSet != null) {
			for (IFileServerCli server : serverSet) {
				try {
					server.exit();
				} catch (IOException e) {
					System.out.println("Closing the fileserver failed.");
				}
			}
		}
		try {
			System.in.close();
		} catch (IOException e) {
			System.out.println("Closing the system failed.");
		}
	}
}
