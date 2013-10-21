package proxy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import message.Response;
import message.request.LoginRequest;
import message.response.CreditsResponse;
import message.response.LoginResponse;
import message.response.MessageResponse;
import message.response.LoginResponse.Type;
import model.RequestTO;
import model.UserInfo;
import model.UserLoginInfo;

public class ProxyServerSocketThread implements Runnable {
	private Socket socket = null;
	private Proxy proxy;
	private boolean running;
	
	//User
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
					LoginRequest loginRequest = (LoginRequest) request.getRequest();
					response = proxy.login(loginRequest);
					for (UserLoginInfo u : proxy.getUserLoginInfos()) {
						if (u.getName().equals(loginRequest.getUsername())) {
							user = u;
						}
					}
					break;
				case Logout:
					if(userCheck()){
						response = proxy.logout();
						user.setOffline();
					} else{
						response = new MessageResponse("Already Logged out!");
					}
					break;
				case Credits:
					if(userCheck()){
						response = proxy.credits();
					}
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
}
