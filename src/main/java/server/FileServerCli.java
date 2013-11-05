package server;

import java.io.IOException;

import message.response.MessageResponse;
import cli.Command;

/**
 * Implements the {@link IFileServerCli}
 * 
 * @author David
 */
public class FileServerCli implements IFileServerCli {

	private FileServer server;

	/**
	 * Initialize a new FileServerCli
	 * @param fileServer
	 *            the fileServer underlying the CLI
	 */
	public FileServerCli(FileServer fileServer) {
		this.server = fileServer;
	}

	@Override
	@Command
	public MessageResponse exit() throws IOException {
		server.close();
		return new MessageResponse("Shutting down file server now");
	}

}
