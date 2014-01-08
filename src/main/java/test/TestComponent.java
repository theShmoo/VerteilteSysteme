/**
 * 
 */
package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import proxy.IProxyCli;
import server.IFileServerCli;
import util.ComponentFactory;
import util.Config;
import cli.Shell;
import cli.TestInputStream;
import cli.TestOutputStream;
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
	private Set<IClientCli> clientSet;
	private List<String> userList;
	private Map<String, String> users;

	public TestComponent() {
		init(new Shell("TestComponent", System.out, System.in), new Config("loadtest"));
	}

	public TestComponent(Shell shell, Config config) {
		init(shell, config);
	}

	private void init(Shell shell, Config config) {
		this.executor = Executors.newCachedThreadPool();
		this.testComponentCli = new TestComponentCli(this);
		this.factory = new ComponentFactory();
		this.timer = new Timer();

		this.shell = shell;
		shell.register(testComponentCli);
		executor.execute(shell);

		getTestComponentData(config);
		userList = new ArrayList<String>();
		userList.add("alice");
		userList.add("bill");
		//users = new HashMap<>();
		users.put("alice", "12345");
		users.put("bill", "23456");

		startProxy();
		createClients(clients);
		startFileServers();
		loginUser(clients);
		
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				uploadFiles();
				
			}
		}, 0, (long) uploadsPerMin);
		
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				downloadFiles();
			}
		}, 0, (long) downloadsPerMin);
	}

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

	public static void main(String[] args) {
		new TestComponent();
	}

	private void startProxy() {
		try {
			proxy = factory.startProxy(new Config("proxy"), new Shell("proxy", new TestOutputStream(System.out), new TestInputStream()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createClients(int clients) {
		int count = 1;
		clientSet = new HashSet<IClientCli>();
		while (count <= clients) {
			try {
				clientSet.add(factory.startClient(new Config("client"), new Shell("client", new TestOutputStream(System.out), new TestInputStream())));
				count++;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void startFileServers() {
		serverSet = new HashSet<IFileServerCli>();
		try {
			serverSet.add(factory.startFileServer(new Config("fs1"), new Shell("fs1", new TestOutputStream(System.out), new TestInputStream())));
			serverSet.add(factory.startFileServer(new Config("fs2"), new Shell("fs2", new TestOutputStream(System.out), new TestInputStream())));
			serverSet.add(factory.startFileServer(new Config("fs3"), new Shell("fs3", new TestOutputStream(System.out), new TestInputStream())));
			serverSet.add(factory.startFileServer(new Config("fs4"), new Shell("fs4", new TestOutputStream(System.out), new TestInputStream())));
			serverSet.add(factory.startFileServer(new Config("fs5"), new Shell("fs5", new TestOutputStream(System.out), new TestInputStream())));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loginUser(int clients) {
		int count = 0;
		for (IClientCli cli : clientSet) {
			String username = userList.get(count);
			try {
				cli.login(username, users.get(username));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			count++;
		}
	}
	
	private void uploadFiles() {
		for (IClientCli cli : clientSet) {
			try {
				System.out.println(cli.upload("long.txt"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void downloadFiles() {
		for (IClientCli cli : clientSet) {
			try {
				System.out.println(cli.download("data.txt"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void close() {
		if (executor != null)  {
			executor.shutdown();
		}
		if (shell != null) {
			shell.close();
		} if (timer != null) {
			timer.cancel();
		}
	}
}
