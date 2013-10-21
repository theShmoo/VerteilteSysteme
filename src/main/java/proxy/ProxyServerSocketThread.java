package proxy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import message.request.LoginRequest;
import message.response.LoginResponse;

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

				Object o = inStream.readObject();
				Class<? extends Object> c = o.getClass();
				System.out.println(c.getCanonicalName());

				LoginRequest data = (LoginRequest) o;
				System.out.println("Object received = " + data);
				outputStream.writeObject(proxy.login(data));
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
