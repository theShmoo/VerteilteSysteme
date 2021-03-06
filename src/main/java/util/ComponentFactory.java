package util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import proxy.IProxyCli;
import proxy.Proxy;
import proxy.ProxyCli;
import server.FileServer;
import server.FileServerCli;
import server.IFileServerCli;
import cli.Shell;
import client.Client;
import client.ClientCli;
import client.IClientCli;

/**
 * Provides methods for starting an arbitrary amount of various components.
 */
public class ComponentFactory {
	
	private ExecutorService executor = Executors.newCachedThreadPool();

	/**
	 * Creates and starts a new client instance using the provided {@link Config} and {@link Shell}.
	 *
	 * @param config the configuration containing parameters such as connection info
	 * @param shell  the {@code Shell} used for processing commands
	 * @return the created component after starting it successfully
	 * @throws Exception if an exception occurs
	 */
	public IClientCli startClient(Config config, Shell shell) throws Exception {
		Client client = new Client(shell,config);
		executor.execute(client);
		return new ClientCli(client);
	}

	/**
	 * Creates and starts a new proxy instance using the provided {@link Config} and {@link Shell}.
	 *
	 * @param config the configuration containing parameters such as connection info
	 * @param shell  the {@code Shell} used for processing commands
	 * @return the created component after starting it successfully
	 * @throws Exception if an exception occurs
	 */
	public IProxyCli startProxy(Config config, Shell shell) throws Exception {
		Proxy proxy = new Proxy(shell,config);
		executor.execute(proxy);
		return new ProxyCli(proxy);
	}

	/**
	 * Creates and starts a new file server instance using the provided {@link Config} and {@link Shell}.
	 *
	 * @param config the configuration containing parameters such as connection info
	 * @param shell  the {@code Shell} used for processing commands
	 * @return the created component after starting it successfully
	 * @throws Exception if an exception occurs
	 */
	public IFileServerCli startFileServer(Config config, Shell shell) throws Exception {
		FileServer server = new FileServer(shell,config);
		executor.execute(server);
		return new FileServerCli(server);
	}
}
