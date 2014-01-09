package model;

import java.io.Serializable;

/**
 * Contains information required to download files from a {@link server.IFileServer}.
 */
public final class DownloadTicket implements Serializable {
	private static final long serialVersionUID = 289413562241940171L;


	private DownloadFileRequest info;
	private RequestTO request;
	private byte[] checksum;


	/**
	 * @param request
	 * @param checksum2
	 */
	public DownloadTicket(RequestTO request, byte[] checksum) {
		this.info = (DownloadFileRequest) request.getRequest();
		this.request = request;
		this.checksum = checksum;
	}

	/**
	 * @return the checksum
	 */
	public byte[] getChecksum() {
		return checksum;
	}
	
	/**
	 * @return the info
	 */
	public DownloadFileRequest getInfo() {
		return info;
	}

	@Override
	public String toString() {
		return String.format("%s@%s - %s", info.getFilename(), info.getPort(), new String(checksum));
	}

	/**
	 * @return
	 */
	public RequestTO getRequest() {
		return request;
	}
}
