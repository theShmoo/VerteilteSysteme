package proxy;

import java.io.IOException;
import java.util.ArrayList;

import message.Response;
import message.response.FileServerInfoResponse;
import message.response.MessageResponse;
import message.response.UserInfoResponse;
import model.FileServerInfo;
import cli.Command;

/**
 * Implementation of the {@link IProxyCli}
 * 
 * @author David
 */
public class ProxyCli implements IProxyCli {

	private Proxy proxy;

	/**
	 * Initialize a new Proxy Command Line Interface
	 * @param proxy the proxy
	 */
	public ProxyCli(Proxy proxy) {
		this.proxy = proxy;
	}

	@Override
	@Command
	public Response fileservers() throws IOException {
		return new FileServerInfoResponse(new ArrayList<FileServerInfo>(
				proxy.getFileServerInfos()));
	}

	@Override
	@Command
	public Response users() throws IOException {
		return new UserInfoResponse(proxy.getUserInfos());
	}

	@Override
	@Command
	public MessageResponse exit() throws IOException {
		proxy.close();
		return new MessageResponse("Shutting down proxy now");
	}

}
