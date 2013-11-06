package proxy;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;

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
import util.SocketThread;
import client.Client;

/**
 * A TCP Server Socket Thread that handles Requests from {@link Client}
 * 
 * @author David
 */
public class ProxyServerSocketThread extends SocketThread implements IProxy {
	private Proxy proxy;

	// User
	private UserLoginInfo user;

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

			while (running) {
				Object input = receive();

				RequestTO request = null;
				Response response = null;

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
					case File:
						// TODO File (what the hell is that shit)
						break;
					case Upload:
						response = upload((UploadRequest) request.getRequest());
						break;
					default:
						response = new MessageResponse("ERROR!");
						break;
					}
				}
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
			FileServerInfo server = proxy.getFileserver();
			if (server == null) {
				return new MessageResponse(
						"We are sorry! There is currently no online file server! Try again later!");
			}
			Response info = proxy.getFileInfo(server, request.getFilename());
			if (info instanceof InfoResponse) {
				user.removeCredits(((InfoResponse) info).getSize());
			} else {
				return info;
			}

			DownloadTicket ticket = new DownloadTicket(user.getName(),
					request.getFilename(), "checksum", server.getAddress(),
					server.getPort());
			DownloadTicketResponse respond = new DownloadTicketResponse(ticket);
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
					+ "\" logged out!");
		}
		return new MessageResponse(
				"Logout failed! The user was already offline");
	}
}
