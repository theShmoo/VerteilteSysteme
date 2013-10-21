package proxy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import message.Response;
import message.request.LoginRequest;
import message.request.LogoutRequest;
import message.response.LoginResponse;
import model.RequestTO;

public class ProxyServerSocketThread implements Runnable {
	private Socket socket = null;
	private int tcpPort;
	private Proxy proxy;
	private boolean running;

	public ProxyServerSocketThread(int tcpPort, Proxy proxy) {
		this.tcpPort = tcpPort;
		this.proxy = proxy;
		this.running = true;
	}

	public void run() {

		try (ServerSocket serverSocket = new ServerSocket(tcpPort)) {
			while (running) {
				socket = serverSocket.accept();
				ObjectInputStream inStream = new ObjectInputStream(
						socket.getInputStream());
				ObjectOutputStream outputStream = new ObjectOutputStream(
						socket.getOutputStream());

				RequestTO request = (RequestTO) inStream.readObject();

				Response response = null;
				
				switch (request.getType()) {
				case Login:
					response = proxy.login((LoginRequest) request
							.getRequest());
					break;
				case Logout:
					response = proxy.logout();
					break;
				default:
					//TODO wrong object received
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
}
