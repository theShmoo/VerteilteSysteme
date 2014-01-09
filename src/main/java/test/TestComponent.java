/**
 * 
 */
package test;

import java.io.IOException;
import java.rmi.RemoteException;
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

import message.Response;
import message.response.MessageResponse;
import proxy.IProxyCli;
import server.IFileServerCli;
import util.ComponentFactory;
import util.Config;
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
		users = new HashMap<>();
		users.put("alice", "12345");
		users.put("bill", "23456");

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
		}, 10000);
//		close();
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
		clientSet = new HashSet<ClientCli>();
		while (count <= clients) {
			try {
				clientSet.add((ClientCli)factory.startClient(new Config("client"), new Shell("client", new TestOutputStream(System.out), new TestInputStream())));
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
		for (ClientCli cli : clientSet) {
			String username = userList.get(count);
			cli.login(username, users.get(username));
			count++;
		}
	}
	
	public void uploadFiles(final String filename) {
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				for (ClientCli cli : clientSet) {
					try {
						System.out.println(cli.upload(filename));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}, 0, uploadsPerMin * 10000 / 60);
	}
	
	public void downloadFiles(final String filename) {
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				for (IClientCli cli : clientSet) {
					try {
						System.out.println(cli.download(filename));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}, 0, downloadsPerMin * 10000 / 60);
	}

	public Response subscribe(String filename, int downloadFileNr) {
		try {
			return clientSet.iterator().next().subscribe(filename, downloadFileNr);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new MessageResponse("Subscribe failed.");
	}

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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (clientSet != null) {
			for (ClientCli client : clientSet) {
				try {
					client.exit();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (serverSet != null) {
			for (IFileServerCli server : serverSet) {
				try {
					server.exit();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
