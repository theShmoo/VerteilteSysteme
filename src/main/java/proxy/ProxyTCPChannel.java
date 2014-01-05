package proxy;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Set;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import message.Response;
import message.request.BuyRequest;
import message.request.DownloadTicketRequest;
import message.request.LoginRequest;
import message.request.UploadRequest;
import message.response.BuyResponse;
import message.response.CreditsResponse;
import message.response.DownloadTicketResponse;
import message.response.InfoResponse;
import message.response.ListResponse;
import message.response.LoginResponse;
import message.response.LoginResponse.Type;
import message.response.MessageResponse;
import model.DownloadTicket;
import model.FileServerInfo;
import model.FileServerStatusInfo;
import model.RequestTO;
import model.UserLoginInfo;

import org.bouncycastle.util.encoders.Base64;

import util.ChecksumUtils;
import util.SecurityUtils;
import util.TCPChannel;
import util.UnexpectedCloseException;
import client.Client;

/**
 * A TCP Server Socket Thread that handles Requests from {@link Client}
 */
public class ProxyTCPChannel extends TCPChannel implements IProxy {

	private Proxy proxy;
	private UserLoginInfo user;
	
	//Security
	private int LOGINSTATUS;	
	private byte[] proxyChallenge = new byte[32];
	private String username;

	/**
	 * Initialize a new ProxyServerSocketThread that handles Requests from
	 * {@link Client}
	 * 
	 * @param proxy
	 *            the proxy
	 * @param socket
	 *            the socket
	 */
	public ProxyTCPChannel(Proxy proxy, Socket socket) {
		super(socket);
		this.proxy = proxy;
		LOGINSTATUS = 0;
	}

