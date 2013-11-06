package client;

import java.io.IOException;

import message.Response;
import message.request.BuyRequest;
import message.request.CreditsRequest;
import message.request.DownloadTicketRequest;
import message.request.ListRequest;
import message.request.LoginRequest;
import message.request.LogoutRequest;
import message.request.UploadRequest;
import message.response.DownloadTicketResponse;
import message.response.LoginResponse;
import message.response.MessageResponse;
import model.DownloadTicket;
import model.RequestTO;
import model.RequestType;
import util.FileUtils;
import cli.Command;

/**
 * Implementation of the {@link IClientCli}
 * 
 * @history 11.10.2013
 * @version 11.10.2013 version 0.1
 * @author David
 */
public class ClientCli implements IClientCli {

	private Client client = null;
	private boolean login;

	/**
	 * Create a new Client Command Line Interface
	 * 
	 * @param client
	 *            the client from the CLI
	 */
	public ClientCli(Client client) {
		this.client = client;
		this.login = false;
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
		login = true;
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
		ListRequest data = new ListRequest();
		RequestTO request = new RequestTO(data, RequestType.List);
		return client.send(request);
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
		RequestTO request = new RequestTO(data, RequestType.Ticket);
		Response response = client.send(request);
		if (response instanceof DownloadTicketResponse) {
			DownloadTicket ticket = ((DownloadTicketResponse) response)
					.getTicket();
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
		byte[] content = FileUtils.read(client.getPath(),filename);
		if (content == null){
			return new MessageResponse("The file \""+filename+"\" does not exist");
		}
		UploadRequest data = new UploadRequest(filename,client.getVersion(filename),content);
		RequestTO request = new RequestTO(data, RequestType.Upload);
		return (MessageResponse) client.send(request);
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

		MessageResponse response = (MessageResponse) client.send(request);
		login = false;
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.IClientCli#exit()
	 */
	@Override
	@Command
	public MessageResponse exit() throws IOException {
		StringBuilder exitMessage = new StringBuilder();
		if (login) {
			exitMessage.append(logout().getMessage()).append("\n");
			login = false;
		}
		client.exit();
		exitMessage.append("Shutting down client now");
		return new MessageResponse(exitMessage.toString());
	}

	/**
	 * Returns <code>true</code> if the user is logged in
	 * 
	 * @return <code>true</code> if the user is logged in
	 */
	public boolean getLoggedIn() {
		return login;
	}

}
