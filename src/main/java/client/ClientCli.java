package client;

import java.io.IOException;

import message.Response;
import message.request.LoginRequest;
import message.request.LogoutRequest;
import message.response.LoginResponse;
import message.response.LoginResponse.Type;
import message.response.MessageResponse;
import model.RequestTO;
import model.RequestType;
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

	/**
	 * Create a new Client Command Line Interface
	 * 
	 * @param client
	 *            the client from the CLI
	 */
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
	public LoginResponse login(String username, String password) {

		if (!client.isLogin()) {
			LoginRequest data = new LoginRequest(username, password);
			RequestTO request = new RequestTO(data, RequestType.Login);
			LoginResponse respond = (LoginResponse) client.send(request);
			client.setLogin(respond.getType() == Type.SUCCESS);
			return respond;
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
	@Command
	public MessageResponse logout() throws IOException {
		MessageResponse response = null;
		if (client.isLogin()) {
			RequestTO request = new RequestTO(new LogoutRequest(),
					RequestType.Logout);
			response = (MessageResponse) client.send(request);
			client.setLogin(false);
		}
		return response;
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
