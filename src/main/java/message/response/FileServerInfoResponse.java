package message.response;

import message.Response;
import model.FileServerInfo;
import java.util.List;

/**
 * Retrieves information about each known file server, online or offline.
 *
 * E.g.:
 * <pre>
 * &gt; !fileservers
 * 1. IP:127.0.0.1 Port:10000 offline Usage: 752
 * 2. IP:127.0.0.2 Port:10000 online Usage: 220
 * </pre>
 */
public class FileServerInfoResponse implements Response {
	private static final long serialVersionUID = -2527380842129589182L;

	private List<FileServerInfo> fileServerInfo;

	public FileServerInfoResponse(List<FileServerInfo> fileServerInfo) {
		this.fileServerInfo = fileServerInfo;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getFileServerInfo().size(); i++) {
			sb.append(i + 1).append(". ").append(getFileServerInfo().get(i)).append("\n");
		}
		return sb.length() > 0 ? sb.toString() : "No file servers connected\n";
	}

	public List<FileServerInfo> getFileServerInfo() {
		return fileServerInfo;
	}
}
