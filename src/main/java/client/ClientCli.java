package client;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.List;

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
import message.response.LoginResponse.Type;
import message.response.MessageResponse;
import model.DownloadTicket;
import model.FileInfo;
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
		synchronized (this) {
			if (login || !client.checkKey(username, password)) {
				System.out.println("The private key could not get read");
				return new LoginResponse(Type.WRONG_CREDENTIALS);
			}

			byte[] ciphertext = client.getClientChallenge(username);
			System.out.println("Client Challenge: "+ciphertext);

			if (ciphertext != null) {
				LoginRequest data = new LoginRequest(ciphertext);
				RequestTO request = new RequestTO(data, RequestType.Login);
				Response response = client.send(request);

				if (response instanceof LoginResponse) {
					LoginResponse r = (LoginResponse) response;
					System.out.println("LoginResponse: "+r.toString());
					if(r.getType() != Type.WRONG_CREDENTIALS){
						ciphertext = client.solveProxyChallenge(
								r.getProxyChallenge(), username, password);
						if (ciphertext != null) {
							data = new LoginRequest(ciphertext);
							request = new RequestTO(data, RequestType.Login);
							response = client.send(request);
							LoginResponse lresp = client.checkLogin(response);
							System.out.println("LoginResponse: "+lresp.toString());
							login = lresp.getType() == Type.SUCCESS;
							return lresp;
						}
					}
				}
			}
			return new LoginResponse(Type.WRONG_CREDENTIALS); // XXX change!
		}
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

	/**
	 * Returns a response containing all informations about the containing files
	 * of the Client
	 * 
	 * @return the infos of the clients files
	 * @throws IOException
	 */
	@Command
	public Response ls() throws IOException {
		List<FileInfo> files = client.getFiles();
		StringBuilder response = new StringBuilder("name\tsize\tversion\n");
		for (FileInfo f : files) {
			response.append(f.toString() + "\n");
		}
		return new MessageResponse(response.toString());
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
		byte[] content = FileUtils.read(client.getPath(), filename);
		if (content == null) {
			return new MessageResponse("The file \"" + filename
					+ "\" does not exist");
		}
		UploadRequest data = new UploadRequest(filename,
				client.getVersion(filename), content);
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
		client.logout();
		return response;
	}

	/**
	 * Returns the number of Read-Quorums that are currently used for the
	 * replication mechanism.
	 * 
	 * @return Response with the number of Read-Quorums
	 * @throws RemoteException
	 */
	@Command
	public Response readQuorum() throws RemoteException {
		return client.getRmi().readQuorum();
	}

	/**
	 * Returns the number of Write-Quorums that are currently used for the
	 * replication mechanism.
	 * 
	 * @return Response with the number of Write-Quorums
	 * @throws RemoteException
	 */
	@Command
	public Response writeQuorum() throws RemoteException {
		return client.getRmi().writeQuorum();
	}

	/**
	 * Returns a sorted list that contains the 3 files that got downloaded the
	 * most. Where the first file in the list, represents the file that got
	 * downloaded the most.
	 * 
	 * @return A sorted list of Strings
	 * @throws RemoteException
	 */
	@Command
	public Response topThreeDownloads() throws RemoteException {
		return client.getRmi().topThreeDownloads();
	}

	/**
	 * Subscribes for the given file.
	 * 
	 * @param filename
	 *            name of the file
	 * @param number
	 *            number of times, how often the file should be downloaded
	 * @return Response that the file was downloaded x times
	 * @throws RemoteException
	 */
	@Command
	public Response subscribe(String filename, int number)
			throws RemoteException {
		// TODO add callback object, so if logout or exit, that the method stops
		return client.getRmi().subscribe(filename, number);
	}

	/**
	 * Returns the Proxy's public key.
	 * 
	 * @return Response with the Proxy's public key
	 * @throws RemoteException
	 */
	@Command
	public Response getProxyPublicKey() throws RemoteException {
		PublicKey key = client.getRmi().getProxyPublicKey();
		if (key != null) {
			client.storeProxyPublicKey(key);
			return new MessageResponse(
					"Successfully received public key of Proxy.");
		}
		return new MessageResponse("Receiving public key of Proxy failed.");
	}

	/**
	 * Exchanges the user's public key with the Proxy.
	 * 
	 * @param username
	 *            the name of the user
	 * @return Response
	 * @throws RemoteException
	 */
	@Command
	public Response setUserPublicKey(String username) throws RemoteException {
		PublicKey publicKey = client.getUserPublicKey(username);
		if (publicKey != null) {
			return client.getRmi().setUserPublicKey(username, publicKey);
		}
		return new MessageResponse("Exchanging the user's public key failed.");
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

}
