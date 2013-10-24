package client;

import java.io.IOException;

import message.Response;
import message.request.BuyRequest;
import message.request.CreditsRequest;
import message.request.DownloadTicketRequest;
import message.request.LoginRequest;
import message.request.LogoutRequest;
import message.response.DownloadTicketResponse;
import message.response.LoginResponse;
import message.response.MessageResponse;
import model.DownloadTicket;
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

			LoginRequest data = new LoginRequest(username, password);
			RequestTO request = new RequestTO(data, RequestType.Login);
			LoginResponse respond = (LoginResponse) client.send(request);
			return respond;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.IClientCli#credits()
	 */
	@Override
	@Command
	public Response credits() throws IOException {
		CreditsRequest data = new CreditsRequest();
		RequestTO request = new RequestTO(data, RequestType.Credits);
		return client.send(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.IClientCli#buy(long)
	 */
	@Override
	@Command
	public Response buy(long credits) throws IOException {
		BuyRequest data = new BuyRequest(credits);
		RequestTO request = new RequestTO(data, RequestType.Buy);
		return client.send(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.IClientCli#list()
	 */
	@Override
	@Command
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
	@Command
	public Response download(String filename) throws IOException {
		DownloadTicketRequest data = new DownloadTicketRequest(filename);
		RequestTO request = new RequestTO(data,RequestType.Ticket);
		Response response = client.send(request);
		if(response instanceof DownloadTicketResponse){
			DownloadTicket ticket = ((DownloadTicketResponse) response).getTicket();
			response = client.download(ticket);
		} 
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.IClientCli#upload(java.lang.String) )
	 */
	@Override
	@Command
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
		RequestTO request = new RequestTO(new LogoutRequest(),
				RequestType.Logout);
		return (MessageResponse) client.send(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.IClientCli#exit()
	 */
	@Override
	@Command
	public MessageResponse exit() throws IOException {
		// TODO implement !exit command
		return null;
	}

}
