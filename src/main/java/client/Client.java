package client;

import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import message.Request;
import message.Response;
import message.request.DownloadFileRequest;
import message.response.DownloadFileResponse;
import message.response.LoginResponse;
import message.response.LoginResponse.Type;
import message.response.MessageResponse;
import model.DownloadTicket;
import model.FileInfo;
import model.RequestTO;
import model.RequestType;

import org.bouncycastle.util.encoders.Base64;

import proxy.IRMI;
import proxy.Proxy;
import server.FileServer;
import util.Config;
import util.FileUtils;
import util.SecurityUtils;
import util.SingleServerSocketCommunication;
import cli.Shell;

/**
 * Implements the interface {@link IClient}
 * 
 * @author David
 */
public class Client implements IClient, Runnable {

	private Shell shell;
	private Thread shellThread;
	private ClientCli clientCli;
	private SingleServerSocketCommunication clientThread;

	// Client properties
	private String downloadDir = "";
	private File folder;
	private String proxyHost = "";
	private int tcpPort;

	// Ram data
	private List<FileInfo> files;

	// security
	private PublicKey proxyPublicKey;
	private String keyDir;
	private byte[] clientChallenge;
	private byte[] secretKey;
	private byte[] IV;
	
	// RMI
	private IRMI rmi = null;

	/**
	 * Create a new Client with the given {@link Shell} for its commands
	 * 
	 * @param shell
	 *            the {@link Shell}
	 * @param config
	 */
	public Client(Shell shell, Config config) {
		init(shell, config);
	}

	/**
	 * Create new Client which creates a new {@link Shell} for its commands
	 */
	public Client() {
		init(new Shell("Client", System.out, System.in), new Config("client"));
	}

