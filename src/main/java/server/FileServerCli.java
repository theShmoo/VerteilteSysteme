package server;

import java.io.IOException;

import message.response.MessageResponse;
import cli.Command;

/**
 * Implements the {@link IFileServerCli}
 * @history 11.10.2013
 * @version 11.10.2013 version X.X
 * @author David
 */
public class FileServerCli implements IFileServerCli {

	@Override
	@Command
	public
	MessageResponse exit() throws IOException {
		// TODO implement !exit command
		return null;
	}

}
