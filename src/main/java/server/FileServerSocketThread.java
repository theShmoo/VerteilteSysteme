package server;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedHashSet;

import message.Response;
import message.request.InfoRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.DetailedListResponse;
import message.response.DownloadFileResponse;
import message.response.InfoResponse;
import message.response.ListResponse;
import message.response.MessageResponse;
import message.response.VersionResponse;
import model.DownloadFileRequest;
import model.FileInfo;
import model.RequestTO;
import proxy.Proxy;
import util.FileUtils;
import util.TCPChannel;
import util.UnexpectedCloseException;
import client.Client;

/**
 * This Thread handles incoming requests from {@link Client} and {@link Proxy}
 * 
 * @author David
 */
public class FileServerSocketThread extends TCPChannel implements IFileServer {

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
		super(socket);
		this.server = fileserver;
		activateIntegrity(server.getHMac());
	}

	@Override
	public void run() {
		try {

			while (running) {

				Object input = receive();

				RequestTO request = null;
				Response response = null;

				if (!(input instanceof RequestTO)) {
					// major error
				} else {
					request = (RequestTO) input;

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
						response = version((VersionRequest) request
								.getRequest());
						break;
					case Info:
						response = info((InfoRequest) request.getRequest());
						break;
					case DetailedList:
						response = detailedList();
						break;
					default:
						response = new MessageResponse("ERROR!");
						break;
					}
					send(response);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnexpectedCloseException e) {
			running = false;
		} finally {
			close();
		}
	}

	@Override
	public Response list() throws IOException {
		return new ListResponse(new LinkedHashSet<String>(Arrays.asList(server
				.getFileNames())));
	}

	/**
	 * Get a Set of fileinfos available on this Fileserver
	 * 
	 * @return a Set of fileinfos
	 * @throws IOException
	 */
	public Response detailedList() throws IOException {
		return new DetailedListResponse(server.getFiles());
	}

	@Override
	public Response download(DownloadFileRequest request) throws IOException {
			byte[] content = FileUtils.read(server.getPath(),
					request.getFilename());
			return new DownloadFileResponse(content);
	}

	@Override
	public Response info(InfoRequest request) throws IOException {
		FileInfo f = server.getFileInfo(request.getFilename());
		if (f == null) {
			return new MessageResponse("The file \"" + request.getFilename()
					+ "\" does not exist!");
		}
		return new InfoResponse(request.getFilename(), f.getFilesize());
	}

	@Override
	public Response version(VersionRequest request) throws IOException {
		FileInfo f = server.getFileInfo(request.getFilename());
		if (f == null)
			return new VersionResponse(request.getFilename(), 0);
		return new VersionResponse(request.getFilename(), f.getVersion());
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {

		server.persist(request);
		return new MessageResponse("File successfully uploaded.");
	}
}
