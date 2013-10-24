package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import message.Response;
import message.request.BuyRequest;
import message.request.DownloadFileRequest;
import message.request.DownloadTicketRequest;
import message.request.InfoRequest;
import message.request.LoginRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.MessageResponse;
import model.RequestTO;
import proxy.Proxy;
import client.Client;

/**
 * 
 * This Thread handles incoming requests from {@link Client} and {@link Proxy}
 * 
 * @author David
 */
public class FileServerSocketThread implements Runnable, IFileServer {

	private boolean running;
	private Socket socket;
	private FileServer server;

	/**
	 * Initialize a new FileServerThread
	 * 
	 * @param fileserver
	 *            the FileServer of the Thread
	 * @param socket
	 *            the Socket
	 */
	public FileServerSocketThread(FileServer fileserver, Socket socket) {
		this.server = fileserver;
		this.running = true;
		this.socket = socket;
	}

	@Override
	public void run() {
		try {

			ObjectInputStream inStream = new ObjectInputStream(
					socket.getInputStream());
			ObjectOutputStream outputStream = new ObjectOutputStream(
					socket.getOutputStream());

			while (running) {
				RequestTO request = (RequestTO) inStream.readObject();

				Response response = null;

				switch (request.getType()) {

				default:
					response = new MessageResponse("PARTY!");
					break;
				}
				outputStream.writeObject(response);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1); // TODO clean shutdown of thread
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public Response list() throws IOException {
		// TODO implement list
		return null;
	}

	@Override
	public Response download(DownloadFileRequest request) throws IOException {
		// TODO implement download
		return null;
	}

	@Override
	public Response info(InfoRequest request) throws IOException {
		// TODO implement info
		return null;
	}

	@Override
	public Response version(VersionRequest request) throws IOException {
		// TODO implement version
		return null;
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		// TODO implement upload
		return null;
	}

}
