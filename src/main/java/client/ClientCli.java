package client;

import java.io.IOException;

import message.Response;
import message.response.LoginResponse;
import message.response.MessageResponse;

/**
 * Implementation of the {@link IClientCli}
 * @history 11.10.2013
 * @version 11.10.2013 version 0.1
 * @author David
 */
public class ClientCli implements IClientCli {

	/* (non-Javadoc)
	 * @see client.IClientCli#login(java.lang.String, java.lang.String)
	 */
	@Override
	public LoginResponse login(String username, String password)
			throws IOException {
		// TODO implement Login
		return null;
	}

	/* (non-Javadoc)
	 * @see client.IClientCli#credits()
	 */
	@Override
	public Response credits() throws IOException {
		// TODO implement !credits command
		return null;
	}

	/* (non-Javadoc)
	 * @see client.IClientCli#buy(long)
	 */
	@Override
	public Response buy(long credits) throws IOException {
		// TODO implement !buy command
		return null;
	}

	/* (non-Javadoc)
	 * @see client.IClientCli#list()
	 */
	@Override
	public Response list() throws IOException {
		// TODO implement !list command
		return null;
	}

	/* (non-Javadoc)
	 * @see client.IClientCli#download(java.lang.String)
	 */
	@Override
	public Response download(String filename) throws IOException {
		// TODO implement !download command
		return null;
	}

	/* (non-Javadoc)
	 * @see client.IClientCli#upload(java.lang.String)
	 */
	@Override
	public MessageResponse upload(String filename) throws IOException {
		// TODO implement !upload command
		return null;
	}

	/* (non-Javadoc)
	 * @see client.IClientCli#logout()
	 */
	@Override
	public MessageResponse logout() throws IOException {
		// TODO implement !logout command
		return null;
	}

	/* (non-Javadoc)
	 * @see client.IClientCli#exit()
	 */
	@Override
	public MessageResponse exit() throws IOException {
		// TODO implement !exit command
		return null;
	}

}
