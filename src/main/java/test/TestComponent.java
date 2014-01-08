/**
 * 
 */
package test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.Response;
import message.response.MessageResponse;
import proxy.Proxy;
import server.FileServer;
import util.ComponentFactory;
import util.Config;
import cli.Shell;
import client.Client;
import client.ClientCli;

/**
 * 
 * @author Astrid
 */
public class TestComponent {

	private ExecutorService executor;
	private Shell shell;
	private TestComponentCli testComponentCli;
	
	private int clients;
	private int uploadsPerMin;
	private int downloadsPerMin;
	private int fileSizeKB;
	private double overwriteRatio;
	
	private Proxy proxy;
	private Set<FileServer> serverSet;
	private Set<ClientCli> clientSet;
	
	public TestComponent() {
		init(new Shell("TestComponent", System.out, System.in), new Config("loadtest"));
	}
	
	public TestComponent(Shell shell, Config config) {
		init(shell, config);
	}
	
	private void init(Shell shell, Config config) {
		this.executor = Executors.newCachedThreadPool();
		this.testComponentCli = new TestComponentCli(this);
		
		this.shell = shell;
		shell.register(testComponentCli);
		executor.execute(shell);
		
		getTestComponentData(config);
		
		startProxy();
		createClients(clients);
		startFileServers();
//		uploadFiles(uploadsPerMin);
//		downloadFiles(downloadsPerMin);
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
		proxy = new Proxy();
	}
	
	private void createClients(int clients) {
		int count = 1;
		clientSet = new HashSet<ClientCli>();
		while (count <= clients) {
//			clientSet.add(ComponentFactory.startClient(new Config("client"), new Shell()));
//			count++;
		}
	}
	
	private void startFileServers() {
		serverSet = new HashSet<FileServer>();
		serverSet.add(new FileServer("fs1"));
		serverSet.add(new FileServer("fs2"));
		serverSet.add(new FileServer("fs3"));
		serverSet.add(new FileServer("fs4"));
		serverSet.add(new FileServer("fs5"));
	}
	
	public void close() {
		if (executor != null)  {
			executor.shutdown();
		}
		if (shell != null) {
			shell.close();
		}
	}
	
	public Response list() {
		String[] array = serverSet.iterator().next().getFileNames();
		return new MessageResponse(array[0]);
	}
}
