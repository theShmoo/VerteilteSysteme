package proxy;

import java.io.IOException;

import message.Response;
import message.response.FileServerInfoResponse;
import message.response.MessageResponse;
import cli.Command;

/**
 * Implementation of the {@link IProxyCli}
 * @history 11.10.2013
 * @version 11.10.2013 version 0.1
 * @author David
 */
public class ProxyCli implements IProxyCli {
	
	private Proxy proxy;
	
	public ProxyCli(Proxy proxy) {
		this.proxy = proxy;
	}

	@Override
	@Command
	public
	Response fileservers() throws IOException {
		return new FileServerInfoResponse(proxy.getFileServerInfos());
	}

	@Override
	@Command
	public
	Response users() throws IOException {
		// TODO implement !users command
		return null;
	}

	@Override
	@Command
	public
	MessageResponse exit() throws IOException {
		// TODO implement !exit command
		return null;
	}

	

}
