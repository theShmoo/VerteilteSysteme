package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedHashSet;

import message.Response;
import message.request.DownloadFileRequest;
import message.request.InfoRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.DownloadFileResponse;
import message.response.ListResponse;
import message.response.MessageResponse;
import message.response.VersionResponse;
import model.DownloadTicket;
import model.FileInfo;
import model.RequestTO;
import proxy.Proxy;
import util.FileUtils;
import client.Client;

/**
 * 
 * This Thread handles incoming requests from {@link Client} and {@link Proxy}
 * 
 * @author David
 */
public class FileServerSocketThread implements Runnable, IFileServer {
	private Socket socket;
	private ObjectInputStream inStream = null;
	private ObjectOutputStream outputStream = null;
	private FileServer server;

	private boolean running;

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
		this.socket = socket;
		this.running = true;
	}

	@Override
	public void run() {
		try {

			this.inStream = new ObjectInputStream(socket.getInputStream());
			this.outputStream = new ObjectOutputStream(socket.getOutputStream());

			while (running) {

				RequestTO request = (RequestTO) inStream.readObject();

				Response response = null;

				switch (request.getType()) {
				case List:
					response = list();
					break;
				case File:
					response = download((DownloadFileRequest) request
							.getRequest());
					break;
				case Upload:
					response = upload((UploadRequest) request.getRequest());
					break;
				case Version:
					response = version((VersionRequest) request.getRequest());
					break;
				default:
					response = new MessageResponse("ERROR!");
					break;
				}
				outputStream.writeObject(response);
			}
		} catch (EOFException e) {
			running = false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1); // TODO clean shutdown of thread
		} finally {
			try {
				socket.close();
				inStream.close();
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Response list() throws IOException {
		return new ListResponse(new LinkedHashSet<String>(Arrays.asList(server
				.getFileNames())));
	}

	@Override
	public Response download(DownloadFileRequest request) throws IOException {
		DownloadTicket ticket = request.getTicket();
		byte[] content = FileUtils.getFile(server.getPath(),
				ticket.getFilename());
		DownloadFileResponse response = new DownloadFileResponse(ticket,
				content);
		return response;
	}

	@Override
	public Response info(InfoRequest request) throws IOException {
		// TODO implement info
		return null;
	}

	@Override
	public Response version(VersionRequest request) throws IOException {
		int version = 0;

		FileInfo f = server.getFileInfo(request.getFilename());
		if (f == null)
			return new VersionResponse(request.getFilename(), 0);
		return new VersionResponse(request.getFilename(), version);
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {

		server.persist(request);

		return null;
	}

}
