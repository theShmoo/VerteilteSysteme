package proxy;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import model.RequestTO;
import model.UserLoginInfo;
import util.ChecksumUtils;
import util.SocketThread;
import client.Client;

/**
 * A TCP Server Socket Thread that handles Requests from {@link Client}
 * 
 * @author David
 */
public class ProxyServerSocketThread extends SocketThread implements IProxy {

	private Proxy proxy;
	private UserLoginInfo user;
	private static final Logger LOG = LoggerFactory
			.getLogger(ProxyServerSocketThread.class);

	/**
	 * Initialize a new ProxyServerSocketThread that handles Requests from
	 * {@link Client}
	 * 
	 * @param proxy
	 *            the proxy
	 * @param socket
	 *            the socket
	 */
	public ProxyServerSocketThread(Proxy proxy, Socket socket) {
		super(socket);
		this.proxy = proxy;
	}

	@Override
	public void run() {

		try {
			LOG.info("start listening!");
			while (running) {
				Object input = receive();

				RequestTO request = null;
				Response response = null;

				if (!(input instanceof RequestTO)) {
					// major error
					LOG.error("The input is not a RequestTO!");
				} else {
					request = (RequestTO) input;

					switch (request.getType()) {
					case Login:
						LOG.info("Received login Request");
						LoginRequest loginRequest = (LoginRequest) request
								.getRequest();
						response = login(loginRequest);
						break;
					case Logout:
						LOG.info("Received logout Request");
						response = logout();
						break;
					case Credits:
						LOG.info("Received Credits Request");
						response = credits();
						break;
					case Buy:
						LOG.info("Received Buy Request");
						response = buy((BuyRequest) request.getRequest());
						break;
					case Ticket:
						LOG.info("Received Ticket Request");
						response = download((DownloadTicketRequest) request
								.getRequest());
						break;
					case List:
						LOG.info("Received List Request");
						response = list();
						break;
					case Upload:
						LOG.info("Received Upload Request");
						response = upload((UploadRequest) request.getRequest());
						break;
					default:
						LOG.error("Received a Request that is not suitable for a Proxy");
						response = new MessageResponse("ERROR!");
						break;
					}
				}
				LOG.info("Send Response");
				send(response);
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
			for (UserLoginInfo u : proxy.getUserLoginInfos()) {
				if (u.getName().equals(request.getUsername())
						&& u.getPassword().equals(request.getPassword())) {
					this.user = u;
					u.setOnline();
					return new LoginResponse(Type.SUCCESS);
				}
			}
		}
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
			return new ListResponse(set);
		}
		return new MessageResponse("No user is authenticated!");
	}

	@Override
	public Response download(DownloadTicketRequest request) throws IOException {
		if (userCheck()) {
			String filename = request.getFilename();
			FileServerInfo server = proxy.getFileserver();
			long size = 0l;
			InfoResponse infoResponse = null;

			// case 1: there is no server
			if (server == null) {
				return new MessageResponse(
						"We are sorry! There is currently no online file server! Try again later!");
			}

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

			String checksum = ChecksumUtils.generateChecksum(user.getName(),
					filename, proxy.getVersion(server, filename), size);
			DownloadTicket ticket = new DownloadTicket(user.getName(),
					filename, checksum, server.getAddress(), server.getPort());
			DownloadTicketResponse respond = new DownloadTicketResponse(ticket);
			// everything worked well the user gets his ticket so we can rank
			// the fileserver as working
			proxy.addServerUsage(server, size);
			return respond;
		}
		return new MessageResponse("No user is authenticated!");
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		if (userCheck()) {
			proxy.distributeFile(request);
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
			return new MessageResponse("User \"" + user.getName()
					+ "\" successfully logged out.");
		}
		return new MessageResponse(
				"Logout failed! The user was already offline.");
	}
}
