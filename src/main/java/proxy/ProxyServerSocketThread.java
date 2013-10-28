package proxy;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import message.response.ListResponse;
import message.response.LoginResponse;
import message.response.LoginResponse.Type;
import message.response.MessageResponse;
import model.DownloadTicket;
import model.FileServerInfo;
import model.RequestTO;
import model.UserLoginInfo;
import client.Client;

/**
 * A TCP Server Socket Thread that handles Requests from {@link Client}
 * 
 * @author David
 */
public class ProxyServerSocketThread implements Runnable, IProxy {
	private Socket socket = null;
	private ObjectInputStream inStream = null;
	private ObjectOutputStream outputStream = null;
	private Proxy proxy;
	private boolean running;

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
		this.proxy = proxy;
		this.running = true;
		this.socket = socket;
	}

	public void run() {

		try {

			this.inStream = new ObjectInputStream(socket.getInputStream());
			this.outputStream = new ObjectOutputStream(socket.getOutputStream());

			while (running) {
				RequestTO request = (RequestTO) inStream.readObject();

				Response response = null;

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
					upload((UploadRequest) request.getRequest());
					break;
				default:
					// TODO wrong object received
					break;
				}
				outputStream.writeObject(response);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (EOFException e) {
			running = false;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			try {
				socket.close();
				inStream.close();
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		return new MessageResponse("Logout failed!");
	}
}
