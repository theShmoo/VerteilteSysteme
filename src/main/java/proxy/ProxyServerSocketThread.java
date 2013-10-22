package proxy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import message.Response;
import message.request.BuyRequest;
import message.request.DownloadTicketRequest;
import message.request.LoginRequest;
import message.request.UploadRequest;
import message.response.CreditsResponse;
import message.response.LoginResponse;
import message.response.MessageResponse;
import message.response.LoginResponse.Type;
import model.RequestTO;
import model.UserLoginInfo;

public class ProxyServerSocketThread implements Runnable, IProxy {
	private Socket socket = null;
	private Proxy proxy;
	private boolean running;

	// User
	private UserLoginInfo user;

	public ProxyServerSocketThread(Proxy proxy, Socket socket) {
		this.proxy = proxy;
		this.running = true;
		this.socket = socket;
	}

	public void run() {

		try {

			ObjectInputStream inStream = new ObjectInputStream(
					socket.getInputStream());
			ObjectOutputStream outputStream = new ObjectOutputStream(
					socket.getOutputStream());

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
				default:
					// TODO wrong object received
					break;
				}
				outputStream.writeObject(response);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private boolean userCheck() {
		return user != null && user.isOnline();
	}

	@Override
	public LoginResponse login(LoginRequest request) throws IOException {
		for (UserLoginInfo u : proxy.getUserLoginInfos()) {
			if (u.getName().equals(request.getUsername())
					&& u.getPassword().equals(request.getPassword())) {
				this.user = u;
				u.setOnline();
				return new LoginResponse(Type.SUCCESS);
			}
		}
		return new LoginResponse(Type.WRONG_CREDENTIALS);
	}

	@Override
	public Response credits() throws IOException {
		if (userCheck()) {
			return new CreditsResponse(user.getCredits());
		}
		return new MessageResponse("Couldn't receive credits!");
	}

	@Override
	public Response buy(BuyRequest credits) throws IOException {
		// TODO implement buy
		return null;
	}

	@Override
	public Response list() throws IOException {
		// TODO implement list
		return null;
	}

	@Override
	public Response download(DownloadTicketRequest request) throws IOException {
		// TODO implement download
		return null;
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		// TODO implement upload
		return null;
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
