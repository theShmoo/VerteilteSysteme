/**
 * 
 */
package test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
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
import util.FileUtils;
import util.NullOutputStream;
import util.TestUtils;
import util.UserLoader;
import cli.Shell;
import cli.TestInputStream;
import cli.TestOutputStream;
import client.ClientCli;
import client.IClientCli;

/**
 * 
 * @author Astrid
 * @author David
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
	private ClientCli subscribeClient;
	private Map<IFileServerCli,File> serverMap;
	private Map<ClientCli, File> clientMap;
	private Set<UserLoginInfo> users;
	private File overwriteFile;

	/**
	 * Initialize a new TestComponent
	 */
	public TestComponent() {
		init(new Shell("TestComponent", System.out, System.in), new Config(
				"loadtest"));
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

		// starting the components
		startProxy();
		createClients(clients);
		startFileServers();
		loginUser(clients);

		// creating the files
		createOverwriteFiles();
		subscribeToOverwriteFiles();

		// downloads and uploads
		startUploadingFiles();
		startDownloadFiles(overwriteFile.getName());

		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				close();
			}
		}, 5*60*1000);
		try {
			StringBuilder s = new StringBuilder();
			s.append(TestUtils.repeat('#', 40));
			s.append('\n');
			s.append("# Test Component started!");
			s.append('\n');
			s.append("#\tNumber of Clients:\t");
			s.append(clients);
			s.append('\n');
			s.append("#\tUploads per Minute:\t");
			s.append(uploadsPerMin);
			s.append('\n');
			s.append("#\tDownloads per Minute:\t");
			s.append(downloadsPerMin);
			s.append('\n');
			s.append("#\tSize of Files in KB:\t");
			s.append(fileSizeKB);
			s.append('\n');
			s.append("#\tOverwrite Ratio:\t");
			s.append(overwriteRatio);
			s.append('\n');
			s.append(TestUtils.repeat('#', 40));
			shell.writeLine(s.toString());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * 
	 */
	private void subscribeToOverwriteFiles() {
		try {
			subscribeClient.subscribe(overwriteFile.getName(),10);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
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
			this.overwriteRatio = Double.parseDouble(config
					.getString("overwriteRatio"));
		} catch (NumberFormatException e) {
			try {
				shell.writeLine("The configuration file \"loadtest.properties\" is invalid! \n\r");
				System.out
						.println("The configuration file \"loadtest.properties\" is invalid! \n\r");
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
			proxy = factory.startProxy(new Config("proxy"), new Shell("proxy",
					new TestOutputStream(System.out), new TestInputStream()));
		} catch (Exception e) {
			System.out.println("Starting the proxy failed.");
		}
	}

	/**
	 * Creates the files to upload to test the overwriteRatio
	 */
	private void createOverwriteFiles() {

		try {
			overwriteFile = FileUtils.createRandomFile(fileSizeKB, FileUtils.createTempDirectory("overwrite"));
		} catch (IOException e) {
			try {
				shell.writeLine("Creating the overwrite File failed!\n"
						+ e.getMessage());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		for (File f : clientMap.values()) {
			File dest = new File(f.getAbsoluteFile()+File.separator+overwriteFile.getName());
			dest.deleteOnExit();
			FileUtils.copyFile(overwriteFile,dest);
		}
		
		for (File f : serverMap.values()) {
			File dest = new File(f.getAbsoluteFile()+File.separator+overwriteFile.getName());
			dest.deleteOnExit();
			FileUtils.copyFile(overwriteFile,dest);
		}
	}

	/**
	 * Starts all Fileservers, which are registered at the system.
	 */
	private void startFileServers() {
		serverMap = new HashMap<IFileServerCli,File>();
		try {
			
			for(int i = 1; i <= 5; i++ ){
				
				IFileServerCli fs = factory.startFileServer(new Config("fs"+i), new Shell(
						"fs"+i, NullOutputStream.getInstance(),
						new TestInputStream()));
				File f = FileUtils.createTempDirectory("fileserver");
				fs.changeDirectory(f.toString());
				serverMap.put(fs, f);
			}
		} catch (Exception e) {
			System.out.println("Starting the fileservers failed.");
		}
	}
	
	/**
	 * Starts the Clients.
	 * 
	 * @param clients
	 *            the number of clients, which should be started.
	 */
	private void createClients(int clients) {
		int count = 1;
		clientMap = new HashMap<ClientCli, File>();
		try {
			while (count <= clients) {
				ClientCli c = (ClientCli) factory.startClient(new Config(
						"client"),
						new Shell("client", NullOutputStream.getInstance(),
								new TestInputStream()));
				File f = FileUtils.createTempDirectory("client");
				c.changeDownloadDir(f.getPath());
				clientMap.put(c, f);
				count++;
			}
			subscribeClient = (ClientCli) factory.startClient(new Config(
					"client"), new Shell("client",
							new TestOutputStream(System.out), new TestInputStream()));
		} catch (Exception e) {
			try {
				shell.writeLine("Creating the clients failed!\n"
						+ e.getMessage());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Logins so many user, which there are clients.
	 * 
	 * @param clients
	 *            number of clients, which uses a user, which is logged-in.
	 */
	private void loginUser(int clients) {
		UserLoginInfo info = users.iterator().next();
		subscribeClient.login(info.getName(), info.getPassword());
		for (ClientCli cli : clientMap.keySet()) {
			info = users.iterator().next();
			cli.login(info.getName(), info.getPassword());
		}
	}

	/**
	 * Uploads the files for every user, which is logged-in, in a specific time.
	 */
	public synchronized void startUploadingFiles() {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				for (ClientCli cli : clientMap.keySet()) {
					upload(cli);
				}
			}
		}, 0, uploadsPerMin * 1000 / 60);
	}

	/**
	 * @param cli
	 */
	protected synchronized void upload(ClientCli cli) {

		try {
			File f;
			if (Math.random() <= overwriteRatio) {
				//change overwrite File and upload it
				RandomAccessFile rand = new RandomAccessFile(overwriteFile, "rw");
				byte[] r = new byte[fileSizeKB*1024]; //dont fill whole file
				new Random().nextBytes(r);
				rand.writeBytes(new String(r));
				rand.close();
				f = overwriteFile;
			} else {
				f = FileUtils.createRandomFile(fileSizeKB, clientMap.get(cli));
			}
			cli.upload(f.getName());
		} catch (IOException e) {
			try {
				shell.writeLine("Uploading Failed!\n"
						+ e.getMessage());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Downloads the file for every user, which is logged-in, in a specific
	 * time.
	 * 
	 * @param filename
	 *            name of the file, which should be downloaded.
	 */
	public synchronized void startDownloadFiles(final String filename) {
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				for (IClientCli cli : clientMap.keySet()) {
					try {
						cli.download(filename);
					} catch (IOException e) {
						System.out.println("Downloading the files failed.");
					}
				}
			}
		}, 0, downloadsPerMin * 1000 / 60);
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
			return subscribeClient.subscribe(filename, downloadFileNr);
		} catch (RemoteException e) {
			System.out.println("Subscribing failed.");
		}
		return new MessageResponse("Subscribe failed.");
	}

	/**
	 * Closes all Sockets and Streams
	 */
	public void close() {
		if (executor != null) {
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
		if (clientMap != null) {
			for (ClientCli client : clientMap.keySet()) {
				try {
					client.exit();
				} catch (IOException e) {
					System.out.println("Closing the client failed.");
				}
			}
			for (File f : clientMap.values()){
				FileUtils.deleteFolder(f);
			}
		}
		if (serverMap != null) {
			for (IFileServerCli server : serverMap.keySet()) {
				try {
					server.exit();
				} catch (IOException e) {
					System.out.println("Closing the fileserver failed.");
				}
			}
			for (File f : serverMap.values()){
				FileUtils.deleteFolder(f);
			}
		}
		FileUtils.deleteFolder(overwriteFile.getParentFile());
		try {
			System.in.close();
		} catch (IOException e) {
			System.out.println("Closing the system failed.");
		}
		System.exit(0);
	}
}