	private void init(Shell shell, Config config) {
		getClientData(config);
		this.shell = shell;
		this.clientCli = new ClientCli(this);
		shell.register(clientCli);
		shellThread = new Thread(shell);

		this.clientThread = new SingleServerSocketCommunication(tcpPort,
				proxyHost);
		this.files = new ArrayList<FileInfo>();
		updateFiles();
		
		//RMI
		Config configRMI = new Config("mc");
		String rmiHost = configRMI.getString("proxy.host");
		int rmiPort = configRMI.getInt("proxy.rmi.port");
		String rmiBindingName = configRMI.getString("binding.name");
		try {
			Registry registry = LocateRegistry.getRegistry(rmiHost, rmiPort);
			rmi = (IRMI) registry.lookup(rmiBindingName);
			//UnicastRemoteObject.exportObject(rmi, 0);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	private void getClientData(Config config) {
		try {
			this.tcpPort = config.getInt("proxy.tcp.port");
			this.proxyHost = config.getString("proxy.host");
			this.downloadDir = config.getString("download.dir");
			this.folder = new File(downloadDir);
			this.keyDir = config.getString("keys.dir");
			this.proxyPublicKey = SecurityUtils.readPublicKey(config
					.getString("proxy.key"));
		} catch (NumberFormatException e) {
			System.out
					.println("The configuration file \"client.properties\" is invalid! \n\r");
			e.printStackTrace();
			try {
				clientCli.exit();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Updates the files
	 */
	private void updateFiles() {

		File[] folderfile = folder.listFiles(FileUtils.TEXTFILTER);

		for (File f : folderfile) {
			FileInfo fi = new FileInfo(f.getName(), f.length());
			if (!files.contains(fi)) {
				files.add(fi);
			} else {
				FileInfo updateable = files.get(files.indexOf(fi));
				if (updateable.getFilesize() != fi.getFilesize()) {
					updateable.setFilesize(fi.getFilesize());
					updateable.increaseVersionNumber();
				}
			}

		}
	}

	private FileInfo getFile(String filename) {
		for (FileInfo f : files) {
			if (f.getFilename().equals(filename)) {
				return f;
			}
		}
		return null;

	}

	/**
	 * Starts a new Client
	 * 
	 * @param args
	 *            no args are used
	 */
	public static void main(String[] args) {
		new Client().run();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		shellThread.start();
		try {
			clientThread.holdConnectionOpen();
		} catch (IOException e) {
			try {
				if (shell != null)
					shell.writeLine("The System is offline! Please try again later");
				exit();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}

	/**
	 * Returns the hostname of the {@link Proxy}
	 * 
	 * @return the hostname of the {@link Proxy}
	 */
	public String getProxyHost() {
		return proxyHost;
	}

	/**
	 * Returns the TCP port of the {@link Proxy}
	 * 
	 * @return the TCP port of the {@link Proxy}
	 */
	public int getTcpPort() {
		return tcpPort;
	}

	/**
	 * Send a data Package to the {@link Proxy}
	 * 
	 * @param request
	 *            the {@link Request} to send to the {@link Proxy}
	 * @return the {@link Response} Object
	 */
	public Response send(RequestTO request) {
		return clientThread.send(request);
	}

	/**
	 * Returns the {@link DownloadFileRequest} from the {@link FileServer} and
	 * saves the File containing in the DownloadFileRequest in the directory of
	 * the client
	 * 
	 * @param ticket
	 *            the {@link DownloadTicket} from the {@link Proxy}
	 * @return <li>a {@link DownloadFileResponse} from the {@link FileServer} if
	 *         everything worked out well <li>a {@link MessageResponse} with the
	 *         error Message if something went wrong
	 */
	public Response download(DownloadTicket ticket) {
		SingleServerSocketCommunication clientToFileServer = new SingleServerSocketCommunication(
				ticket.getPort(), proxyHost);
		Response response = clientToFileServer.send(new RequestTO(
				new DownloadFileRequest(ticket), RequestType.File));

		if (response instanceof DownloadFileResponse) {
			DownloadFileResponse download = (DownloadFileResponse) response;
			FileUtils.write(download.getContent(), downloadDir,
					ticket.getFilename());
			updateFiles();
		}
		return response;
	}

	/**
	 * returns the path of the Clients Download directory
	 * 
	 * @return the path of the Clients Download directory
	 */
	public String getPath() {
		return downloadDir;
	}

	/**
	 * @return the file infos
	 */
	public List<FileInfo> getFiles() {
		updateFiles();
		return files;
	}

	/**
	 * Returns the version of the file with the given filename
	 * 
	 * @param filename
	 *            the filename
	 * @return the version of the file with the given filename or -1 if the file
	 *         does not exist
	 */
	public int getVersion(String filename) {
		FileInfo file = getFile(filename);
		if (file == null) {
			return -1;
		}
		return file.getVersion();
	}

	/**
	 * Closes all Threads and shuts down the client
	 */
	public void exit() {
		if (clientThread != null)
			clientThread.close();
		try {
			System.in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (shellThread != null)
			shellThread.interrupt();
		if (shell != null)
			shell.close();
	}

	/**
	 * Returns a Base64 encoded encrypted message that contains:
	 * <ul>
	 * <li>the Username</li>
	 * <li>the base64 encoded Client Challenge</li>
	 * </ul>
	 * 
	 * @param username
	 *            the username of the login requesting user
	 * @return a Base64 encoded encrypted message
	 */
	public byte[] getClientChallenge(String username) {

		// generate a 32 bit client challenge
		SecureRandom secureRandom = new SecureRandom();
		clientChallenge = new byte[32];
		secureRandom.nextBytes(clientChallenge);

		// combine the challenge with the rest
		username = "!login " + username + " ";
		byte[] user = username.getBytes();
		byte[] b64ClientChallenge = Base64.encode(clientChallenge);

		byte[] message = SecurityUtils.combineByteArrays(user,
				b64ClientChallenge);

		// Encrypt the base64 message with the public key from the proxy
		byte[] encryptedMessage = SecurityUtils
				.encrypt(proxyPublicKey, message);

		// encode the final message again with Base64
		return Base64.encode(encryptedMessage);
	}

	/**
	 * Returns the byte array that should get sent to the proxy with the
	 * resolved proxy challenge
	 * 
	 * @param username
	 *            the username of the user
	 * @param password
	 *            the password of the private key of the user
	 * @param encryptedMessage
	 *            the base64 encoded message encrypted with the public key of
	 *            the user from the proxy with the challange
	 * @return the resolved proxy challenge
	 */
	public byte[] solveProxyChallenge(byte[] encryptedMessage, String username,
			String password) {

		// decrypt the encryptedMessage with the private Key of the User
		byte[] b64message = null;
		try {
			b64message = SecurityUtils.decrypt(
					getPrivateKey(username, password),
					Base64.decode(encryptedMessage));
		} catch (LoginException e) {
			System.out.println(e.getMessage());
			return null;
		}

		// get data out of message
		// !ok <client-challenge> <proxy-challenge> <secret-key> <iv-parameter>.
		String[] strs = new String(b64message).split(" ");

		if (strs[1].equals(new String(Base64.encode(clientChallenge)))) {
			secretKey = strs[3].getBytes();
			IV = strs[4].getBytes();
			clientThread.setIV(Base64.decode(IV));
			clientThread.setKey(Base64.decode(secretKey));
			clientThread.activateSecureConnection();
			return strs[2].getBytes();
		}
		return null;

	}

	private PrivateKey getPrivateKey(String username, String password) throws LoginException {
		if (FileUtils.check(keyDir, username + ".pem")) {
			return SecurityUtils.readPrivateKey(keyDir + "\\" + username
					+ ".pem", password);
		}
		return null;
	}

	/**
	 * @param response
	 * @return The Login Response
	 */
	public LoginResponse checkLogin(Response response) {
		if (response instanceof LoginResponse
				&& ((LoginResponse) response).getType() == Type.SUCCESS) {
			return (LoginResponse) response;
		}
		clientThread.deactivateSecureConnection();
		return new LoginResponse(Type.WRONG_CREDENTIALS);
	}

	/**
	 * Checks if the connection to the proxy is valid, if not try to connect
	 * again. If not possible throw an error

	 */
	public void checkConnection() {
		if (!clientThread.isActive()) {
			try {
				clientThread.holdConnectionOpen();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Check if the data is valid
	 * 
	 * @param password the password of the user that tries to logon
	 * @param username the username of the user that tries to logon
	 * @return true if the key is valid
	 */
	public boolean checkKey(String username, String password){
		
		try {
			getPrivateKey(username,password);
		} catch (LoginException e) {
			return false;
		}

		return true;
	}

	/**
	 * Deactivate the secure connection after logout
	 */
	public void logout() {
		clientThread.deactivateSecureConnection();
	}
	
	/**
	 * @return the rmi
	 */
	public IRMI getRmi() {
		return rmi;
	}
	
	/**
	 * Stores the public key of the Proxy.
	 * 
	 * @param publicKey public key of the Proxy
	 */
	public void storeProxyPublicKey(PublicKey publicKey) {
		SecurityUtils.storePublicKey(publicKey, keyDir + "\\.proxy.pub.pem");
	}
	
	/**
	 * Returns the public key of the user.
	 * 
	 * @param username name of the user
	 * @return PublicKey
	 */
	public PublicKey getUserPublicKey(String username) {
		return SecurityUtils.readPublicKey(keyDir+"\\" + username + "pub.pem");
	}
}