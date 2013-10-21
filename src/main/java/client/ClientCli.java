package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import message.Response;
import message.request.LoginRequest;
import message.response.LoginResponse;
import message.response.LoginResponse.Type;
import message.response.MessageResponse;
import cli.Command;

/**
 * Implementation of the {@link IClientCli}
 * 
 * @history 11.10.2013
 * @version 11.10.2013 version 0.1
 * @author David
 */
public class ClientCli implements IClientCli {

	Client client = null;

	public ClientCli(Client client) {
		this.client = client;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.IClientCli#login(java.lang.String, java.lang.String)
	 */
	@Override
	@Command
	public LoginResponse login(String username, String password)
			throws IOException {

		String host = client.getProxyHost();
		int port = client.getTcpPort();

		if (!client.isLogin()) {
			try (Socket socket = new Socket(host, port);
					ObjectOutputStream outputStream = new ObjectOutputStream(
							socket.getOutputStream());
					ObjectInputStream inStream = new ObjectInputStream(
							socket.getInputStream());) {

				LoginRequest data = new LoginRequest(username, password);
				outputStream.writeObject(data);
				System.out.println("Object sent = " + data);
				LoginResponse respond = (LoginResponse) inStream.readObject();
				System.out.println("Object received = " + respond);
				socket.close();
				return respond;
			} catch (UnknownHostException e) {
				System.err.println("Don't know about host " + host);
				System.exit(1);
			} catch (IOException e) {
				System.err.println("Couldn't get I/O for the connection to "
						+ host);
				e.printStackTrace();
				System.exit(1);
			} catch (ClassNotFoundException e) {
				System.err.println("The received object is unknown to " + host);
				e.printStackTrace();
				System.exit(1);
			}

		}

		return new LoginResponse(Type.WRONG_CREDENTIALS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.IClientCli#credits()
	 */
	@Override
	public Response credits() throws IOException {
		if (client.isLogin()) {

		}
		// TODO implement !credits command
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.IClientCli#buy(long)
	 */
	@Override
	public Response buy(long credits) throws IOException {
		// TODO implement !buy command
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.IClientCli#list()
	 */
	@Override
	public Response list() throws IOException {
		// TODO implement !list command
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.IClientCli#download(java.lang.String)
	 */
	@Override
	public Response download(String filename) throws IOException {
		// TODO implement !download command
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.IClientCli#upload(java.lang.String) )
	 */
	@Override
	public MessageResponse upload(String filename) throws IOException {
		// TODO implement !upload command
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.IClientCli#logout()
	 */
	@Override
	public MessageResponse logout() throws IOException {
		// TODO implement !logout command
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.IClientCli#exit()
	 */
	@Override
	public MessageResponse exit() throws IOException {
		// TODO implement !exit command
		return null;
	}

}