	@Override
	public void run() {

		try {
			// start listening
			while (running) {
				Object input = null;
				RequestTO request = null;
				Response response = null;
				
				try{
					input = receive();
				} catch (UnexpectedCloseException e){
					LOGINSTATUS = 0;
					deactivateSecureConnection();
					user = null;
					response = new MessageResponse("ERROR!");
				}

				if (!(input instanceof RequestTO)) {
					// major error
				} else {
					request = (RequestTO) input;

					switch (request.getType()) {
					case Login:
						LoginRequest loginRequest = (LoginRequest) request
								.getRequest();
						response = login(loginRequest);
						break;
					case Logout:
						response = logout();
						break;
					case Credits:
						response = credits();
						break;
					case Buy:
						response = buy((BuyRequest) request.getRequest());
						break;
					case Ticket:
						response = download((DownloadTicketRequest) request
								.getRequest());
						break;
					case List:
						response = list();
						break;
					case Upload:
						response = upload((UploadRequest) request.getRequest());
						break;
					default:
						// Received a Request that is not suitable for a Proxy
						response = new MessageResponse("ERROR!");
						break;
					}
				}
				
				send(response);
				
				if(LOGINSTATUS == 1){
					activateSecureConnection();
				}
				if(LOGINSTATUS == 3){
					deactivateSecureConnection();
					LOGINSTATUS = 0;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}

	private boolean userCheck() {
		return user != null && user.isOnline();
	}

	@Override
	public LoginResponse login(LoginRequest request) throws IOException {
		if (!userCheck()) {
			switch (LOGINSTATUS) {
			// Client Challenge
			case 0:
				byte[] encryptedMessage = Base64.decode(request.getMessage());

				// Decrypt the message with the private key from the proxy
				byte[] b64message = SecurityUtils.decrypt(
						proxy.getPrivateKey(), encryptedMessage);

				if (b64message != null) {
					String s = new String(b64message);
					String[] strs = s.split(" ");
					username = strs[1];
					byte[] clientChallenge = strs[2].getBytes();

					// get public key from user
					PublicKey userPublicKey = proxy.getUserPublicKey(username);
					if (userPublicKey == null) {
						return new LoginResponse(Type.WRONG_CREDENTIALS);
					}
					// generate a 32 bit proxy challenge
					SecureRandom secureRandom = new SecureRandom();
					secureRandom.nextBytes(proxyChallenge);
					final byte[] b64ProxyChallenge = Base64
							.encode(proxyChallenge);

					// generate 256 Bit AES secret Key
					byte[] b64key256 = null;
					try {
						KeyGenerator generator = KeyGenerator
								.getInstance("AES");
						generator.init(256);
						SecretKey key = generator.generateKey();
						byte[] bKey = key.getEncoded();
						b64key256 = Base64.encode(bKey);
						setKey(bKey);
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}

					// generate 16 Byte initialization vector (IV)
					final byte[] IV = new byte[16];
					secureRandom.nextBytes(IV);
					final byte[] b64IV = Base64.encode(IV);
					setIV(IV);
					// combine to one message:
					String ok = "!ok";
					String separator = " ";
					byte[] sep = separator.getBytes();
					byte[] message = SecurityUtils.combineByteArrays(
							ok.getBytes(), sep, clientChallenge, sep,
							b64ProxyChallenge, sep, b64key256, sep, b64IV);

					byte[] encryptedRetourMessage = SecurityUtils.encrypt(
							userPublicKey, message);
					LOGINSTATUS = 1;
					return new LoginResponse(Base64.encode(encryptedRetourMessage));
				}
				break;
			case 1:
				byte[] data = Base64.decode(request.getMessage());
				if (Arrays.equals(data,proxyChallenge)) {
					for (UserLoginInfo u : proxy.getUserLoginInfos()) {
						if (u.getName().equals(username)) {
							this.user = u;
							u.setOnline();
							LOGINSTATUS = 2;
							return new LoginResponse(Type.SUCCESS);
						}
					}
				}
				break;
			}
		}
		LOGINSTATUS = 0;
		return new LoginResponse(Type.WRONG_CREDENTIALS);
	}

	@Override
	public Response credits() throws IOException {
		if (userCheck()) {
			return new CreditsResponse(user.getCredits());
		}
		return new MessageResponse("No user is authenticated!");
	}

	@Override
	public Response buy(BuyRequest credits) throws IOException {
		if (userCheck()) {
			user.addCredits(credits.getCredits());
			return new BuyResponse(user.getCredits());
		}
		return new MessageResponse("No user is authenticated!");
	}

	@Override
	public Response list() throws IOException {
		if (userCheck()) {
			Set<String> set = proxy.getFiles();
			if (set == null) {
				return new MessageResponse(
						"Sorry there is currently no fileserver available! Please try again later...");
			}
			return new ListResponse(set);
		}
		return new MessageResponse("No user is authenticated!");
	}

	@Override
	public Response download(DownloadTicketRequest request) throws IOException {
		if (userCheck()) {
			String filename = request.getFilename();
			ArrayList<List<FileServerStatusInfo>> list = proxy.getGiffordsLists();
			List<FileServerStatusInfo> fnr = list.get(0);
			
			FileServerInfo server = null;
			// case 1: there is no server
			if (fnr.size() == 0) {
				return new MessageResponse(
						"We are sorry! There is currently no online file server! Try again later!");
			} 
			server = fnr.get(0).getModel();
			
			long size = 0l;
			InfoResponse infoResponse = null;

			Response info = proxy.getFileInfo(server, filename);
			// case 2: server exists and returns a valid info of the file
			if (info instanceof InfoResponse) {
				infoResponse = (InfoResponse) info;
				size = infoResponse.getSize();
				// case 3: user has enough Credits! everything is fine!
				if (user.hasEnoughCredits(size)) {
					user.removeCredits(size);
					// case 4: the user does not have enough credits
				} else {
					return new MessageResponse(
							"Sry! You have too less credits!\nYou have "
									+ user.getCredits()
									+ " credits and you need " + size
									+ " credits! To buy credits type: \"!buy "
									+ (size - user.getCredits()) + "\"");
				}
				// case 5: The server exists but does not return a valid info
				// (maybe file does not exist)
			} else {
				return info;
			}

			//check, which server from the nrs has the highest version of the file
			int version = proxy.getVersion(server, filename);
			long usage = server.getUsage();
			for (int i = 1; i < fnr.size(); i++) {
				if (version < proxy.getVersion(fnr.get(i).getModel(), filename)) {
					version = proxy.getVersion(fnr.get(i).getModel(), filename);
					server = fnr.get(i).getModel();
					usage = server.getUsage();
				} else if (version == proxy.getVersion(fnr.get(i).getModel(), filename) && usage > fnr.get(i).getUsage()) {
					server = fnr.get(i).getModel();
					usage = server.getUsage();
				}
			}
			
			String checksum = ChecksumUtils.generateChecksum(user.getName(),
					filename, version, size);
			DownloadTicket ticket = new DownloadTicket(user.getName(),
					filename, checksum, server.getAddress(), server.getPort());
			DownloadTicketResponse respond = new DownloadTicketResponse(ticket);
			// everything worked well the user gets his ticket so we can rank
			// the fileserver as working
			proxy.addServerUsage(server, size);
			
			//add file to the download-list
			proxy.increaseDownloadNumber(filename);
			return respond;
		}
		return new MessageResponse("No user is authenticated!");
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		if (userCheck()) {
			proxy.uploadFile(request);
			user.addCredits(request);
			return new MessageResponse(
					"File successfully uploaded.\n\rYou now have "
							+ user.getCredits() + " credits.");
		}
		return new MessageResponse("No user is authenticated!");
	}

	@Override
	public MessageResponse logout() throws IOException {
		if (userCheck()) {
			user.setOffline();
			LOGINSTATUS = 3;
			return new MessageResponse("User \"" + user.getName()
					+ "\" successfully logged out.");
		}
		return new MessageResponse(
				"Logout failed! The user was already offline.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see util.SocketThread#close()
	 */
	@Override
	public void close() {
		super.close();
		if (user != null) {
			user.setOffline();
		}
	}
}
